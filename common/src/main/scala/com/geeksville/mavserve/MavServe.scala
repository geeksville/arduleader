package com.geeksville.mavserve

import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.VehicleModel
import com.geeksville.flight.Location

/**
 * A web server that serves up vehicle data (and static html/js/art content) to a web browser.  Intended to be a starting point for
 * a server meant for Mavelous.
 *
 * This server currently delivers data from the following HTTP GETable locations:
 *
 * /static (contains static content - i.e. FIXME, put your Mavelous files here)
 * /parameters (a json object with param names and values)
 * /vehicle (a json object with location/orientation data - intended to be repeatedly fetched by Mavelous)
 */
class MavServe(val v: VehicleModel) extends InstrumentedActor {

  val subscription = v.eventStream.subscribe(this)

  log.info("Starting MavServe")
  // FIXME launch web server

  override def postStop() {
    v.eventStream.removeSubscription(subscription)
    super.postStop()
  }

  override def onReceive = {
    case l: Location =>
      // FIXME This is just an example on how to receive one of the various messages published by VehicleModel.  Eventually you might want to
      // push new content to the client (via a comet connection, EventStream or WebSocket).  But you don't need to bother with this at first - just
      // poll the server fetching new data periodically
      log.debug("Received a location update: " + l)
  }

}