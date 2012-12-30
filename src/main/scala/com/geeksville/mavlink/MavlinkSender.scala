package com.geeksville.mavlink

import akka.actor.Actor
import com.geeksville.flight._
import org.mavlink.messages.MAVLinkMessage
import java.net._

/**
 * An actor that forwards mavlink packets out via UDP
 */
class MavlinkSender extends Actor {

  val host = "localhost"
  val port = 51200

  // Get the internet address of the specified host
  private val address = InetAddress.getByName(host)

  // Initialize a datagram packet with data and address

  // Create a datagram socket, send the packet through it, close it.
  private val socket = new DatagramSocket

  def receive = {
    case msg: MAVLinkMessage ⇒
      println("Sending: " + msg)
      val bytes = msg.encode()
      val packet = new DatagramPacket(bytes, bytes.length,
        address, port)
      socket.send(packet)
  }

  override def postStop() {
    socket.close()
    super.postStop()
  }
}