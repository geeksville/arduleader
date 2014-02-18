/**
 * Copyright 2010 Mission Motors, Inc.
 * Kindly released under the Apache Source License (http://www.apache.org/licenses/LICENSE-2.0.html) on Feb 19th, 2013
 */
package com.ridemission.rest

import scala.util.matching._
import scala.io._
import scala.collection.mutable.ListBuffer

import java.io._
import java.net._
import java.util.concurrent._

import com.geeksville.util._
import com.geeksville.util.ThreadTools._
import Using._

import HttpConstants._

import Method._

/// A very small REST/web server.  Create and register handlers by calling addHandler
class MicroRESTServer(portNum: Int, val localOnly: Boolean = true) {
  val queueLen = 10
  private val serverSocket = new ServerSocket(portNum, queueLen)
  private val listenerThread = ThreadTools.createDaemon("RESTServe")(readerFunct)
  private val workers = Executors.newFixedThreadPool(8)

  private val handlers = new ListBuffer[RESTHandler]

  // Http req looks like GET /fish HTTP/1.1
  private val ReqRegex = "(.+) (.+) (.+)".r
  // headers look like Foo-Blah: somevalue
  private val HeaderRegex = "(.+):\\s*(.+)".r

  listenerThread.start()

  /// Add a handler which is responsible for a certain URL path (regex)
  /// Note: newer handlers are searched before older handlers
  def addHandler(handler: RESTHandler) {
    assert(handler != null)
    handlers.prepend(handler)
  }

  /// Shut down this server
  def close() {
    println("Shutting down the server")
    serverSocket.close() // Should cause the thread to die
    workers.shutdown()
    println("REST server exited")
  }

  /// Handle an incoming connection
  private def handleConnection(client: Socket) {
    var firstLine = ""
    try {
      val other = client.getRemoteSocketAddress.asInstanceOf[InetSocketAddress]
      println("GCS connection request from " + other.getAddress.getHostAddress)
      if (localOnly && other.getAddress.getHostAddress != "127.0.0.1") {
        println("Refusing remote client")
        (new ErrorResponse(401, "UNAUTHORIZED", "Remote connections refused")).send(client)
      } else {
        client.setKeepAlive(true)

        val reqStream = client.getInputStream
        val reqLines = new UnbufferedStreamSource(reqStream).getLines
        firstLine = reqLines.next.trim
        print("Request: " + firstLine + " ")
        // Read headers till blank line
        val headerMap = Map(reqLines.takeWhile(_.length != 0).toSeq.map { line =>
          //println(s"Considering: $line")
          val HeaderRegex(k, v) = line.trim
          k -> v
        }: _*)
        println(headerMap.mkString("headers: ", ", ", ""))

        val ReqRegex(methodStr, req, httpVer) = firstLine
        var reqURI = new URI(req)
        val reqPath = Option(reqURI.getPath).getOrElse("")

        // Loop until we find a handler and call it
        val handler = handlers.find { h =>
          val matches = h.pathRegex.unapplySeq(reqPath)
          val method = Method.withName(methodStr)
          if (matches.isDefined && h.canHandle(method, matches.get)) {
            val queryStr = reqURI.getQuery
            val params = MicroRESTServer.parseParams(queryStr)

            // Construct a source with the right # of bytes
            val contentLength = headerMap.getOrElse("Content-Length", "-1").toInt
            val headStream = if (contentLength != -1)
              new HeadInputStream(reqStream, contentLength)
            else
              reqStream
            val req = Request(client, reqURI, matches.get, headStream, params)
            h.replyToRequest(req)

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
      }
    } catch {
      case ex: MatchError =>
        println(s"malformed headers for: $firstLine $ex")
        // ex.printStackTrace()
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