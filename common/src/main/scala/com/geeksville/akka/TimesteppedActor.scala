package com.geeksville.akka

import akka.actor.Actor
import akka.actor.PoisonPill
import scala.concurrent.duration._

/**
 * This is an Actor mixin that runs doNextStep periodically
 */
trait TimesteppedActor extends Actor {
  import context._

  private case object SimNext

  def numPoints: Int
  def interval: Double

  protected var numRemaining = numPoints

  /// What step are we currently on?
  def currentStep = numPoints - numRemaining

  private def scheduleNext() = context.system.scheduler.scheduleOnce(interval seconds, self, SimNext)

  // Start our sim
  scheduleNext()

  protected def doNextStep(): Unit

  abstract override def receive = ({
    case SimNext =>
      if (numRemaining == 0)
        self ! PoisonPill
      else {
        doNextStep()

        numRemaining -= 1
        scheduleNext()
      }
  }: PartialFunction[Any, Unit]).orElse(super.receive)
}

