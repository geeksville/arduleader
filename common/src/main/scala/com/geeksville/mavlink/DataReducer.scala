package com.geeksville.mavlink

import scala.collection.mutable.HashMap
import org.mavlink.messages.ardupilotmega._

/**
 * A utility class that takes in messages and emits a fewer number of messages.
 * All message types are emitted at most once every X seconds, with the exception of
 * critical message types (waypoints, param values etc...) those are always kept
 */
class DataReducer {

  val throttleMsec = 1000

  private case class MsgId(sysId: Int, commandId: Int)

  private val throttles = HashMap[MsgId, ThrottleByTime]()

  private val criticalCommands = Set(
    msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE,
    msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM,
    msg_statustext.MAVLINK_MSG_ID_STATUSTEXT,
    msg_set_mode.MAVLINK_MSG_ID_SET_MODE)

  private def allowedByThrottle(m: TimestampedMessage): Boolean = {
    val t = throttles.getOrElseUpdate(MsgId(m.msg.sysId, m.msg.messageType), new ThrottleByTime(throttleMsec))

    t.isAllowed(m.timeMsec)
  }

  def filter(m: TimestampedMessage) = criticalCommands.contains(m.msg.messageType) || allowedByThrottle(m)
}

private class ThrottleByTime(minIntervalMsec: Int) {
  private var lasttimeMsec = 0L

  /**
   * A new event has occured, should it pass the throttle?
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