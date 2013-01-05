package com.geeksville.flight.wingman

import akka.actor.Actor
import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.flight.lead.VehicleSimulator
import org.mavlink.messages.ardupilotmega.msg_global_position_int
import com.geeksville.flight.lead.FlightLead
import com.geeksville.flight.lead.Location
import org.mavlink.messages.ardupilotmega.msg_mission_ack
import org.mavlink.messages.MAV_MISSION_RESULT
import com.geeksville.util.Counted
import com.geeksville.util.MathTools._

/**
 * A full description of how to get from p1 to p2
 *
 * x and z are in meters.  Positive z means the leader is above us.
 * bearing is in degrees.
 */
case class Distance3D(x: Double, z: Double, bearing: Int)

class Wingman extends InstrumentedActor with VehicleSimulator {
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
  var desiredBearing = 0

  var leadLoc: Option[Location] = None
  var ourLoc: Option[Location] = None
  /**
   * Who are we following?
   */
  val leaderId = FlightLead.systemId

  private val throttle = new Counted(10)

  MavlinkEventBus.subscribe(self, leaderId)

  // So we can see acks
  MavlinkEventBus.subscribe(self, Wingman.targetSystemId)

  /**
   * Distance & bearing to our lead craft, or None
   */
  def distanceToLeader = for {
    us <- ourLoc;
    lead <- leadLoc
  } yield {
    Distance3D(
      distance(us.lat, us.lon, lead.lat, lead.lon),
      lead.alt - us.alt,
      bearing(us.lat, us.lon, lead.lat, lead.lon))
  }

  def receive = {
    // We only care about position messages from the plane we are following
    case msg: msg_global_position_int ⇒
      if (msg.sysId == leaderId) {
        //log.debug("WRx" + msg.sysId + ": " + msg)
        leadLoc = Some(decodePosition(msg))
        updateGoal()
      } else if (msg.sysId == Wingman.targetSystemId) {
        ourLoc = Some(decodePosition(msg))
        updateGoal() // FIXME, should we send this less often? (not when either lead or our position changes?)
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
   * (FIXME - not correct yet)
   */
  def desiredLoc = leadLoc

  /**
   * Send a new dest waypoint to the target
   */
  def updateGoal() {
    // Resolve opts, if we are missing we can't really do anything
    for {
      dist <- distanceToLeader;
      l <- desiredLoc
    } yield {
      sendMavlink(makeMissionItem(l.lat.toFloat, l.lon.toFloat, l.alt.toFloat))

      throttle { i =>
        log.info("Wingman has sent %d waypoints".format(i))
        log.debug("Distance: " + dist)
      }
    }
  }
}

object Wingman {
  /**
   * The ID we use when sending our messages
   */
  val systemId = 254

  /**
   * The plane we should control
   */
  val targetSystemId = 1
}