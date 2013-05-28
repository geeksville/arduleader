package com.geeksville.flight

/**
 * A mixin that adds waypointsForMap
 */
trait WaypointsForMap {

  def waypoints: Seq[Waypoint]

  /**
   * An expanded version of waypoints (i.e. resolving jumps), but removing any waypoints that don't have position
   */
  def waypointsForMap = {
    var index = 0
    val inspected = Array.fill(waypoints.size)(false)

    // No matter what we never want to emit more waypoints than we started with
    (0 until waypoints.size).flatMap { loopNum =>
      if (index >= waypoints.size)
        None
      else if (!inspected(index)) {
        val wp = waypoints(index)
        inspected(index) = true

        if (wp.isJump) {
          index = wp.jumpSequence
          None
        } else {
          index += 1
          if (!wp.isForMap)
            None
          else
            Some(wp)
        }
      } else {
        // Already seen it
        index += 1
        None
      }
    }
  }
}

