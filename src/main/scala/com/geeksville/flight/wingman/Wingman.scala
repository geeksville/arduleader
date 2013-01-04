package com.geeksville.flight.wingman

import akka.actor.Actor
import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.flight.lead.VehicleSimulator
import org.mavlink.messages.ardupilotmega.msg_global_position_int
import com.geeksville.flight.lead.FlightLead

class Wingman extends InstrumentedActor with VehicleSimulator {
  override def systemId = Wingman.systemId

  MavlinkEventBus.subscribe(self, FlightLead.systemId)

  // So we can see acks
  MavlinkEventBus.subscribe(self, Wingman.targetSystemId)

  def receive = {
    // We only care about position messages
    case msg: msg_global_position_int ⇒
      log.info("WRx" + msg.sysId + ": " + msg)
    //case msg => log.debug("Ignoring: " + msg);
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