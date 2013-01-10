package com.geeksville.akka

import akka.actor.Actor
import akka.actor.ActorLogging

/**
 * Mixin to add a number of debugging utilities to actors
 */
trait InstrumentedActor extends Actor with ActorLogging {
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(self + " restarted due to message=%s, exception=%s".format(message.getOrElse("unspecified"), reason))
    log.debug("stack trace: " + reason.getStackTrace.mkString("\n"))
    super.preRestart(reason, message)
  }

  /**
   * A nice readable name for this actor (removing bad symbols)
   */
  override def toString = "%s(%s)".format(getClass.getSimpleName, self.path)
}