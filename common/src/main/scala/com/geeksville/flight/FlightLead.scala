package com.geeksville.flight

// Standard akka imports
import scala.concurrent.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.util.Counted
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.logback.Logging
import com.geeksville.akka.MockAkka

/**
 * Listen for GPS Locations on the event bus, and drive our simulated vehicle
 */
class FlightLead(sysId: Int = FlightLead.systemId) extends VehicleSimulator with HeartbeatSender {

  private val throttle = new Counted(10)

  override def systemId = sysId

  MockAkka.eventStream.subscribe(self, { e: Any => e.isInstanceOf[Location] })

  def onReceive = {
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