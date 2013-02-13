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
import java.util.concurrent.ScheduledThreadPoolExecutor

case class Cancellable(f: ScheduledFuture[_]) {
  def cancel() {
    f.cancel(false)
  }
}

class Scheduler extends Logging {
  val jscheduler = new ScheduledThreadPoolExecutor(1)

  jscheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false)

  def close() = {
    val wasRunning = jscheduler.shutdownNow()
    jscheduler.awaitTermination(500, TimeUnit.MILLISECONDS)
    logger.error("Done shutting down scheduler numTasks=" + wasRunning.size)
  }

  def scheduleOnce(d: Duration, dest: InstrumentedActor, msg: Any) = {

    val msecs = d.toMillis

    def cb = new Runnable {
      def run() {
        // logger.info("handle once")
        dest ! msg
      }
    }
    //logger.debug("scheduling once " + msecs + " msg " + msg)
    val r = jscheduler.schedule(cb, msecs, TimeUnit.MILLISECONDS)
    //logger.debug("Measured delay: " + r.getDelay(TimeUnit.MILLISECONDS))

    Cancellable(r)
  }

  def schedule(initial: Duration, next: Duration)(cb: => Unit) = {

    logger.info("scheduling")
    val r = jscheduler.scheduleWithFixedDelay(cb _, initial.toMillis, next.toMillis, TimeUnit.MILLISECONDS)

    Cancellable(r)
  }
}