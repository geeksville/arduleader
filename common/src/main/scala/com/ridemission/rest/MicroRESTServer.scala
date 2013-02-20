/**
 * Copyright 2010 Mission Motors, Inc.
 * Kindly released under the Apache Source License (http://www.apache.org/licenses/LICENSE-2.0.html) on Feb 19th, 2013
 */
package com.ridemission.rest

import scala.actors._
import scala.util.matching._
import scala.io._
import scala.collection.mutable.ListBuffer

import java.io._
import java.net._
import java.util.concurrent._

import com.geeksville.util._
import com.geeksville.util.ThreadTools._
import Using._

object HttpConstants {
  val utf = "; charset=utf-8"

  val contentTypeText = "text/plain" + utf
  val contentTypeHtml = "text/html" + utf
  val contentTypeJson = "text/json" + utf
  val contentTypeBinary = "application/octet-stream"

  val extensionToMime = Map(
    "html" -> contentTypeHtml,
    "txt" -> contentTypeText,
    "png" -> "image/png",
    "js" -> ("application/javascript" + utf),
    "ttf" -> "font/truetype",
    "otf" -> "font/opentype",
    "css" -> ("text/css" + utf),
    "xml" -> ("application/xml" + utf),
    "xpi" -> "application/x-xpinstall")
}

import HttpConstants._

object Method extends Enumeration {
  type Method = Value

  val GET = Value("GET")
  val POST = Value("POST")
  val PUT = Value("PUT")
}
import Method._

case class Request(connection: Socket, request: URI, matches: List[String],
  method: Method, payloadStream: InputStream,
  parameters: Map[String, String]) {

  lazy val payload = new UnbufferedStreamSource(payloadStream)
}

/// All content for this web server is provided by subclasses of this class.
/// Note: I considered using an Actor for this, but we actually want a number of simultaneously
/// executing RESTHandlers - which is kinda the opposite of Actors
abstract class RESTHandler(val pathRegex: Regex, val method: Method) {

  /// @return true if this handler will match against the provided path
  def canHandle(matches: List[String]) = true

  def replyToRequest(req: Request) {
    val response = handleRequest(req)
    response.send(req.connection)
  }

  /// You must provide this method, given a request return a response
  protected def handleRequest(req: Request): Response
}

/// A handler for GET requests
abstract class GETHandler(pathRegex: Regex) extends RESTHandler(pathRegex, GET)

/// Subclasses should implement handleRequest and read req.payload
abstract class PUTHandler(pathRegex: Regex) extends RESTHandler(pathRegex, PUT)

abstract class POSTHandler(pathRegex: Regex) extends RESTHandler(pathRegex, POST) {
  protected def handlePost(req: Request, params: Map[String, String]): Response

  override protected def handleRequest(req: Request): Response = {
    val str = req.payload.mkString
    val parms = MicroRESTServer.parseParams(str)
    handlePost(req, parms)
  }
}

/// A very small REST/web server.  Create and register handlers by calling addHandler
class MicroRESTServer(portNum: Int) {
  val queueLen = 10
  private val serverSocket = new ServerSocket(portNum, queueLen)
  private val listenerThread = ThreadTools.start("RESTServe")(readerFunct)
  private val workers = Executors.newFixedThreadPool(8)

  private val handlers = new ListBuffer[RESTHandler]

  // Http req looks like GET /fish HTTP/1.1
  private val ReqRegex = "(.+) (.+) (.+)".r
  // headers look like Foo-Blah: somevalue
  private val HeaderRegex = "(.+): (.+)".r

  /// Add a handler which is responsible for a certain URL path (regex)
  /// Note: newer handlers are searched before older handlers
  def addHandler(handler: RESTHandler) {
    handlers.prepend(handler)
  }

  /// Shut down this server
  def close() {
    serverSocket.close() // Should cause the thread to die
  }

  /// Handle an incoming connection
  private def handleConnection(client: Socket) {
    var firstLine = ""
    try {
      client.setKeepAlive(true)

      val reqStream = client.getInputStream
      val reqLines = new UnbufferedStreamSource(reqStream).getLines
      firstLine = reqLines.next.trim
      println("Request: " + firstLine)
      // Read headers till blank line
      val headerMap = Map(reqLines.takeWhile(_.length != 0).toSeq.map { line =>
        val HeaderRegex(k, v) = line.trim
        k -> v
      }: _*)
      // print(headerMap.mkString("Request headers:\n  ", "\n  ", "\n"))

      val ReqRegex(methodStr, req, httpVer) = firstLine
      var reqURI = new URI(req)
      val reqPath = reqURI.getPath

      // Loop until we find a handler and call it
      val handler = handlers.find { handler =>
        val matches = handler.pathRegex.unapplySeq(reqPath)
        if (matches.isDefined && handler.canHandle(matches.get)) {
          val method = Method.withName(methodStr)
          val queryStr = reqURI.getQuery
          val params = MicroRESTServer.parseParams(queryStr)

          // Construct a source with the right # of bytes
          val contentLength = headerMap.getOrElse("Content-Length", "-1").toInt
          val headStream = if (contentLength != -1)
            new HeadInputStream(reqStream, contentLength)
          else
            reqStream
          val req = Request(client, reqURI, matches.get, method, headStream, params)
          handler.replyToRequest(req)

          true // We just handled things
        } else
          false
      }
      if (!handler.isDefined) {
        val notFound = new ErrorResponse(404, "NOT FOUND",
          "%s did not match any resource".format(reqPath))

        println("ERROR could not find: " + reqPath)
        notFound.send(client)
      }
    } catch {
      case ex: MatchError =>
        println("bad client request: " + firstLine)
        client.close()

      case ex: Exception =>
        // Normally the worker/response takes care of closing the connection but something went badly and we
        // don't want leaks
        client.close()
        println("Abandoning connection due to: " + ex)
        ex.printStackTrace
    }
  }

  private def readerFunct() {
    try {
      while (true) {
        val socket = serverSocket.accept()

        val runnable = new Runnable {
          def run() {
            handleConnection(socket)
          }
        }

        // The runnable will take care of sending the reply
        workers.execute(runnable)
      }
    } catch {
      case x: SocketException =>
        println("REST socket has been closed, exiting receiver...")
    }
  }
}

object MicroRESTServer {

  /// For testing
  def main(args: Array[String]) {
    val server = new MicroRESTServer(4404)
    server.addHandler(new FileHandler("/tmp", new File("/tmp")))
  }

  def decode(url: String) = URLDecoder.decode(url, "UTF-8")

  /// Split query params into a Map 
  def parseParams(queryStr: String): Map[String, String] =
    if (queryStr == null)
      Map[String, String]()
    else
      Map(queryStr.split('&').map { pair =>
        val a = pair.split('=')
        (decode(a(0)), decode(a(1)))
      }: _*)

}