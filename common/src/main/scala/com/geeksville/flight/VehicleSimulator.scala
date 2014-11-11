package com.geeksville.flight

import com.geeksville.flight._
import scala.concurrent.duration._
import scala.language.postfixOps
import org.mavlink.messages.ardupilotmega._
import org.mavlink.messages._
import java.util.GregorianCalendar
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.SendYoungest
import com.geeksville.mavlink.CanSendMavlink

/**
 * Pretend to be a vehicle, generating mavlink messages for our system id.
 *
 */
trait VehicleSimulator extends CanSendMavlink { // InstrumentedActor

  def componentId = 190 // FIXME, just copied what mission control was doing

  var groundAltitude = 0.0

  /**
   * We want to use a 'system boot time' but for now I'll just pick something that is relatively recent (so 32 bit msecs don't roll over)
   */
  private val startTime = (new GregorianCalendar(2012, 12, 30)).getTime.getTime

  var vehicleTypeCode = MAV_TYPE.MAV_TYPE_GCS // MAV_TYPE.MAV_TYPE_FLAPPING_WING // Close enough... ;-)

  // The custom mode we send in our outgoing heartbeat msgs
  var gcsCustomMode = 0
  var gcsBaseMode = MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED | MAV_MODE_FLAG.MAV_MODE_FLAG_AUTO_ENABLED
  var autopilotCode = MAV_AUTOPILOT.MAV_AUTOPILOT_INVALID

  /**
   * Created lazily so our customMode can be changed if we want
   */
  def heartbeat = {
    /*
     * type  uint8_t Type of the MAV (quadrotor, helicopter, etc., up to 15 types, defined in MAV_TYPE ENUM)
autopilot uint8_t Autopilot type / class. defined in MAV_AUTOPILOT ENUM
base_mode uint8_t System mode bitfield, see MAV_MODE_FLAGS ENUM in mavlink/include/mavlink_types.h
custom_mode uint32_t  A bitfield for use for autopilot-specific flags.
system_status uint8_t System status flag, see MAV_STATE ENUM
mavlink_version uint8_t_mavlink_version MAVLink version, not writable by user, gets added by protocol because of magic data type: uint8_t_mavlink_version
* */
    val msg = new msg_heartbeat(systemId, componentId)

    msg.`type` = vehicleTypeCode
    msg.autopilot = autopilotCode
    msg.base_mode = gcsBaseMode
    msg.custom_mode = gcsCustomMode
    msg.system_status = MAV_STATE.MAV_STATE_ACTIVE
    msg.mavlink_version = 3 // Seems to be what ardupilot uses
    msg
  }

  /**
   * This is the _source_ (not target) system id for when sending messages
   */
  def systemId: Int

  /**
   * The system we are trying to control
   */
  protected def targetSystem: Int = 1

  /**
   * In special cases (spectator mode, or a RX only radio, we want to suppress _any_ packet sends to the vehicle)
   */
  var listenOnly = false

  final def sendMavlink(m: MAVLinkMessage) {
    if (!listenOnly)
      handlePacket(m)
  }

  final def sendMavlink(m: SendYoungest) {
    if (!listenOnly)
      handlePacket(m)
  }

  /**
   * Set by ardupilot custom modes
   */
  def setMode(mode: Int) = {
    val msg = new msg_set_mode(systemId, componentId)
    msg.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED
    msg.custom_mode = mode
    msg.target_system = targetSystem

    msg
  }

  def commandLong(command: Int, targetComponent: Int = 1) = {
    val msg = new msg_command_long(systemId, componentId)
    msg.command = command
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def secondarySetMode(secondaryMode: Int, targetComponent: Int = 1) = {
    val r = commandLong(MAV_CMD.MAV_CMD_DO_SET_MODE, targetComponent)
    r.param1 = secondaryMode
    r
  }

  def commandAck() = {
    val msg = new msg_command_ack(systemId, componentId)
    msg.result = MAV_CMD_ACK.MAV_CMD_ACK_OK
    msg
  }

  /**
   * Do a manual levelling operation
   */
  def commandDoCalibrate(targetComponent: Int = 1,
    calINS: Boolean = false,
    calBaro: Boolean = false,
    calAccel: Boolean = false) = {
    val r = commandLong(MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION, targetComponent)
    r.param1 = if (calINS) 1 else 0 // Cal INS
    r.param2 = 0
    r.param3 = if (calBaro) 1 else 0 // Cal Baro
    r.param5 = if (calAccel) 1 else 0 // Cal accel
    r
  }

  protected def commandDoArm(armed: Boolean, targetComponent: Int = 250) = {
    val r = commandLong(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, targetComponent)
    r.param1 = if (armed) 1 else 0
    r.param2 = 0
    r
  }

  // The following variants aren't really needed, at least for Ardupilot you can just use SET_MODE

  def commandLoiter(targetComponent: Int = 1) = commandLong(MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM, targetComponent)
  def commandRTL(targetComponent: Int = 1) = commandLong(MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH, targetComponent)
  // def commandAuto(targetSystem: Int = 1, targetComponent: Int = 1) = commandLong(MAV_CMD.MAV_CMD_MISSION_START, targetSystem, targetComponent)

  def commandManual(targetComponent: Int = 1) = secondarySetMode(MAV_MODE.MAV_MODE_MANUAL_ARMED, targetComponent)
  def commandAuto(targetComponent: Int = 1) = secondarySetMode(MAV_MODE.MAV_MODE_AUTO_ARMED, targetComponent)
  def commandFBWA(targetComponent: Int = 1) = secondarySetMode(MAV_MODE.MAV_MODE_STABILIZE_ARMED, targetComponent)

  def paramRequestList(targetComponent: Int = 1) = {
    val msg = new msg_param_request_list(systemId, componentId)
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def paramSet(paramId: String, paramType: Int, paramVal: Float, targetComponent: Int = 1) = {
    val msg = new msg_param_set(systemId, componentId)
    msg.setParam_id(paramId)
    msg.param_value = paramVal
    msg.param_type = paramType
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def paramRequestReadByIndex(paramIndex: Int, targetComponent: Int = 1) = {
    val msg = new msg_param_request_read(systemId, componentId)
    msg.param_index = paramIndex
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def paramRequestReadById(id: String, targetComponent: Int = 1) = {
    val msg = new msg_param_request_read(systemId, componentId)
    msg.param_index = -1
    msg.setParam_id(id)
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def rcChannelsOverride(targetComponent: Int = 1) = {
    val msg = new msg_rc_channels_override(systemId, componentId)

    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def requestDataStream(id: Int, freqHz: Int, enabled: Boolean, targetComponent: Int = 1) = {
    val msg = new msg_request_data_stream(systemId, componentId)
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg.req_stream_id = id
    msg.req_message_rate = freqHz
    msg.start_stop = if (enabled) 1 else 0
    msg
  }

  def fenceFetchPoint(pointNum: Int, targetComponent: Int = 1) = {
    val msg = new msg_fence_fetch_point(systemId, componentId)
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg.idx = pointNum
    msg
  }

  def fencePoint(pointNum: Int, count: Int, lat: Float, lng: Float, targetComponent: Int = 1) = {
    val msg = new msg_fence_point(systemId, componentId)
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg.idx = pointNum
    msg.count = count
    msg.lat = lat
    msg.lng = lng
    msg
  }

  def missionRequestList(targetComponent: Int = 1) = {
    /* * MISSION_REQUEST_LIST {target_system : 1, target_component : 1}
* MISSION_COUNT {target_system : 255, target_component : 190, count : 1}
* MISSION_REQUEST {target_system : 1, target_component : 1, seq : 0} */

    val msg = new msg_mission_request_list(systemId, componentId)
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def missionRequest(seq: Int, targetComponent: Int = 1) = {
    /* * MISSION_REQUEST_LIST {target_system : 1, target_component : 1}
* MISSION_COUNT {target_system : 255, target_component : 190, count : 1}
* MISSION_REQUEST {target_system : 1, target_component : 1, seq : 0} */
    val msg = new msg_mission_request(systemId, componentId)
    msg.seq = seq
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def missionAck(typ: Int, targetComponent: Int = 1) = {
    val msg = new msg_mission_ack(systemId, componentId)
    msg.`type` = typ
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def missionCount(count: Int, targetComponent: Int = 1) = {
    val msg = new msg_mission_count(systemId, componentId)
    msg.count = count
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  /**
   * DO NOT USE - not supported on Copters
   */
  def missionWritePartial(start: Int, end: Int, targetComponent: Int = 1) = {
    val msg = new msg_mission_write_partial_list(systemId, componentId)
    msg.start_index = start
    msg.end_index = end
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def missionSetCurrent(seq: Int, targetComponent: Int = 1) = {
    val msg = new msg_mission_set_current(systemId, componentId)
    msg.seq = seq
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg
  }

  def missionCurrent(seq: Int) = {
    val msg = new msg_mission_current(systemId, componentId)
    msg.seq = seq
    msg
  }

  def makeVFRHud(airspeed: Float, groundspeed: Float, throttlePct: Int, heading: Int) = {
    val msg = new msg_vfr_hud(systemId, componentId)

    msg.airspeed = airspeed
    msg.groundspeed = groundspeed
    msg.throttle = throttlePct
    msg.heading = heading
    msg
  }

  def makeAttitude(pitch: Float, yaw: Float, roll: Float) = {
    val msg = new msg_attitude(systemId, componentId)

    msg.pitch = pitch
    msg.yaw = yaw
    msg.roll = roll
    msg
  }

  /**
   * * Here's what mission planner sends when you choose go-to some point at 100m alt:
   * MAVLINK_MSG_ID_MISSION_ITEM :   param1=0.0  param2=0.0  param3=0.0  param4=0.0  x=37.52122  y=-122.31037  z=100.0  seq=0  command=16  target_system=1  target_component=1  frame=3  current=2  autocontinue=0
   * The device responds with: INFO  c.g.mavlink.LogIncomingMavlink      : Rcv1: MAVLINK_MSG_ID_MISSION_ACK :   target_system=255  target_component=190  type=0
   *
   */
  def missionItem(seq: Int, location: Location, current: Int = 0, isRelativeAlt: Boolean = true, targetComponent: Int = 1) = {
    val msg = new msg_mission_item(systemId, componentId)
    msg.seq = seq
    msg.x = location.lat.toFloat
    msg.y = location.lon.toFloat
    msg.z = location.alt.get.toFloat
    msg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT
    msg.frame = if (isRelativeAlt) MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT else MAV_FRAME.MAV_FRAME_GLOBAL
    msg.current = current // Use 2 for guided mode, 3 means alt change only
    msg.target_system = targetSystem
    msg.target_component = targetComponent
    msg.autocontinue = 1 // Default to true
    msg
  }

  /**
   * FIXME - we currently assume dest has a relative altitude
   */
  def makeGuided(dest: Location) = {
    val r = missionItem(0, dest, current = 2)
    r
  }

  /**
   * lat & lng in degrees
   * alt in meters MSL (we will compute relative_alt / agl)
   * velocities in m/s
   * heading in degrees
   */
  def makePosition(l: Location) = {
    /* Per https://pixhawk.ethz.ch/mavlink/#GLOBAL_POSITION_INT
 * time_boot_ms uint32_t  Timestamp (milliseconds since system boot)
lat int32_t Latitude, expressed as * 1E7
lon int32_t Longitude, expressed as * 1E7
alt int32_t Altitude in meters, expressed as * 1000 (millimeters), above MSL
relative_alt  int32_t Altitude above ground in meters, expressed as * 1000 (millimeters)
vx  int16_t Ground X Speed (Latitude), expressed as m/s * 100
vy  int16_t Ground Y Speed (Longitude), expressed as m/s * 100
vz  int16_t Ground Z Speed (Altitude), expressed as m/s * 100
hdg uint16_t  Compass heading in degrees * 100, 0.0..359.99 degrees. If unknown, set to: 65535
 */
    val msg = new msg_global_position_int(systemId, componentId)

    msg.time_boot_ms = System.currentTimeMillis - startTime
    msg.lat = (l.lat * 1e7).toInt
    msg.lon = (l.lon * 1e7).toInt
    msg.alt = (l.alt.get * 1000).toInt
    msg.relative_alt = ((l.alt.get - groundAltitude) * 1000).toInt
    msg.vx = (l.vx.getOrElse(0.0) * 100).toInt
    msg.vy = (l.vy.getOrElse(0.0) * 100).toInt
    //msg.vz = (l.vz.getOrElse(0.0) * 100).toInt
    msg.hdg = (l.dir.getOrElse(655.35) * 100).toInt

    msg
  }

  def makeGPSRaw(l: Location) = {
    /* Type  Description
time_usec uint64_t  Timestamp (microseconds since UNIX epoch or microseconds since system boot)
fix_type  uint8_t 0-1: no fix, 2: 2D fix, 3: 3D fix. Some applications will not use the value of this field unless it is at least two, so always correctly fill in the fix.
lat int32_t Latitude in 1E7 degrees
lon int32_t Longitude in 1E7 degrees
alt int32_t Altitude in 1E3 meters (millimeters) above MSL
eph uint16_t  GPS HDOP horizontal dilution of position in cm (m*100). If unknown, set to: 65535
epv uint16_t  GPS VDOP horizontal dilution of position in cm (m*100). If unknown, set to: 65535
vel uint16_t  GPS ground speed (m/s * 100). If unknown, set to: 65535
cog uint16_t  Course over ground (NOT heading, but direction of movement) in degrees * 100, 0.0..359.99 degrees. If unknown, set to: 65535
satellites_visible  uint8_t Number of satellites visible. If unknown, set to 255 */
    val msg = new msg_gps_raw_int(systemId, componentId)

    msg.time_usec = (System.currentTimeMillis - startTime) * 1000
    msg.lat = (l.lat * 1e7).toInt
    msg.lon = (l.lon * 1e7).toInt
    msg.alt = (l.alt.get * 1000).toInt
    msg.eph = 65535
    msg.epv = 65535
    msg.vel = 65535
    msg.cog = 65535
    msg.satellites_visible = 255
    msg.fix_type = 3
    msg
  }

  def makeStatusText(message: String) = {
    val msg = new msg_statustext(systemId, componentId)

    msg.setText(message)
    msg.severity = MAV_SEVERITY.MAV_SEVERITY_ALERT // Only alert severity shows on the GUI
    msg
  }

  def makeSysStatus() = {
    val msg = new msg_sys_status(systemId, componentId)

    msg.onboard_control_sensors_present = 1 << 5 // GPS
    msg.onboard_control_sensors_enabled = 1 << 5
    msg.onboard_control_sensors_health = 1 << 5
    msg.battery_remaining = -1
    msg
  }

}

object VehicleSimulator {
  /**
   * The sysId andro/posix pilot uses when sending packets
   */
  val andropilotId = 253

  def decodePosition(m: msg_global_position_int): Location =
    Location(m.lat / 1e7, m.lon / 1e7, Some(m.alt / 1000.0))

  def decodePosition(m: msg_gps_raw_int): Option[Location] =
    if (m.fix_type >= 2) {
      val alt = if (m.fix_type >= 3) Some(m.alt / 1000.0) else None
      Some(Location(m.lat / 1e7, m.lon / 1e7, alt))
    } else None
}
