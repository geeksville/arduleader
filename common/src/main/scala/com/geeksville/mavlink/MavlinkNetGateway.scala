package com.geeksville.mavlink

import java.net._
import org.mavlink.messages.MAVLinkMessage
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
trait MavlinkNetGateway extends MavlinkSender with MavlinkReceiver {
  private val thread = ThreadTools.createDaemon("netgw")(worker _)

  private var shuttingDown = false

  thread.start()

  protected def socket: Socket

  protected def sendPacket(b: Array[Byte]): Unit
  protected def receivePacket(): Option[MAVLinkMessage]

  protected def doSendMavlink(bytes: Array[Byte]) {
    sendPacket(bytes)
  }

  override def postStop() {
    shuttingDown = true
    socket.close() // Force thread exit
    super.postStop()
  }

  private def worker() {
    try {
      while (!shuttingDown) {
        receivePacket.foreach(handleIncomingPacket)
      }
    } catch {
      case ex: SocketException =>
        if (!shuttingDown) // If we are shutting down, ignore socket exceptions
          throw ex

      case ex: Exception =>
        log.warning("exception in Net receiver: " + ex)
    }

    log.debug("Net receiver exiting")
  }
}

