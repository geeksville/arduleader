package com.geeksville.flight

import com.geeksville.mavlink.HeartbeatMonitor
import org.mavlink.messages.ardupilotmega._

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleMonitor extends HeartbeatMonitor {

  var status: Option[String] = None
  var location: Option[Location] = None
  var batteryPercent: Option[Float] = None

  protected def onLocationChanged(l: Location) {}
  protected def onStatusChanged(s: String) {}
  protected def onSysStatusChanged() {}

  private val modeMap = Map(0 -> "MANUAL", 1 -> "CIRCLE", 2 -> "STABILIZE",
    5 -> "FLY_BY_WIRE_A", 6 -> "FLY_BY_WIRE_B", 10 -> "AUTO",
    11 -> "RTL", 12 -> "LOITER", 15 -> "GUIDED", 16 -> "INITIALIZING")

  def currentMode = modeMap.getOrElse(customMode.getOrElse(-1), "unknown")

  private def mReceive: Receiver = {
    case m: msg_statustext =>
      log.info("Received status: " + m.getText)
      status = Some(m.getText)
      onStatusChanged(m.getText)

    case msg: msg_sys_status =>
      batteryPercent = Some(msg.battery_remaining / 100.0f)
      onSysStatusChanged()

    case msg: msg_global_position_int ⇒
      val loc = VehicleSimulator.decodePosition(msg)
      //log.debug("Received location: " + loc)
      location = Some(loc)
      onLocationChanged(loc)
  }

  override def onReceive = mReceive.orElse(super.onReceive)
}