package com.geeksville.mavlink

import java.net._
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.MAVLinkMessageFactory
import org.mavlink.IMAVLinkMessage
import com.geeksville.util.ThreadTools

/**
 * Receive UDPMavlink messages and forward to actors
 * Use with mavproxy like so:
 * mavproxy.py --master=/dev/ttyACM0 --out=localhost:51232
 *
 * FIXME - hook to actors
 */
class UDPMavlinkReceiver(val portNumber: Int = 51232) {
  val socket = new DatagramSocket(portNumber)

  val thread = ThreadTools.createDaemon("UDPMavReceive")(worker)

  thread.start()

  def close() {
    socket.close() // Force thread exit
  }

  def receivePacket() = {
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

  def worker() {
    while (true) {
      receivePacket.foreach { p =>
        println("received: " + p)
      }
    }
  }
}