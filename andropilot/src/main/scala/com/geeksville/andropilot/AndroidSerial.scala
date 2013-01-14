package com.geeksville.andropilot;

import android.content.Context
import android.hardware.usb._
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import java.io._
import com.hoho.android.usbserial.driver.UsbSerialDriver
import scala.concurrent.SyncVar

class AndroidSerial(baudRate: Int)(implicit context: Context) extends AndroidLogger {
  //Get UsbManager from Android.
  info("Looking for USB service")
  val manager = context.getSystemService(Context.USB_SERVICE).asInstanceOf[UsbManager]

  // FIXME - eventually allowed delayed creation?
  private var driver = new SyncVar[UsbSerialDriver]

  open()

  val readTimeout = 1000 * 1000 // FIXME
  val writeTimeout = 1000

  val in = new InputStream {

    override def available = if (driver.isSet) 1 else 0

    override def read = {
      val arr = new Array[Byte](1)

      val r = read(arr)
      if (r < 0)
        r
      else
        arr(0).toInt & 0xff
    }

    override def read(arr: Array[Byte], off: Int, len: Int) = {
      assert(off == 0)
      assert(len <= arr.size)
      debug("Calling read " + len)
      val r = driver.get.read(arr, readTimeout)
      if (r >= 0)
        debug("Bytes read: " + arr.mkString(","))
      r
    }

    override def close() {
      AndroidSerial.this.close()
    }
  }

  val out = new OutputStream {

    override def write(b: Int) = {
      val arr = Array[Byte](b.toByte)
      write(arr)
    }

    override def write(b: Array[Byte], off: Int, len: Int) = {
      debug("Writing: " + b.take(len).mkString(","))
      driver.get.write(b, writeTimeout)
    }
  }

  private def open() {

    info("Acquiring")
    val rawDevice = AndroidSerial.getDevice // We assume we already have access
    val d = UsbSerialProber.acquire(manager, rawDevice)

    info("Opening port")
    d.open()
    d.setBaudRate(baudRate)
    info("Port open")
    driver.put(d)
  }

  def close() {
    driver.get.close
  }
}

object AndroidSerial extends AndroidLogger {
  def getDevice(implicit context: Context) = {
    val manager = context.getSystemService(Context.USB_SERVICE).asInstanceOf[UsbManager]
    val devices = manager.getDeviceList.asScala.values
    info("Connected devices: " + devices.map { dvr =>
      "%s:vend=%04x:id=%04x".format(dvr.getDeviceName, dvr.getVendorId, dvr.getProductId)
    }.mkString(","))

    val filtered = devices.filter { dvr => dvr.getVendorId == 0x0403 && dvr.getProductId == 0x6001 }
    if (filtered.size == 0)
      throw new IOException("Port not found")
    else if (filtered.size != 1)
      throw new IOException("FIXME, multiple devices attached - not yet supported")

    filtered.head
  }

  def requestAccess(success: UsbDevice => Unit, failure: UsbDevice => Unit)(implicit context: Context) = {

    val device = getDevice

    info("Requesting access")
    new AccessGrantedReceiver(device, success, failure)
  }
}