package com.geeksville.mavlink

import java.net._
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.MAVLinkMessageFactory
import org.mavlink.IMAVLinkMessage
import com.geeksville.util.ThreadTools
import akka.event._
import com.geeksville.flight._
import akka.actor.Actor

/**
 * published on our eventbus
 */
case class MavlinkReceived(message: MAVLinkMessage)

/**
 * Receive UDPMavlink messages and forward to actors
 * Use with mavproxy like so:
 * mavproxy.py --master=/dev/ttyACM0 --master localhost:51200 --out=localhost:51232
 *
 * FIXME - make sure we don't overrun the rate packets can be read
 */
class MavlinkReceiver extends Actor {

  val portNumber: Int = 51232
  val socket = new DatagramSocket(portNumber)

  val thread = ThreadTools.createDaemon("UDPMavReceive")(worker)

  /**
   * For now we pipe all our notifications through the system event stream - we might refine this later
   */
  val destEventBus = context.system.eventStream

  thread.start()

  def receive = {
    case None =>
      println("FIXME - no receiver needed?")
  }

  private def handlePacket(msg: MAVLinkMessage) {
    destEventBus.publish(MavlinkReceived(msg))
  }

  override def postStop() {
    socket.close() // Force thread exit
  }

  private def receivePacket() = {
    val bytes = new Array[Byte](512)
    val packet = new DatagramPacket(bytes, bytes.length)
    socket.receive(packet)
    val msg = packet.getData

    msg match {
      case Array(start, payLength, packSeq, sysId, compId, msgId, payload @ _*) =>
        if (start == IMAVLinkMessage.MAVPROT_PACKET_START_V10) {
          Option(MAVLinkMessageFactory.getMessage(msgId & 0xff, sysId & 0xff, compId & 0xff, payload.take(payLength).toArray))
        } else {
          println("Error: Ignoring bad MAVLink packet")
          None
        }
      case _ =>
        println("Error: Ignoring bad match")
        None
    }
  }

  private def worker() {
    while (true) {
      receivePacket.foreach(handlePacket)
    }
  }
}