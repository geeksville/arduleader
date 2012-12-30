package com.geeksville.flight.lead

import com.geeksville.flight._
import akka.actor.Props
import com.geeksville.mavlink.MavlinkSender
import akka.actor.ActorSystem
import org.mavlink.messages.ardupilotmega.msg_global_position_int

/**
 * Pretend to be a vehicle, generating mavlink messages for our system id
 */
class VehicleSimulator(val systemId: Int = 1)(implicit system: ActorSystem) {

  val sender = system.actorOf(Props[MavlinkSender])

  val componentId = 0 // FIXME

  var groundAltitude = 0

  /**
   * lat & lng in degrees
   * alt in meters MSL (we will compute relative_alt / agl)
   * velocities in m/s
   * heading in degrees
   */
  def sendPosition(lat: Double, lon: Double, alt: Double, vx: Double, vy: Double, vz: Double, hdg: Double = 655.35) {
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

    msg.lat = (lat * 1e7).toInt
    msg.lon = (lon * 1e7).toInt
    msg.alt = (alt * 1000).toInt
    msg.relative_alt = ((alt - groundAltitude) * 1000).toInt
    msg.vx = (vx * 100).toInt
    msg.vy = (vy * 100).toInt
    msg.vz = (vz * 100).toInt
    msg.hdg = (hdg * 100).toInt

    sender ! msg
  }
}