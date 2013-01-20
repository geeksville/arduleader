package com.geeksville.flight

import com.geeksville.mavlink.HeartbeatMonitor
import org.mavlink.messages.ardupilotmega._

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleMonitor extends HeartbeatMonitor {

  var status: Option[String] = None
  var location: Option[Location] = None

  protected def onLocationChanged(l: Location) {}
  protected def onStatusChanged(s: String) {}

  private def mReceive: Receiver = {
    case m: msg_statustext =>
      log.info("Received status: " + m.getText)
      status = Some(m.getText)
      onStatusChanged(m.getText)

    case msg: msg_global_position_int ⇒
      val loc = VehicleSimulator.decodePosition(msg)
      log.info("Received location: " + loc)
      location = Some(loc)
      onLocationChanged(loc)
  }

  override def onReceive = mReceive.orElse(super.onReceive)
}