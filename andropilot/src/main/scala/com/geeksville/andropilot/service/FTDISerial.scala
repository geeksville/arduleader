package com.geeksville.andropilot.service

import android.content.Context
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import java.io._
import com.ftdi.j2xx.D2xxManager
import com.ftdi.j2xx.FT_Device

class FTDISerial(baudRate: Int)(implicit context: Context) extends AndroidSerial with AndroidLogger {

  private var devOpt: Option[FT_Device] = None

  private def closed = !devOpt.isDefined

  val in = new InputStream {
    debug("Opening FTDI input stream")

    override def available = {
      val r = devOpt.map(_.getQueueStatus).getOrElse(0)
      if (r >= 0)
        r
      else
        0
    }

    override def read = {
      val arr = new Array[Byte](1)

      val r = read(arr)
      if (r < 0)
        r
      else
        arr(0).toInt & 0xff
    }

    override def read(arr: Array[Byte], off: Int, numrequested: Int) = {
      assert(off == 0)

      val timeoutMsec = 1000
      val r = devOpt.map(_.read(arr, numrequested, timeoutMsec)).getOrElse(0)
      debug("Read returns " + r)
      r
    }

    override def close() {
      if (!closed) {
        debug("Closing FTDI input stream")
        FTDISerial.this.close()
        super.close()
        debug("Done closing serial input stream")
      }
    }
  }

  val out = new OutputStream {

    override def write(b: Int) = {
      val arr = Array[Byte](b.toByte)
      write(arr)
    }

    override def write(b: Array[Byte], off: Int, len: Int) = {
      debug("Writing: " + b.take(len).mkString(","))
      // async.foreach(_.write(b, writeTimeout))
      assert(off == 0)

      // We wait for writes to complete, so if we get backed up we can make a decision on handling on a packet by packet basis
      devOpt.foreach(_.write(b, len, true))
    }
  }

  open()

  private def open() {
    info("Opening FTDI device")
    val d2xx = D2xxManager.getInstance(context)

    val numDev = d2xx.createDeviceInfoList(context);
    if (numDev < 1)
      throw new IOException("No FTDI devices")

    val dev = d2xx.openByIndex(context, 0)

    if (dev == null)
      throw new IOException("FTDI open failed")

    dev.setBitMode(0.toByte, D2xxManager.FT_BITMODE_RESET)
    dev.setBaudRate(baudRate)
    dev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
      D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE)
    dev.setFlowControl(D2xxManager.FT_FLOW_NONE, 0.toByte, 0.toByte)

    // Bunch up reads into 32msec buckets
    dev.setLatencyTimer(32.toByte)

    dev.purge((D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX).toByte)
    devOpt = Some(dev)
    info("Done opening FTDI")
  }

  def close() {
    devOpt.foreach(_.close())
    devOpt = None
  }
}

