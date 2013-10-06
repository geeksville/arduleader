package com.geeksville.mavserve

import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.VehicleModel
import com.geeksville.flight.Location
import com.ridemission.rest.MicroRESTServer
import com.ridemission.rest.FileHandler
import java.io.File
import com.ridemission.rest.GETHandler
import com.ridemission.rest.Request
import com.ridemission.rest.SimpleResponse
import com.ridemission.rest.HttpConstants
import com.ridemission.rest.JsonTools
import com.ridemission.rest.JObject
import com.ridemission.rest.JArray

/**
 * A (demo) web server that serves up vehicle data (and static html/js/art content) to a web browser.  Intended to be a starting point for
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

  /**
   * Handles REST gets of vehicle params
   */
  val paramHandler = new GETHandler("/parameters".r) {

    override protected def handleRequest(req: Request) = {
      // FIXME- use something like the following if you want to parse html path
      // use the following as the argument to the superclass constructor: "/vdata/gethtml/(.*)".r
      // (This makes a regex with one portion getting pulled out).  Then in the function you can reference
      // the matches in the regex as follows:
      // val pattern = req.matches(0)

      // Note: I use flatMap here so that I can automatically collapse out any elements that are missing (the for comprehension will return None in that case)
      val params = v.parameters.flatMap { p =>
        for { id <- p.getId; pval <- p.getValue } yield { id -> pval }
      }

      new SimpleResponse(JObject(params: _*))
    }
  }

  /**
   * Handles REST gets of vehicle state
   */
  val vehicleHandler = new GETHandler("/vehicle".r) {

    override protected def handleRequest(req: Request) = {

      val vinfo = Seq(
        v.location.map { l => "loc" -> JArray(l.lat, l.lon, l.alt) },
        v.attitude.map { l => "attitude" -> JArray(l.pitch, l.yaw, l.roll) }).flatten

      new SimpleResponse(JObject(vinfo: _*))
    }
  }

  val server = startWebServer()

  override def postStop() {
    server.close()
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

  private def startWebServer() = {
    log.info("Starting MavServe")

    val server = new MicroRESTServer(4404)
    // FIXME - we currently assume the cwd is the default of 'posixpilot'
    server.addHandler(new FileHandler("/static", new File("../httpcontent")))
    server.addHandler(paramHandler)
    server.addHandler(vehicleHandler)
    server
  }
}