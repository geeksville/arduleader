package com.geeksville.andropilot

import android.content.Context
import java.io._
import com.geeksville.mavlink.MavlinkStream
import com.geeksville.util.ByteOnlyInputStream
import com.geeksville.akka.InstrumentedActor

object MavlinkAndroid {
  def create(baudRate: Int)(implicit context: Context): (Unit => InstrumentedActor) = {
    val port = new AndroidSerial(baudRate)
    val out = new BufferedOutputStream(port.out, 8192)

    // Buffer reads a little, so the dumb byte reads in MAVLinkReader don't kill us
    // One FTDI packet is 62 bytes of payload + 2 bytes control
    val instream = new BufferedInputStream(port.in, 32)
    (Unit => new MavlinkStream(out, instream))
  }
}