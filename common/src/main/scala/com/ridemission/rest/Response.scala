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
import Using._

import HttpConstants._

trait Response {
  def contentType: String
  def statusMessage = "200 OK"

  def headers = Seq(
    // "Connection" -> "KeepAlive",
    "Connection" -> "close",
    "Accept" -> "multipart/form-data",
    "Accept-Encoding" -> "multipart/form-data",
    "Server" -> "MicroRESTServer",
    "cache-control" -> "no-store",
    // Sander just pointed this out: https://developer.mozilla.org/en/HTTP_access_control#section_6
    // Allows hip to do the js queries from anywhere...
    "Access-Control-Allow-Origin" -> "*",
    "Content-Type" -> contentType)

  /// Send this response to the client (only for use by the RESTServer) and possibly close
  /// the connection
  def send(connection: Socket)

  protected def sendHeaders(respStream: PrintWriter) {
    respStream.print("HTTP/1.1 " + statusMessage + "\r\n")
    headers.foreach { case (h, v) => respStream.printf("%s: %s\r\n", h, v) }
    respStream.print("\r\n")
  }
}

/// A non streaming response
case class SimpleResponse(val contentType: String, response: String) extends Response {

  override def headers = super.headers :+ "Content-Length" -> response.length.toString

  def this(response: String) = this(contentTypeText, response)
  def this(response: JValue) = this(contentTypeJson, response.asJSON)

  def send(connection: Socket) {
    using(connection) { connection =>
      using(new PrintWriter(connection.getOutputStream)) { respStream =>

        super.sendHeaders(respStream)

        respStream.print(response)
      }
    }
  }
}

/// A streaming response
/// @param outf a callback that will write data to the client, the callee should _not_ close
/// this stream.
case class StreamResponse(val contentType: String)(val outf: OutputStream => Unit) extends Response {
  override def headers = super.headers :+ "Transfer-Encoding" -> "chunked"

  /// The adapter that allows the users' callback to write to our client with 
  /// a series of chunks
  /// UGH - this would normally be an anonymous class, but Android doesn't
  /// like that
  private class ChunkStream(val raw: OutputStream, val respStream: PrintWriter) extends OutputStream {

    // val out = new ByteBuffer.allocateDirect(8192)
    val CRLF = "\r\n".map(_.toByte).toArray

    val hexDigits = "0123456789abcdef".map(_.toByte).toArray

    val maxDigits = 8
    val hexout = new Array[Byte](maxDigits)

    // Print hex very efficiently (to avoid allocs)
    def printHex(nIn: Int) {
      /*
        assert(nIn >= 0) // This code won't yet work for -1

        var n = nIn
        for (i <- 0 until maxDigits) {
          val wloc = maxDigits - 1 - i
          hexout(wloc) = hexDigits(n & 0x0f)
          n = n >> 4

          if (n == 0) {
            raw.write(hexout, wloc, i + 1)
            printf("%x => %s\n", hexout.map(_.toChar).mkString.substring(wloc, i + 1))
            return
          }
        }
	*/
      respStream.print(Integer.toHexString(nIn))
    }

    private def printHeader(len: Int) {
      printHex(len)
      // raw.write(CRLF)
      respStream.print("\r\n")
      respStream.flush()
    }

    private def printTrailer() {
      respStream.print("\r\n")
      respStream.flush()
      // raw.write(CRLF)
      // raw.flush()
    }

    def print(str: String) {
      printHeader(str.length)
      // raw.write(str.map(_.toByte).toArray)
      respStream.print(str)
      printTrailer()
    }

    override def write(b: Int) {
      printHeader(1)
      raw.write(b)
      printTrailer()
    }

    override def write(b: Array[Byte]) {
      if (b.length != 0) {
        printHeader(b.length)
        raw.write(b)
        printTrailer()
      }
    }

    override def write(b: Array[Byte], off: Int, count: Int) {
      if (count != 0) {
        printHeader(count)
        raw.write(b, off, count)
        printTrailer()
      }
    }

    override def close() {
      // We ignore the close, instead, once the callee returns we will
      // call closeConnection
      println("Ignoring close request")
    }

    def closeConnection() {
      //println("Done sending stream")
      printHeader(0)
      printTrailer() // End of stream

      respStream.close()
    }
  }

  def send(connection: Socket) {
    val raw = connection.getOutputStream

    // Too expensive to use in the output stream
    val respStream = new PrintWriter(raw)
    super.sendHeaders(respStream)
    respStream.flush()

    val outStream = new ChunkStream(raw, respStream)
    try {
      outf(outStream)
    } catch {
      case ex: Exception =>
        println("Exception in callback " + ex)
        ex.printStackTrace()
        outStream.print(ex.toString)
    } finally {
      outStream.closeConnection()
      connection.close()
    }
  }
}

class ErrorResponse(val errorCode: Int, val errorString: String, humanMessage: String)
  extends SimpleResponse(humanMessage) {

  /// i.e. 404 NOT FOUND
  override def statusMessage = "%d %s".format(errorCode, errorString)
}

