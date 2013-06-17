package com.geeksville.andropilot.service

import android.content.Context
import java.io._
import com.geeksville.mavlink.MavlinkStream
import com.geeksville.flight.VehicleModel
import com.geeksville.aserial.AsyncSerial
import com.geeksville.flight.VehicleClient
import com.ridemission.scandroid.AndroidLogger
import com.geeksville.andropilot.FlurryClient

object MavlinkAndroid extends AndroidLogger {
  val useNativeFtdi = true

  def create(baudRate: Int)(implicit context: Context) = {
    // Is the device ftdi based?  If so, we have the option of using their native library
    val isFtdiDevice = AndroidSerial.getDevice.map { d => AndroidSerial.isTelemetry(d) }.getOrElse(false)

    val port = if (useNativeFtdi && isFtdiDevice)
      try {
        new FTDISerial(baudRate)
      } catch {
        case ex: Exception =>
          error("FTDI failed: " + ex)
          //usageEvent("ftdi_failed", "message" -> ex.getMessage)
          // Fall back to old version
          new USBAndroidSerial(baudRate)
      }
    else
      new USBAndroidSerial(baudRate)

    // Actually buffering output is bad and unneeded, because the only place we do a write is a single call in MavlinkStream and that call is careful to
    // write all bytes in one go.

    // It is better to leave the output buffer as shallow as possible so we have the option to wrap packets with SendOnce().  If a packet is wrapped inside
    // a SendOnce instance we will scour the MavlinkStream actor incoming message queue for dups and send _only_ the freshest packet with that ID.
    // This allows us to be sure things like joystick set rc-chanel events never get bunched up.

    //val out = new BufferedOutputStream(port.out, 512) // we buffer so a single flush can be used to squirt out an entire packet
    // val out = port.out // For now no buffering

    // Buffer reads a little, so the dumb byte reads in MAVLinkReader don't kill us
    // One FTDI packet is 62 bytes of payload + 2 bytes control
    // FIXME - buffering doesn't work yet with my serial reader
    //val instream = new BufferedInputStream(port.in, 64)
    val instream = port.in

    VehicleClient.isUsbBusted = !port.isInstanceOf[FTDISerial] && AsyncSerial.isUsbBusted

    new MavlinkStream(port.out, instream)
  }
}