package com.geeksville.andropilot.service

import android.content.Context
import android.hardware.usb._
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import java.io._
import com.hoho.android.usbserial.driver.UsbSerialDriver
import scala.concurrent.SyncVar
import com.geeksville.aserial.AsyncSerial
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.geeksville.andropilot.AccessGrantedReceiver

class NoAcquirePortException extends Exception

trait AndroidSerial {
  def out: OutputStream
  def in: InputStream
}

class USBAndroidSerial(rawDevice: UsbDevice, baudRate: Int)(implicit context: Context) extends AndroidSerial with AndroidLogger {
  //Get UsbManager from Android.
  info("Looking for USB service")
  val manager = context.getSystemService(Context.USB_SERVICE).asInstanceOf[UsbManager]

  // FIXME - eventually allowed delayed creation?
  private var driver = new SyncVar[UsbSerialDriver]

  val readTimeout = 1000
  val writeTimeout = 1000

  private lazy val async = driver.get(10000).map { d =>
    val toSkip = if (d.isInstanceOf[FtdiSerialDriver]) 2 else 0
    new AsyncSerial(d, toSkip)
  } // Give enough time for the port to open at startup

  open(rawDevice)

  val in = new InputStream {

    private var closed = false

    private var rxBuf: Option[AsyncSerial#Request] = None

    debug("Opening serial input stream")

    override def available = if (driver.isSet) 1 else 0

    override def read = {
      val arr = new Array[Byte](1)

      val r = read(arr)
      if (r < 0)
        r
      else
        arr(0).toInt & 0xff
    }

    /**
     * Refill our buffer
     */
    private def fillBuffer() {
      rxBuf.foreach(_.clearAndStart())
      do {
        // The ftdi driver can return zero if it received a serial packet but the length
        // count was zero
        // FIXME - this is super inefficient - because we will spin the CPU hard, need to fix
        // android-usb-serial
        rxBuf = async.map(_.readBuffer(readTimeout)).getOrElse(throw new EOFException("Port not open"))
      } while (!rxBuf.isDefined && !closed)

      /*
      rxBuf.foreach { r =>
        val bytes = (r.buffer.array.map { x => "%02x".format(x) }.mkString(","))
        debug("Bytes pos=%2d,limit=%2d: %s".format(r.buffer.position, r.buffer.limit, bytes))
      }
      */
    }

    override def read(arr: Array[Byte], off: Int, numrequested: Int) = {
      assert(off == 0)
      assert(numrequested <= arr.size)

      var destoff = off
      var numremaining = numrequested

      /** move X bytes from our buffer to the result, updating invariants */
      def extract(numBytes: Int) {
        if (numBytes > 0)
          rxBuf.get.buffer.get(arr, destoff, numBytes)
        destoff += numBytes
        numremaining -= numBytes
      }

      var resultcode = 0
      while (numremaining > 0 && resultcode >= 0) {
        val available = rxBuf.map(_.buffer.remaining).getOrElse(0)

        resultcode = if (closed)
          -1
        else if (numremaining <= available) {
          // We have all the needed bytes in our buffer
          extract(numremaining)
          0 // Claim success
        } else {
          // User wants more than we have - give them what we got then start a new read
          extract(available)
          fillBuffer()
          0 // try again
        }
      }

      // debug("Bytes read: " + (arr.toSeq.map { x => "%02x".format(x) }.mkString(",")))
      if (resultcode < 0)
        resultcode
      else
        numrequested
    }

    override def close() {
      if (!closed) {
        debug("Closing serial input stream")
        closed = true
        USBAndroidSerial.this.close()
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
      //debug("Writing: " + b.take(len).mkString(","))
      async.foreach(_.write(b, writeTimeout))
    }
  }

  private def open(rawDevice: UsbDevice) {

    info("Acquiring")
    val d = UsbSerialProber.acquire(manager, rawDevice)

    if (d == null)
      throw new NoAcquirePortException

    info("Opening port")
    d.open()

    //disconnectReceiver.register()

    Thread.sleep(200) // Give USB device some time to settle before setting params

    try {
      d.setParameters(baudRate, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE)
    } catch {
      case ex: IOException =>
        error("Second attempt to set parameters")
        d.setParameters(baudRate, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE)
    }

    // on apm just turn on rts/dtr because that's what linux does...
    if (AndroidSerial.isAPM(rawDevice) || AndroidSerial.isPX4(rawDevice)) {
      d.setRTS(true)
      d.setDTR(true)
    } else
      d.setFlowControl(UsbSerialDriver.FLOWCONTROL_RTSCTS)

    info("Port open")
    driver.put(d)
  }

  def close() {
    // disconnectReceiver.unregister()
    getDriverNoWait.foreach { d =>
      debug("closing serial driver")
      async.foreach(_.close())
      d.close()
      driver.take() // Discard our driver reference
      debug("done closing serial driver")
    }
  }

  def getDriverNoWait = driver.get(0)
}

object AndroidSerial extends AndroidLogger {

  def isFtdiDevice(dvr: UsbDevice) = dvr.getVendorId == 0x0403 &&
    (dvr.getProductId == 0x6001 || dvr.getProductId == 0x6015 ||
      dvr.getProductId == 0x03ea || dvr.getProductId == 0x03eb)

  // cp2102 devices show up as ACM devices
  def isTelemetry(dvr: UsbDevice) = isFtdiDevice(dvr) || dvr.getVendorId == 0x10C4
  def isAPM(dvr: UsbDevice) = dvr.getVendorId == 0x2341 && dvr.getProductId == 0x0010
  def isPX4(dvr: UsbDevice) = dvr.getVendorId == 0x26ac && dvr.getProductId == 0x0011

  def getDevices(implicit context: Context) = {
    val manager = context.getSystemService(Context.USB_SERVICE).asInstanceOf[UsbManager]
    val devices = manager.getDeviceList.asScala.values
    info("Connected devices: " + devices.map { dvr =>
      "%s:vend=%04x:id=%04x".format(dvr.getDeviceName, dvr.getVendorId, dvr.getProductId)
    }.mkString(","))

    val filtered = devices.filter { dvr => isTelemetry(dvr) || isAPM(dvr) || isPX4(dvr) }
    filtered
  }

  def requestAccess(device: UsbDevice, success: UsbDevice => Unit, failure: UsbDevice => Unit)(implicit context: Context) = {
    info("Requesting access")
    val r = new AccessGrantedReceiver(device, success, failure)
    r.requestPermission()
    r
  }
}
