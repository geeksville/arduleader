package com.geeksville.flight.wingman

import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.flight.VehicleSimulator
import org.mavlink.messages.ardupilotmega.msg_global_position_int
import com.geeksville.flight.FlightLead
import com.geeksville.flight.Location
import org.mavlink.messages.ardupilotmega.msg_mission_ack
import org.mavlink.messages.MAV_MISSION_RESULT
import com.geeksville.util.Counted
import com.geeksville.util.MathTools._
import com.geeksville.flight.HeartbeatSender
import com.geeksville.flight.EventBusVehicleReceiver
import com.geeksville.mavlink.EventBusVehicleSender

/**
 * A full description of how to get from p1 to p2
 *
 * x and z are in meters.  Positive z means the leader is above us.
 * bearing is in degrees.
 */
case class Distance3D(x: Double, z: Double, bearing: Int)

class Wingman extends InstrumentedActor with VehicleSimulator with HeartbeatSender with EventBusVehicleSender {
  override def systemId = Wingman.systemId

  /**
   * How far behind the leader do we want to follow (50 means 50 meters behind lead)
   */
  var desiredDistanceX = 50

  /**
   * How far below the leader do we want to be (50 means 50 meters below lead)
   */
  var desiredDistanceZ = 0

  /**
   * What bearing do we want to the leader (clockwise - 0 means he's at our 12 o'clock, 90 deg is 3 o'clock)
   */
  var desiredBearing = 90

  var leadLoc: Option[Location] = None
  var ourLoc: Option[Location] = None
  /**
   * Who are we following?
   */
  val leaderId = FlightLead.systemId

  private val throttle = new Counted(10)

  MavlinkEventBus.subscribe(self, leaderId)

  // So we can see acks
  MavlinkEventBus.subscribe(self, targetSystem)

  /**
   * Distance & bearing to our lead craft, or None
   */
  def distanceToLeader = for {
    us <- ourLoc;
    lead <- leadLoc
  } yield {
    Distance3D(
      distance(us.lat, us.lon, lead.lat, lead.lon),
      lead.alt.get - us.alt.get,
      bearing(us.lat, us.lon, lead.lat, lead.lon))
  }

  def onReceive = {
    case msg: msg_global_position_int ⇒
      if (msg.sysId == leaderId) {
        // The lead plane moved

        // log.debug("WRx" + msg.sysId + ": " + msg)
        leadLoc = Some(VehicleSimulator.decodePosition(msg))
        updateGoal()
      } else if (msg.sysId == targetSystem) {
        // We have a new update of our position

        ourLoc = Some(VehicleSimulator.decodePosition(msg))
        // updateGoal() // Not needed here, generates 2x too many updates: goal depends only on the lead aircraft position (currently)
      }

    case msg: msg_mission_ack =>
      if (msg.target_system == systemId) {
        if (msg.`type` != MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED)
          log.error("wp refused: " + msg)

        // log.debug("ack rcvd: " + msg)
      }

    //case msg => log.debug("Ignoring: " + msg);
  }

  /**
   * Get the desired position for our target plane
   * (FIXME - not tested yet)
   */
  def desiredLoc = leadLoc.map { l =>
    val (lat, lon) = applyBearing(l.lat, l.lon, desiredDistanceX, desiredBearing - 180)
    Location(lat, lon, Some(l.alt.get - desiredDistanceZ))
  }

  /**
   * Send a new dest waypoint to the target
   */
  def updateGoal() {
    // Resolve opts, if they are missing we can't really do anything
    for {
      // dist <- distanceToLeader;
      l <- desiredLoc
    } yield {
      // Tell the plane we are controlling the new goal
      sendMavlink(missionItem(0, l, current = 2, isRelativeAlt = false))

      // Generate fake position updates for our systemId, so the 'goal' can be seen in QGroundControl
      sendMavlink(makePosition(l))
      sendMavlink(makeGPSRaw(l))

      throttle { i =>
        log.info("Wingman has sent %d waypoints".format(i))
        // log.debug("Distance: " + dist)

        // Update status
        sendMavlink(makeStatusText("Wingman..."))
        sendMavlink(makeSysStatus())
      }
    }
  }
}

object Wingman {
  /**
   * The ID we use when sending our messages
   */
  val systemId = 254
}
