package com.geeksville.flight.lead

import com.geeksville.flight._
import akka.actor.Props
import akka.actor._
import akka.util.duration._
import org.mavlink.messages.ardupilotmega._
import org.mavlink.messages._
import java.util.GregorianCalendar
import com.geeksville.mavlink.MavlinkEventBus

/**
 * Pretend to be a vehicle, generating mavlink messages for our system id.
 *
 */
trait VehicleSimulator { self: Actor =>

  val componentId = 1 // FIXME

  var groundAltitude = 0.0

  /**
   * We want to use a 'system boot time' but for now I'll just pick something that is relatively recent (so 32 bit msecs don't roll over)
   */
  val startTime = (new GregorianCalendar(2012, 12, 30)).getTime.getTime

  /**
   * Our heartbeat message
   */
  val heartbeat = {
    /*
     * type  uint8_t Type of the MAV (quadrotor, helicopter, etc., up to 15 types, defined in MAV_TYPE ENUM)
autopilot uint8_t Autopilot type / class. defined in MAV_AUTOPILOT ENUM
base_mode uint8_t System mode bitfield, see MAV_MODE_FLAGS ENUM in mavlink/include/mavlink_types.h
custom_mode uint32_t  A bitfield for use for autopilot-specific flags.
system_status uint8_t System status flag, see MAV_STATE ENUM
mavlink_version uint8_t_mavlink_version MAVLink version, not writable by user, gets added by protocol because of magic data type: uint8_t_mavlink_version
* */
    val msg = new msg_heartbeat(systemId, componentId)

    msg.`type` = MAV_TYPE.MAV_TYPE_FLAPPING_WING // Close enough... ;-)
    msg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC
    msg.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_AUTO_ENABLED
    msg.custom_mode = 0
    msg.system_status = MAV_STATE.MAV_STATE_ACTIVE
    msg.mavlink_version = 3 // Seems to be what ardupilot uses

    msg
  }

  // Send a heartbeat every 10 seconds 
  context.system.scheduler.schedule(0 milliseconds, 1 seconds) {
    sendMavlink(heartbeat)
  }

  def systemId: Int

  def sendMavlink(m: MAVLinkMessage) = MavlinkEventBus.publish(m)

  def decodePosition(m: msg_global_position_int) =
    Location(m.lat / 1e7, m.lon / 1e7, m.alt / 1000.0)

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
    msg.alt = (l.alt * 1000).toInt
    msg.relative_alt = ((l.alt - groundAltitude) * 1000).toInt
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
    msg.alt = (l.alt * 1000).toInt
    msg.eph = 65535
    msg.epv = 65535
    msg.vel = 65535
    msg.cog = 65535
    msg.satellites_visible = 255
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

  def makeMissionItem(lat: Float, lon: Float, alt: Float, targetSys: Int) = {
    val msg = new msg_mission_item(systemId, componentId)

    msg.target_system = targetSys
    msg.target_component = 1
    msg.param3 = 0 // FIXME - see if we can control loiter direction this way...
    msg.x = lon
    msg.y = lat
    msg.z = alt
    msg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT
    msg.frame = 3 // FIXME
    msg.current = 2 // FIXME
    msg.autocontinue = 0 // FIXME

    msg
  }
}

