package com.geeksville.mavlink

import akka.actor.Actor

class LogIncomingMavlink extends Actor {
  context.system.eventStream.subscribe(self, classOf[MavlinkReceived])

  def receive = {
    case MavlinkReceived(msg) ⇒
      println("Rcv" + msg.sysId + ": " + msg)
  }
}

