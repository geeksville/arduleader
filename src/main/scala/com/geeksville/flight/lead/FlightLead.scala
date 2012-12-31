package com.geeksville.flight.lead

// Standard akka imports
import akka.actor._
import akka.util.Duration
import akka.util.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.wingman.Wingman
import com.geeksville.util.Counted

/**
 * Listen for GPS Locations on the event bus, and drive our simulated vehicle
 */
class FlightLead extends InstrumentedActor with VehicleSimulator {

  val mavlink: ActorRef = context.system.actorOf(Props[MavlinkSender])

  private val throttle = new Counted(10)

  // Send a heartbeat every 10 seconds 
  context.system.scheduler.schedule(0 milliseconds, 1 seconds, mavlink, heartbeat)

  context.system.eventStream.subscribe(self, classOf[Location])

  def receive = {
    case Location(lat, logg, alt, time) =>
      mavlink ! makePosition(lat, logg, alt)
      mavlink ! makeGPSRaw(lat, logg, alt)
      throttle { i =>
        val msg = "Emitted %d points...".format(i)
        log.info(msg)
        mavlink ! makeStatusText(msg)

        mavlink ! makeSysStatus()
      }
  }
}

object FlightLead {
  def main(args: Array[String]) {
    println("FlightLead running...")

    // FIXME create this somewhere else
    Akka.actorOf(Props[MavlinkReceiver], "mavrx")

    // Create flightlead actors
    // Akka.actorOf(Props(new LogIncomingMavlink(VehicleSimulator.systemId)), "hglog")
    Akka.actorOf(Props[FlightLead], "lead")
    Akka.actorOf(Props(new IGCPublisher("testdata/pretty-good-res-dumps-1hr.igc")), "igcpub")

    // Create wingman actors

    Akka.actorOf(Props[Wingman], "wing")

    // Include this if you want to see all traffic from the ardupilot (very verbose)
    // Akka.actorOf(Props(new LogIncomingMavlink(Wingman.targetSystemId)), "ardlog")

    // to see GroundControl packets
    Akka.actorOf(Props(new LogIncomingMavlink(255)), "gclog")

    Thread.sleep(1000 * 60 * 10)
  }
}