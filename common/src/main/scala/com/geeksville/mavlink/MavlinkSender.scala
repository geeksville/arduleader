package com.geeksville.mavlink

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._
import akka.actor.Stash

case class SendYoungest(msg: MAVLinkMessage)

/**
 * An actor that watches its inbound channel and sends any mavlink messages it recieves via some sort of transport
 *
 * It is better to leave the output buffer as shallow as possible so we have the option to wrap packets with SendYoungest().  If a packet is wrapped inside
 * a SendOnce instance we will scour the MavlinkStream actor incoming message queue for dups and send _only_ the freshest packet with that ID.
 * This allows us to be sure things like joystick set rc-chanel events never get bunched up.
 *
 */
trait MavlinkSender extends InstrumentedActor {

  /**
   * Provided by subclass
   */
  protected def doSendMavlink(bytes: Array[Byte])

  var seqNum = 0

  private def assignSequence(msg: MAVLinkMessage) = {
    msg.sequence = seqNum
    seqNum = (seqNum + 1) & 0xff
    //log.debug(s"sending $msg")
    msg
  }

  def onReceive = {
    case SendYoungest(msg) =>
      doSendMavlink(assignSequence(findYoungest(msg).get).encode())

    case msg: MAVLinkMessage =>
      //log.debug("UDPSend: " + msg)
      doSendMavlink(assignSequence(msg).encode())
  }

  /**
   * Scour our message queue to find fresher versions of the specified message (recursive)
   */
  private def findYoungest(msg: MAVLinkMessage): Option[MAVLinkMessage] = {
    val younger = nextYoungest(msg)

    // Did we find anything younger?
    younger match {
      case Some(m) => findYoungest(m)
      case None => Some(msg)
    }
  }

  private def nextYoungest(msg: MAVLinkMessage): Option[MAVLinkMessage] = {
    log.warning("FIXME - using busted nextYoungest due to akka")
    None
  }

  /* Need to figure out how to make this work with akka - probably by specifying a custom msg queue?
    private def nextYoungest(msg: MAVLinkMessage): Option[MAVLinkMessage] = {
    val r = receiveWithin(0) {
      case SendYoungest(m) if m.messageType == msg.messageType =>
        Some(m)
      case TIMEOUT =>
        None
    }

    //log.debug("nextYoungest = " + r)
    r
  }
  */
}