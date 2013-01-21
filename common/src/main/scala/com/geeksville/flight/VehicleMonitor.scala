package com.geeksville.flight

import com.geeksville.mavlink.HeartbeatMonitor
import org.mavlink.messages.ardupilotmega._

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleMonitor extends HeartbeatMonitor with VehicleSimulator {

  var status: Option[String] = None
  var location: Option[Location] = None
  var batteryPercent: Option[Float] = None

  var waypoints = Seq[msg_mission_item]()

  private var numWaypointsRemaining = 0
  private var nextWaypointToFetch = 0

  override def systemId = 253 // We always claim to be a ground controller (FIXME, find a better way to pick a number)

  protected def onLocationChanged(l: Location) {}
  protected def onStatusChanged(s: String) {}
  protected def onSysStatusChanged() {}
  protected def onWaypointsDownloaded() {}

  private val codeToModeMap = Map(0 -> "MANUAL", 1 -> "CIRCLE", 2 -> "STABILIZE",
    5 -> "FLY_BY_WIRE_A", 6 -> "FLY_BY_WIRE_B", 10 -> "AUTO",
    11 -> "RTL", 12 -> "LOITER", 15 -> "GUIDED", 16 -> "INITIALIZING")

  private val modeToCodeMap = codeToModeMap.map { case (k, v) => (v, k) }

  def currentMode = codeToModeMap.getOrElse(customMode.getOrElse(-1), "unknown")

  /**
   * The mode names we understand
   */
  def modeNames = modeToCodeMap.keys

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

    //
    // Messages for downloading waypoints from vehicle
    //

    case msg: msg_mission_count =>
      if (msg.target_system == systemId) {
        log.info("Vehicle has %d waypoints, downloading...".format(msg.count))
        // We were just told how many waypoints the target has, now fetch them (one at a time)
        numWaypointsRemaining = msg.count
        nextWaypointToFetch = 0
        waypoints = Seq()
        requestNextWaypoint()
      }

    case msg: msg_mission_item =>
      if (msg.target_system == systemId) {
        log.debug("Receive: " + msg)
        waypoints = waypoints :+ msg

        /*
 * MISSION_ITEM {target_system : 255, target_component : 190, seq : 0, frame : 0, command : 16, current : 1, autocontinue : 1, param1 : 0.0, param2 : 0.0, param3 : 0.0, param4 : 0.0, x : 37.5209159851, y : -122.309059143, z : 143.479995728}
 */
        nextWaypointToFetch += 1
        requestNextWaypoint()
      }
  }

  override def onHeartbeatFound() {
    super.onHeartbeatFound()

    // First contact, download any waypoints from the vehicle
    startWaypointDownload()
  }

  def startWaypointDownload() {
    sendMavlink(missionRequestList())
  }

  /**
   * Tell vehicle to select a new mode
   */
  def setMode(mode: String) {
    sendMavlink(setMode(modeToCodeMap(mode)))
  }

  /**
   * FIXME - support timeouts
   */
  private def requestNextWaypoint() {
    if (numWaypointsRemaining > 0) {
      numWaypointsRemaining -= 1
      sendMavlink(missionRequest(nextWaypointToFetch))
    } else
      onWaypointsDownloaded()
  }

  override def onReceive = mReceive.orElse(super.onReceive)
}