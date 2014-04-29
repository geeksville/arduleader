package com.geeksville.akka

import akka.actor.{ Actor, DeadLetter, Props }
import akka.actor.ActorLogging
import akka.actor.UnhandledMessage
import org.mavlink.messages.MAVLinkMessage

class EventStreamDebugger extends Actor with ActorLogging with DebuggableActor {
  private val messagesToIgnore = List(classOf[MAVLinkMessage])

  context.system.eventStream.subscribe(self, classOf[DeadLetter])
  context.system.eventStream.subscribe(self, classOf[UnhandledMessage])

  def receive = {
    case d: DeadLetter => log.error(s"DeadLetter: $d")
    case d: UnhandledMessage =>
      // There are a few message types we expect to be ignored by most
      d.message match {
        case _: MAVLinkMessage =>
        case GetDebugInfo =>
        case _ =>
          log.error(s"UnhandledMessage: $d")
      }
  }
}
