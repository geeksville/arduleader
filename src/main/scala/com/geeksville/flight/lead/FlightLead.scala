package com.geeksville.flight.lead

// Standard akka imports
import akka.actor._
import akka.util.Duration
import akka.util.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor

/**
 * Listen for GPS Locations on the event bus, and drive our simulated vehicle
 */
class FlightLead extends InstrumentedActor with VehicleSimulator {

  val mavlink: ActorRef = context.system.actorOf(Props[MavlinkSender])

  // Send a heartbeat every 10 seconds 
  context.system.scheduler.schedule(0 milliseconds, 10 seconds, mavlink, heartbeat)

  context.system.eventStream.subscribe(self, classOf[Location])

  def receive = {
    case Location(lat, log, alt, time) =>
      mavlink ! makePosition(lat, log, alt)
  }
}

object FlightLead {
  def main(args: Array[String]) {
    println("FlightLead running...")

    // FIXME create this somewhere else
    Akka.actorOf(Props[MavlinkReceiver], "mavrx")
    Akka.actorOf(Props[LogIncomingMavlink], "mavlog")
    Akka.actorOf(Props[FlightLead], "lead")
    Akka.actorOf(Props(new IGCPublisher("testdata/pretty-good-res-dumps-1hr.igc")), "igcpub")

    Thread.sleep(1000 * 60 * 10)
  }
}