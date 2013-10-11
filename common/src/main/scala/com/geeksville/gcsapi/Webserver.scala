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
import com.ridemission.rest.POSTHandler

/**
 * This exposes the GCS scripting API via a conventional REST web server
 */
class Webserver(val root: SmallAPI, localonly: Boolean = true) extends InstrumentedActor {

  val baseUrl = "/api/"
  val readRegex = (baseUrl + "(.*)").r

  // Second param is the name of the method to call
  val callRegex = (baseUrl + "(.*)/(.*)").r

  /**
   * Handles REST gets of vehicle params.
   * FIXME - this is just syntactic REST sugar for calling the obj._get method?
   */
  val getterHandler = new GETHandler(readRegex) {

    override protected def handleRequest(req: Request) = {
      println("In get handler")

      // FIXME- use something like the following if you want to parse html path
      // use the following as the argument to the superclass constructor: "/vdata/gethtml/(.*)".r
      // (This makes a regex with one portion getting pulled out).  Then in the function you can reference
      // the matches in the regex as follows:
      val p = req.matches(0)
      val r = root.get(p)
      new SimpleResponse(r)
    }
  }

  /**
   * Handles REST gets of vehicle params
   */
  val postHandler = new POSTHandler(callRegex) {

    override protected def handleRequest(req: Request) = {
      val obj = req.matches(0)
      val method = req.matches(1)

      val args = req.payloadAsJson.asInstanceOf[Seq[_]]
      println("JSON arguments into post: " + args.mkString(","))
      val r = root.call(obj, method, args)
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

    val server = new MicroRESTServer(4404, localonly)
    server.addHandler(getterHandler)
    server.addHandler(postHandler)
    server
  }
}