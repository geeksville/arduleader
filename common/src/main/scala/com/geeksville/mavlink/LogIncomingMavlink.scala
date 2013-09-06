package com.geeksville.mavlink

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import LogIncomingMavlink._

class LogIncomingMavlink(sysId: Int, allow: MAVLinkMessage => Boolean = allowDefault) extends InstrumentedActor {
  MavlinkEventBus.subscribe(self, sysId)

  def onReceive = {
    case msg: msg_rc_channels_override =>
      val sent = ((msg.chan4_raw & 0xffffffffL) << 48L) | ((msg.chan3_raw & 0xffffffffL) << 32L) | ((msg.chan2_raw & 0xffffffffL) << 16L) | (msg.chan1_raw & 0xffffffffL)
      val now = System.currentTimeMillis()
      log.info("RC override latency " + (now - sent) + " ms")

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
  val boringMessages: Set[Class[_]] = Set(
    classOf[msg_heartbeat],
    classOf[msg_sys_status],
    classOf[msg_hwstatus],
    classOf[msg_rc_channels_scaled],
    classOf[msg_rc_channels_raw],
    classOf[msg_servo_output_raw],
    classOf[msg_meminfo],
    classOf[msg_raw_imu],
    classOf[msg_raw_pressure],
    classOf[msg_gps_raw_int],
    classOf[msg_scaled_pressure])

  def allowDefault(msg: MAVLinkMessage) = !boringMessages.contains(msg.getClass)
  def allowNothing(msg: MAVLinkMessage) = false
}