package com.geeksville.mavlink

import akka.actor.Actor
import com.geeksville.flight._

class LogIncomingMavlink extends Actor {
  Akka.eventStream.subscribe(self, classOf[MavlinkReceived])

  def receive = {
    case MavlinkReceived(msg) ⇒ println("received: " + msg)
  }
}

