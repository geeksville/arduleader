package com.geeksville.akka

import com.geeksville.logback.Logger
import com.geeksville.logback.Logging
import scala.collection.mutable.HashSet

object MockAkka extends Logging {

  // The system event stream
  val eventStream = new EventStream
  val scheduler = new Scheduler

  private val actors = HashSet[InstrumentedActor]()

  def actorOf(generator: => InstrumentedActor, name: String = "unspecified") = {
    val r = generator
    actors.add(r)
    r.start()
    r
  }

  def shutdown() {
    logger.info("Shutting down actors")
    actors.foreach(_ ! PoisonPill)
    logger.info("Done shutting down")
  }
}