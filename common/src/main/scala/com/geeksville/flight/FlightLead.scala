package com.geeksville.flight

// Standard akka imports
import akka.actor._
import scala.concurrent.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.util.Counted
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.logback.Logging

/**
 * Listen for GPS Locations on the event bus, and drive our simulated vehicle
 */
class FlightLead extends InstrumentedActor with VehicleSimulator {

  private val throttle = new Counted(10)

  override def systemId = FlightLead.systemId

  context.system.eventStream.subscribe(self, classOf[Location])

  def receive = {
    case l: Location =>
      sendMavlink(makePosition(l))
      sendMavlink(makeGPSRaw(l))
      throttle { i =>
        val msg = "Emitted %d points...".format(i)
        log.info(msg)
        sendMavlink(makeStatusText(msg))
        sendMavlink(makeSysStatus())
      }
  }
}
object FlightLead {

  /**
   * We use a systemId 2, because the ardupilot is normally on 1.
   */
  val systemId: Int = 2
}