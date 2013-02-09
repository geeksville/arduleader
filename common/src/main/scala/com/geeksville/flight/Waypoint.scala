package com.geeksville.flight

import org.mavlink.messages.ardupilotmega.msg_mission_item
import org.mavlink.messages.MAV_CMD
import org.mavlink.messages.MAV_FRAME

/**
 * A wrapper for waypoints - to provide a higher level API
 */
case class Waypoint(val msg: msg_mission_item) {
  private val commandCodes = Map(
    MAV_CMD.MAV_CMD_NAV_TAKEOFF -> "Takeoff",
    MAV_CMD.MAV_CMD_NAV_WAYPOINT -> "Waypoint", // Navigate to Waypoint
    MAV_CMD.MAV_CMD_NAV_LAND -> "Land", // LAND to Waypoint
    MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM -> "Loiter", // Loiter indefinitely
    MAV_CMD.MAV_CMD_NAV_LOITER_TURNS -> "LoiterN", // Loiter N Times
    MAV_CMD.MAV_CMD_NAV_LOITER_TIME -> "LoiterT",
    MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH -> "RTL")

  private val frameCodes = Map(
    MAV_FRAME.MAV_FRAME_GLOBAL -> "MSL",
    MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT -> "AGL")

  def commandStr = commandCodes.get(msg.command).getOrElse("cmd=" + msg.command)
  def frameStr = frameCodes.get(msg.frame).getOrElse("frame=" + msg.frame)

  def seq = msg.seq

  /// The magic home position
  def isHome = (msg.current != 2) && (msg.seq == 0)

  /// If the airplane is heading here
  def isCurrent = msg.current == 1

  def isMSL = msg.frame == MAV_FRAME.MAV_FRAME_GLOBAL

  def altitude = msg.z

  /**
   * A short description of this waypoint
   */
  def shortString = {
    val r = if (isHome)
      "Home"
    else
      commandStr

    r
  }

  /**
   * Longer descriptiong (with arguments)
   */
  def longString = {
    import msg._
    val params = Seq(param1, param2, param3, param4)
    val hasParams = params.find(_ != 0.0f).isDefined
    val r = if (hasParams)
      "Alt=%sm %s params=%s".format(z, frameStr, params.mkString(","))
    else
      "Altitude %sm %s".format(z, frameStr)

    shortString + ": " + r
  }
}