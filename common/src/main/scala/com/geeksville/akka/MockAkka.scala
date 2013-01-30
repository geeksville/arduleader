package com.geeksville.akka

import com.geeksville.logback.Logger
import com.geeksville.logback.Logging
import scala.collection.mutable.HashSet

object MockAkka extends Logging {

  // The system event stream
  val eventStream = new EventStream
  var scheduler = new Scheduler

  private val actors = HashSet[InstrumentedActor]()

  def actorOf[T <: InstrumentedActor](r: T, name: String = "unspecified") = {
    actors.add(r)
    r.start()
    r
  }

  def shutdown() {
    logger.info("Shutting down actors")
    actors.foreach { a =>
      logger.debug("Killing " + a)
      a ! PoisonPill
    }
    actors.clear()
    scheduler.close()
    // Make a new scheduler since we just torched the current one
    scheduler = new Scheduler
    logger.info("Done shutting down")
  }
}