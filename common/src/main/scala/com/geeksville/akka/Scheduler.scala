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
  def scheduleOnce(d: Duration, dest: InstrumentedActor, msg: Any) = {

    val msecs = d.toMillis

    def runOnce = {
      //logger.info("Waiting " + msecs)

      Actor.reactWithin(msecs) {
        case TIMEOUT =>
          //logger.info("handle once")
          dest ! msg
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
          //logger.info("handle fixed rate")
          cb; fixedRateLoop
        case PoisonPill =>
      }
    }
    Cancellable(Actor.actor(fixedRateLoop))
  }
}