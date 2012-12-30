package com.geeksville.flight.lead

// Standard akka imports
import akka.actor._
import akka.util.Duration
import akka.util.duration._

/**
 * Listen for GPS Locations on the event bus, and drive our simulated vehicle
 */
class FlightLead extends VehicleSimulator {

  def receive = {
    case Location(lat, log, alt, time) =>
      sendPosition(lat, log, alt)
  }
}