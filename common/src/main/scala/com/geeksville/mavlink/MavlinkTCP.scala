package com.geeksville.mavlink

import java.net._
import com.geeksville.util.ThreadTools
import com.geeksville.akka.InstrumentedActor
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

object MavlinkTCP {

  /**
   * Initiates a connection to TCP server
   */
  def connect(destHostName: String, destPortNumber: Int) = {

    // These must be lazy - to ensure we don't do networking in the main thread (an android restriction)
    lazy val socket = {
      val r = new Socket(destHostName, destPortNumber)

      r.setReceiveBufferSize(32768)
      r.setTcpNoDelay(true)
      r
    }
    lazy val in = {
      val s = socket.getInputStream
      assert(s != null)
      new BufferedInputStream(s, 512)
    }
    lazy val out = {
      val s = socket.getOutputStream
      assert(s != null)
      new BufferedOutputStream(s, 512)
    }

    new MavlinkStream(out, in)
  }

}
