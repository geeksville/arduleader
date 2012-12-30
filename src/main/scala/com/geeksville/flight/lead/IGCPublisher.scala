package com.geeksville.flight.lead

// Standard akka imports
import akka.actor._
import akka.util.Duration
import akka.util.duration._
import com.geeksville.akka.InstrumentedActor

/**
 * Reads a IGC file, and publishes Location on the event bus
 */
class IGCPublisher(filename: String) extends InstrumentedActor {
  val igc = new IGCReader(filename)

  /**
   * For now we pipe all our notifications through the system event stream - we might refine this later
   */
  val destEventBus = context.system.eventStream

  scheduleEvents()

  def receive = {
    case x: Location =>
      // Do the broadcast now
      destEventBus.publish(x)
  }

  /**
   * Convert every location into a scheduled message (hopefully the scheduler is smart)
   */
  private def scheduleEvents() {
    if (!igc.locations.isEmpty) {
      val startTime = igc.locations(0).time

      // FIXME - for testing limit to ten points!!!!!
      igc.locations.take(10).foreach { l =>
        log.debug("Schedule: " + l)
        context.system.scheduler.scheduleOnce((l.time - startTime) milliseconds, self, l)
      }
      log.debug("Done scheduling")
    }
  }
}