package com.geeksville.gcsapi

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
 * This exposes the GCS scripting API via a conventional REST web server
 */
class Webserver(val root: SmallAPI) extends InstrumentedActor {

  /**
   * Handles REST gets of vehicle params
   */
  val getterHandler = new GETHandler("/api/(.*)".r) {

    override protected def handleRequest(req: Request) = {
      // FIXME- use something like the following if you want to parse html path
      // use the following as the argument to the superclass constructor: "/vdata/gethtml/(.*)".r
      // (This makes a regex with one portion getting pulled out).  Then in the function you can reference
      // the matches in the regex as follows:
      val pattern = req.matches(0)
      val r = root.get(pattern)

      new SimpleResponse(r)
    }
  }

  val server = startWebServer()

  override def postStop() {
    server.close()
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
    log.info("Starting Webserver on http://localhost:4404")

    val server = new MicroRESTServer(4404)
    // FIXME - we currently assume the cwd is the default of 'posixpilot'
    server.addHandler(new FileHandler("/static", new File("../httpcontent")))
    server.addHandler(getterHandler)
    server
  }
}