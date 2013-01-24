package com.geeksville.akka

import scala.concurrent.duration.Duration
import scala.actors.Actor
import scala.actors.TIMEOUT
import com.geeksville.logback.Logger
import com.geeksville.logback.Logging
import com.geeksville.util.ThreadTools._
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledFuture

case class Cancellable(f: ScheduledFuture[_]) {
  def cancel() {
    f.cancel(false)
  }
}

class Scheduler extends Logging {
  val jscheduler = Executors.newScheduledThreadPool(1)

  def scheduleOnce(d: Duration, dest: InstrumentedActor, msg: Any) = {

    val msecs = d.toMillis

    def cb() {
      logger.info("handle once")
      dest ! msg
    }
    val r = jscheduler.schedule(cb _, d.toMillis, TimeUnit.MILLISECONDS)

    Cancellable(r)
  }

  def schedule(initial: Duration, next: Duration)(cb: => Unit) = {

    val r = jscheduler.scheduleWithFixedDelay(cb _, initial.toMillis, next.toMillis, TimeUnit.MILLISECONDS)

    Cancellable(r)
  }
}