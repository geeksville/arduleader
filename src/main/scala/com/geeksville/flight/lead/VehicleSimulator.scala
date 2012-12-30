package com.geeksville.flight.lead

import com.geeksville.flight._
import akka.actor.Props
import com.geeksville.mavlink.MavlinkSender
import akka.actor._
import org.mavlink.messages.ardupilotmega._
import org.mavlink.messages._

/**
 * Pretend to be a vehicle, generating mavlink messages for our system id.
 *
 */
trait VehicleSimulator {
  import VehicleSimulator._

  val componentId = 0 // FIXME

  var groundAltitude = 0.0

  val startTime = System.currentTimeMillis

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
    msg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_INVALID
    msg.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_AUTO_ENABLED
    msg.custom_mode = 0
    msg.system_status = MAV_STATE.MAV_STATE_ACTIVE
    msg.mavlink_version = 3 // Seems to be what ardupilot uses

    msg
  }

  /**
   * lat & lng in degrees
   * alt in meters MSL (we will compute relative_alt / agl)
   * velocities in m/s
   * heading in degrees
   */
  def makePosition(lat: Double, lon: Double, alt: Double, vx: Double = 0, vy: Double = 0, vz: Double = 0, hdg: Double = 655.35) = {
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
    msg.lat = (lat * 1e7).toInt
    msg.lon = (lon * 1e7).toInt
    msg.alt = (alt * 1000).toInt
    msg.relative_alt = ((alt - groundAltitude) * 1000).toInt
    msg.vx = (vx * 100).toInt
    msg.vy = (vy * 100).toInt
    msg.vz = (vz * 100).toInt
    msg.hdg = (hdg * 100).toInt

    msg
  }
}

object VehicleSimulator {
  /**
   * We use a systemId 2, because the ardupilot is normally on 1.
   */
  val systemId: Int = 2
}