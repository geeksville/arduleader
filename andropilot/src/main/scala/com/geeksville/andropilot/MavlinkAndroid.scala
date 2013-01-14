package com.geeksville.andropilot

import android.content.Context
import java.io._
import com.geeksville.mavlink.MavlinkStream
import com.geeksville.util.ByteOnlyInputStream
import akka.actor.Actor

object MavlinkAndroid {
  def create(baudRate: Int)(implicit context: Context): (Unit => Actor) = {
    val port = new AndroidSerial(baudRate)
    val out = new BufferedOutputStream(port.out, 8192)
    val instream = new ByteOnlyInputStream(port.in)
    (Unit => new MavlinkStream(out, instream))
  }
}