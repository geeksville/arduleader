package com.geeksville.flight

// Standard akka imports
import scala.concurrent.duration._
import com.geeksville.akka.InstrumentedActor
import java.io.InputStream
import com.geeksville.akka.Cancellable

/**
 * Reads a IGC file, and publishes Location on the event bus
 */
class IGCPublisher(stream: InputStream) extends InstrumentedActor {
  val igc = new IGCReader(stream)

  /**
   * For now we pipe all our notifications through the system event stream - we might refine this later
   */
  val destEventBus = context.system.eventStream

  private var scheduled: Seq[Cancellable] = Seq()

  scheduleEvents()

  def onReceive = {
    case x: Location =>
      // Do the broadcast now
      log.info("doing loc publish")
      destEventBus.publish(x)
      log.info("did loc publish")
  }

  override def postStop() {
    log.debug("Removing scheduled IGCs")
    scheduled.foreach(_.cancel)
    super.postStop()
  }

  /**
   * Convert every location into a scheduled message (hopefully the scheduler is smart)
   */
  private def scheduleEvents() {
    if (igc.locations.length > 1) { // Need at least two points, because it seems like the first point is bogus in some tracklogs
      val points = igc.locations.tail
      val startTime = points(0).time

      scheduled = points.map { l =>
        log.debug("Schedule: " + l)
        context.system.scheduler.scheduleOnce((l.time - startTime) milliseconds, self, l)
      }
      log.info("Done scheduling " + points.size + " points")
    }
  }
}