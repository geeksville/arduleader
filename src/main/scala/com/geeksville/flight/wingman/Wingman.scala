package com.geeksville.flight.wingman

import akka.actor.Actor
import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.MavlinkReceived
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.flight.lead.VehicleSimulator
import org.mavlink.messages.ardupilotmega.msg_global_position_int

class Wingman extends InstrumentedActor {
  MavlinkEventBus.subscribe(self, VehicleSimulator.systemId)

  def receive = {
    // We only care about position messages
    case MavlinkReceived(msg: msg_global_position_int) ⇒
      log.info("WRx" + msg.sysId + ": " + msg)
    case MavlinkReceived(msg) =>
      // log.debug("Ignoring: " + msg)
      ;
  }
}

object Wingman {
  /**
   * The plane we should control
   */
  val targetSystemId = 1
}