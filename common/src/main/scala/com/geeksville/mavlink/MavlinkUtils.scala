package com.geeksville.mavlink

import org.mavlink.IMAVLinkMessage
import org.mavlink.messages.MAVLinkMessageFactory
import org.mavlink.messages.MAVLinkMessage

object MavlinkUtils {

  /// A more verbose tostring for mavlink msgs
  def toString(m: MAVLinkMessage) = {
    s"$m sysId=${m.sysId} compId=${m.componentId}"
  }

  def bytesToPacket(msg: Array[Byte]): Option[MAVLinkMessage] = {

    //log.info("Processing packet from " + remote.get)

    msg match {
      case Array(start, payLength, packSeq, sysId, compId, msgId, payload @ _*) =>
        if (start == IMAVLinkMessage.MAVPROT_PACKET_START_V10) {
          val packet = MAVLinkMessageFactory.getMessage(msgId & 0xff, sysId & 0xff, compId & 0xff, payload.take(payLength).toArray)
          //log.debug("Mav rx sysId=%d: %s".format(sysId & 0xff, packet))
          Option(packet)
        } else {
          //log.error("Ignoring bad MAVLink packet")
          None
        }
      case _ =>
        //log.error("Ignoring bad match")
        None
    }
  }
}