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

import HttpConstants._

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
