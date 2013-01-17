package com.geeksville.akka

import scala.concurrent.duration.Duration
import scala.actors.Actor
import scala.actors.TIMEOUT
import com.geeksville.logback.Logger
import com.geeksville.logback.Logging

case class Cancellable(actor: Actor) {
  def cancel() {
    actor ! PoisonPill
  }
}

class Scheduler extends Logging {
  def scheduleOnce(d: Duration, dest: InstrumentedActor, cb: => Unit) = {

    def runOnce {
      Actor.reactWithin(d.toMillis) {
        case TIMEOUT =>
          cb
        case PoisonPill =>
      }
    }
    Cancellable(Actor.actor(runOnce))
  }

  def schedule(initial: Duration, next: Duration)(cb: => Unit) = {
    val initialMs = initial.toMillis
    val nextMs = next.toMillis

    logger.warn("FIXME, ignoring initialMs")
    def fixedRateLoop {
      Actor.reactWithin(nextMs) {
        case TIMEOUT =>
          cb; fixedRateLoop
        case PoisonPill =>
      }
    }
    Cancellable(Actor.actor(fixedRateLoop))
  }
}