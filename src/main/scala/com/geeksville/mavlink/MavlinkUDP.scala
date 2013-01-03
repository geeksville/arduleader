package com.geeksville.mavlink

import java.net._
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.MAVLinkMessageFactory
import org.mavlink.IMAVLinkMessage
import com.geeksville.util.ThreadTools
import akka.event._
import com.geeksville.flight._
import akka.actor.Actor
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
class MavlinkUDP extends InstrumentedActor with MavlinkReceiver {

  // Do I open a port and listen at that address, or am I trying to reach out and talk to a particular port
  val isListener = false

  val portNumber: Int = 14550
  val serverHost = InetAddress.getByName("localhost")

  val socket = if (isListener) new DatagramSocket(portNumber) else new DatagramSocket()

  val thread = ThreadTools.createDaemon("UDPMavReceive")(worker)

  /**
   * The app last received packets from on our wellknown port number
   */
  var remote: Option[SocketAddress] = None

  thread.start()

  def receive = {
    case msg: MAVLinkMessage ⇒
      log.debug("Sending: " + msg)
      val bytes = msg.encode()
      if (isListener) {
        remote.map { r =>
          val packet = new DatagramPacket(bytes, bytes.length, r)
          socket.send(packet)
        }.getOrElse {
          log.debug("Can't send message, we haven't heard from a peer")
        }
      } else {
        val packet = new DatagramPacket(bytes, bytes.length, serverHost, portNumber)
        socket.send(packet)
      }
  }

  override def postStop() {
    socket.close() // Force thread exit
    super.postStop()
  }

  private def receivePacket() = {
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
    while (true) {
      receivePacket.foreach(handlePacket)
    }
  }
}

