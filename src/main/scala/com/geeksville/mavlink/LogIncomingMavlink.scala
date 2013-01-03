package com.geeksville.mavlink

import akka.actor.Actor
import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import LogIncomingMavlink._

class LogIncomingMavlink(sysId: Int, allow: MAVLinkMessage => Boolean = allowDefault) extends InstrumentedActor {
  MavlinkEventBus.subscribe(self, sysId)

  def receive = {
    case msg: msg_statustext =>
      log.info("Rcv" + msg.sysId + ": " + msg.getText)

    case msg: MAVLinkMessage ⇒
      def str = "Rcv" + msg.sysId + ": " + msg
      if (allow(msg))
        log.info(str)
      else
        log.debug(str)
  }
}

object LogIncomingMavlink {
  val boringMessages: Set[Class[_]] = Set(classOf[msg_heartbeat], classOf[msg_sys_status])

  def allowDefault(msg: MAVLinkMessage) = !boringMessages.contains(msg.getClass)
  def allowNothing(msg: MAVLinkMessage) = false
}