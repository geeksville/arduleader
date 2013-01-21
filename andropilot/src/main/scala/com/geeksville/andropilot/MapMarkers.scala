package com.geeksville.andropilot

import com.geeksville.gmaps._
import org.mavlink.messages.ardupilotmega.msg_mission_item
import com.ridemission.scandroid.AndroidLogger

class WaypointMarker(val msg: msg_mission_item) extends SmartMarker with AndroidLogger {
  def lat = msg.x
  def lon = msg.y
  override def title = Some("Waypoint #" + msg.seq)
  override def snippet = Some(msg.toString)
  override def draggable = true

  override def toString = title.get

  override def onDragEnd() {
    super.onDragEnd()
    debug("Drag ended on " + this)
  }
}