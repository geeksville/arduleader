package com.geeksville.mavlink

import java.net._
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.MAVLinkMessageFactory
import org.mavlink.IMAVLinkMessage
import com.geeksville.util.ThreadTools
import com.geeksville.akka.InstrumentedActor

/**
 * published on our eventbus when someone wants a packet sent to the outside world
 */
// case class MavlinkSend(message: MAVLinkMessage)

/**
 * Receive UDPMavlink messages and forward to actors
 * Use with mavproxy like so:
 * Following instructions are stale...
 * mavproxy.py --master=/dev/ttyACM0 --master localhost:51200 --out=localhost:51232
 *
 * FIXME - make sure we don't overrun the rate packets can be read
 */
class MavlinkUDP(destHostName: String = "localhost", val destPortNumber: Option[Int] = Some(14550), val localPortNumber: Option[Int] = None) extends InstrumentedActor with MavlinkReceiver {

  val portNumber: Int = 14550

  // These must be lazy - to ensure we don't do networking in the main thread (an android restriction)
  lazy val serverHost = InetAddress.getByName(destHostName)
  lazy val socket = localPortNumber.map { n => new DatagramSocket(n) }.getOrElse(new DatagramSocket)

  val thread = ThreadTools.createDaemon("UDPMavReceive")(worker _)

  /**
   * The app last received packets from on our wellknown port number
   */
  var remote: Option[SocketAddress] = None

  thread.start()

  def onReceive = {
    case msg: MAVLinkMessage ⇒
      log.debug("UDPSend: " + msg)
      val bytes = msg.encode()

      // Do we know a remote port?
      destPortNumber.map { destPort =>
        val packet = new DatagramPacket(bytes, bytes.length, serverHost, destPort)
        socket.send(packet)
      }.getOrElse {
        // Has anyone called into us?

        remote.map { r =>
          val packet = new DatagramPacket(bytes, bytes.length, r)
          socket.send(packet)
        }.getOrElse {
          log.debug("Can't send message, we haven't heard from a peer")
        }
      }
  }

  override def postStop() {
    socket.close() // Force thread exit
    super.postStop()
  }

  private def receivePacket() = {
    //log.info("In receive udp packet")

    val bytes = new Array[Byte](512)
    val packet = new DatagramPacket(bytes, bytes.length)
    socket.receive(packet)
    remote = Some(packet.getSocketAddress)

    val msg = packet.getData

    msg match {
      case Array(start, payLength, packSeq, sysId, compId, msgId, payload @ _*) =>
        if (start == IMAVLinkMessage.MAVPROT_PACKET_START_V10) {
          val packet = MAVLinkMessageFactory.getMessage(msgId & 0xff, sysId & 0xff, compId & 0xff, payload.take(payLength).toArray)
          log.debug("Mav rx sysId=%d: %s".format(sysId & 0xff, packet))
          Option(packet)
        } else {
          log.error("Ignoring bad MAVLink packet")
          None
        }
      case _ =>
        log.error("Ignoring bad match")
        None
    }
  }

  private def worker() {
    try {
      while (!self.isTerminated) {
        receivePacket.foreach(handlePacket)
      }
    } catch {
      case ex: SocketException =>
        if (!self.isTerminated) // If we are shutting down, ignore socket exceptions
          throw ex

      case ex: Exception =>
        log.warn("exception in UDP receiver: " + ex)
    }

    log.debug("UDP receiver exiting")
  }
}

