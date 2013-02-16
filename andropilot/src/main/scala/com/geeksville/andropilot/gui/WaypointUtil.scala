package com.geeksville.andropilot.gui

import org.mavlink.messages.MAV_CMD
import com.geeksville.andropilot.R

/**
 * Android specific waypoint utilities.
 */
object WaypointUtil {
  val wpToDrawable = Map(MAV_CMD.MAV_CMD_NAV_TAKEOFF -> R.drawable.waypoint_takeoff,
    MAV_CMD.MAV_CMD_NAV_WAYPOINT -> R.drawable.waypoint_dot,
    MAV_CMD.MAV_CMD_NAV_LAND -> R.drawable.waypoint_land,
    MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM -> R.drawable.waypoint_forever,
    MAV_CMD.MAV_CMD_NAV_LOITER_TURNS -> R.drawable.waypoint_number,
    MAV_CMD.MAV_CMD_NAV_LOITER_TIME -> R.drawable.waypoint_timed,
    MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH -> R.drawable.waypoint_rtl,
    MAV_CMD.MAV_CMD_NAV_LAND -> R.drawable.waypoint_land,
    MAV_CMD.MAV_CMD_DO_JUMP -> R.drawable.yellow)

  val defaultDrawable = R.drawable.blue

  /// Get a drawable suitable for use by android
  def toDrawable(cmd: Int) = wpToDrawable.getOrElse(cmd, defaultDrawable)
}