package com.geeksville.akka

import akka.actor.{ Actor, DeadLetter, Props }
import akka.actor.ActorLogging
import akka.actor.UnhandledMessage

class EventStreamDebugger extends Actor with ActorLogging {
  context.system.eventStream.subscribe(self, classOf[DeadLetter])
  context.system.eventStream.subscribe(self, classOf[UnhandledMessage])

  def receive = {
    case d: DeadLetter => log.error(s"DeadLetter: $d")
    case d: UnhandledMessage => log.error(s"UnhandledMessage: $d")
  }
}
