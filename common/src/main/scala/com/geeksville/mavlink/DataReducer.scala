package com.geeksville.mavlink

import scala.collection.mutable.HashMap
import org.mavlink.messages.ardupilotmega._
import org.mavlink.messages.MAVLinkMessage

/**
 * A utility class that takes in messages and emits a fewer number of messages.
 * All message types are emitted at most once every X seconds, with the exception of
 * critical message types (waypoints, param values etc...) those are always kept
 */
class DataReducer(val throttleMsec: Int = 1000) {

  // We let each vehicle have its own set of throttles
  private case class MsgId(sysId: Int, commandId: Int)

  private val throttles = HashMap[MsgId, ThrottleByTime]()

  private def allowedByThrottle(m: TimestampedMessage): Boolean = {
    val t = throttles.getOrElseUpdate(MsgId(m.msg.sysId, m.msg.messageType), new ThrottleByTime(throttleMsec))

    t.isAllowed(m.timeMsec)
  }

  def filter(m: TimestampedMessage) = DataReducer.isCritical(m.msg) || allowedByThrottle(m)
}

object DataReducer {
  val criticalCommands = Set(
    msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE,
    msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM,
    msg_statustext.MAVLINK_MSG_ID_STATUSTEXT,
    msg_set_mode.MAVLINK_MSG_ID_SET_MODE)

  /// Commands a user is likely to want to plot
  val plottableCommands = Set(
    // msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT,
    msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT,
    msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW,
    msg_rc_channels_scaled.MAVLINK_MSG_ID_RC_CHANNELS_SCALED,
    msg_attitude.MAVLINK_MSG_ID_ATTITUDE,
    msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT,
    msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS,
    msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD,
    msg_set_mode.MAVLINK_MSG_ID_SET_MODE)

  /// Useful for currying
  def filterByIds(allowed: Set[Int])(m: MAVLinkMessage) = allowed.contains(m.messageType)

  def isCritical = filterByIds(criticalCommands) _
}

private class ThrottleByTime(minIntervalMsec: Int) {
  private var lasttimeMsec = 0L

  /**
   * A new event has occurred, should it pass the throttle?
   */
  def isAllowed(now: Long) = {
    val span = now - lasttimeMsec
    if (span >= minIntervalMsec || span < 0) {
      lasttimeMsec = now
      true
    } else
      false
  }

}