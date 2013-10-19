package com.geeksville.mavlink

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import LogIncomingMavlink._
import scala.collection.mutable.HashMap

/**
 * Keep a map of the most recent packet of any received type
 */
class LastSeenPackets(sysId: Int) extends InstrumentedActor {
  MavlinkEventBus.subscribe(self, sysId)

  val recent = HashMap[Int, MAVLinkMessage]()

  def toLongString = recent.values.mkString("Recent packets:\n", "\n", "\n")

  def onReceive = {
    case msg: MAVLinkMessage =>
      recent(msg.messageType) = msg
  }
}

