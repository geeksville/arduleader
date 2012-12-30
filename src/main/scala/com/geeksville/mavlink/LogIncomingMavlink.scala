package com.geeksville.mavlink

import akka.actor.Actor
import com.geeksville.akka.InstrumentedActor

class LogIncomingMavlink extends InstrumentedActor {
  context.system.eventStream.subscribe(self, classOf[MavlinkReceived])

  def receive = {
    case MavlinkReceived(msg) ⇒
      log.info("Rcv" + msg.sysId + ": " + msg)
  }
}

