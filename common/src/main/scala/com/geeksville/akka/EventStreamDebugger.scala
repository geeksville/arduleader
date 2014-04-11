package com.geeksville.akka

import akka.actor.{ Actor, DeadLetter, Props }
import akka.actor.ActorLogging
import akka.actor.UnhandledMessage
import org.mavlink.messages.MAVLinkMessage

class EventStreamDebugger extends Actor with ActorLogging {
  private val messagesToIgnore = List(classOf[MAVLinkMessage])

  context.system.eventStream.subscribe(self, classOf[DeadLetter])
  context.system.eventStream.subscribe(self, classOf[UnhandledMessage])

  def receive = {
    case d: DeadLetter => log.error(s"DeadLetter: $d")
    case d: UnhandledMessage =>
      val isIgnore = messagesToIgnore.find { c =>
        c.isInstance(d.message)
      }.isDefined
      if (!isIgnore)
        log.error(s"UnhandledMessage: $d")
  }
}
