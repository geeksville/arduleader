package com.geeksville.andropilot

import android.content.Context
import java.io._
import com.geeksville.mavlink.MavlinkStream
import com.geeksville.util.ByteOnlyInputStream
import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.VehicleMonitor
import com.geeksville.aserial.AsyncSerial

object MavlinkAndroid {
  def create(baudRate: Int)(implicit context: Context) = {
    val port = new AndroidSerial(baudRate)
    val out = new BufferedOutputStream(port.out, 512) // we buffer so a single flush can be used to squirt out an entire packet
    // val out = port.out // For now no buffering

    // Buffer reads a little, so the dumb byte reads in MAVLinkReader don't kill us
    // One FTDI packet is 62 bytes of payload + 2 bytes control
    // FIXME - buffering doesn't work yet with my serial reader
    //val instream = new BufferedInputStream(port.in, 64)
    val instream = port.in

    VehicleMonitor.isUsbBusted = AsyncSerial.isUsbBusted

    new MavlinkStream(out, instream)
  }
}