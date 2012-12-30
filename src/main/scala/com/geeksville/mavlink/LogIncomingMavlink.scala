package com.geeksville.mavlink

import akka.actor.Actor
import com.geeksville.akka.InstrumentedActor

class LogIncomingMavlink(sysId: Int) extends InstrumentedActor {
  MavlinkEventBus.subscribe(self, sysId)

  def receive = {
    case MavlinkReceived(msg) ⇒
      log.info("Rcv" + msg.sysId + ": " + msg)
  }
}

