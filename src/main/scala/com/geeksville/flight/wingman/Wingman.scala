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

class Wingman extends InstrumentedActor with VehicleSimulator {
  override def systemId = Wingman.systemId

  var leadLoc: Option[Location] = None

  /**
   * Who are we following?
   */
  val leaderId = FlightLead.systemId

  private val throttle = new Counted(10)

  MavlinkEventBus.subscribe(self, leaderId)

  // So we can see acks
  MavlinkEventBus.subscribe(self, Wingman.targetSystemId)

  def receive = {
    // We only care about position messages from the plane we are following
    case msg: msg_global_position_int ⇒
      if (msg.sysId == leaderId) {
        //log.debug("WRx" + msg.sysId + ": " + msg)
        val l = decodePosition(msg)
        leadLoc = Some(l)
        updateTarget()
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
  def targetLoc = leadLoc

  /**
   * Send a new dest waypoint to the target
   */
  def updateTarget() {
    targetLoc.foreach { l =>
      sendMavlink(makeMissionItem(l.lat.toFloat, l.lon.toFloat, l.alt.toFloat))

      throttle { i =>
        log.info("Wingman has sent %d waypoints".format(i))
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