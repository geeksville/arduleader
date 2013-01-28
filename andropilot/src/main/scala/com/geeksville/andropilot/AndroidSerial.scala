package com.geeksville.andropilot;

import android.content.Context
import android.hardware.usb._
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import java.io._
import com.hoho.android.usbserial.driver.UsbSerialDriver
import scala.concurrent.SyncVar
import java.nio.ByteBuffer
import android.content.BroadcastReceiver
import android.content.Intent
import android.app.PendingIntent
import android.content.IntentFilter
import scala.language.reflectiveCalls
import com.geeksville.aserial.AsyncSerial
import com.hoho.android.usbserial.driver.FtdiSerialDriver

class NoAcquirePortException extends Exception

class AndroidSerial(baudRate: Int)(implicit context: Context) extends AndroidLogger {
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

  /*
  val disconnectReceiver = new BroadcastReceiver {
    def register() {
      // We now want to close our device if it ever gets removed
      val filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
      context.registerReceiver(this, filter)
    }

    def unregister() {
      context.unregisterReceiver(this)
    }

    override def onReceive(context: Context, intent: Intent) {
      val action = intent.getAction()
      error("USB device disconnected")

      if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
        val device = Option(intent.getParcelableExtra(UsbManager.EXTRA_DEVICE).asInstanceOf[UsbDevice])
        error("Closing port due to disconnection")
        out.close()
        in.close() // This should force readers to barf
      }
    }
  }
 */

  open()

  val in = new InputStream {

    private var closed = false

    // FIXME, the android-usb-serial library is buggy, get my bytebuffer based fix working here first, then
    // prop it into that lib
    private val rxBuf = ByteBuffer.allocate(512) // +2 bytes for ftdi header == preferred ftdi 512 bytes

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
    private def fillBuffer() = {
      rxBuf.clear()

      var r = 0
      do {
        // The ftdi driver can return zero if it received a serial packet but the length
        // count was zero
        // FIXME - this is super inefficient - because we will spin the CPU hard, need to fix
        // android-usb-serial
        r = async.map(_.read(rxBuf.array, readTimeout)).getOrElse(throw new EOFException("Port not open"))
      } while (r == 0 && !closed)

      // If success, update # available bytes
      if (r >= 0)
        rxBuf.limit(r)

      //debug("Fill: " + rxBuf.array.take(r).map { b => "%02x".format(b) }.mkString(","))

      r
    }

    override def read(arr: Array[Byte], off: Int, numrequested: Int) = {
      assert(off == 0)
      assert(numrequested <= arr.size)

      var destoff = off
      var numremaining = numrequested

      /** move X bytes from our buffer to the result, updating invariants */
      def extract(numBytes: Int) {
        if (numBytes > 0)
          rxBuf.get(arr, destoff, numBytes)
        destoff += numBytes
        numremaining -= numBytes
      }

      var resultcode = 0
      while (numremaining > 0 && resultcode >= 0) {
        val available = rxBuf.remaining

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
        }
      }

      //debug("Bytes read: " + (arr.toSeq.map { x => "%02x".format(x) }.mkString(",")))
      if (resultcode < 0)
        resultcode
      else
        numrequested
    }

    override def close() {
      if (!closed) {
        debug("Closing serial input stream")
        closed = true
        AndroidSerial.this.close()
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

  private def open() {

    info("Acquiring")
    val rawDevice = AndroidSerial.getDevice.get // We assume we already have access
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
    try {
      d.setFlowControl(UsbSerialDriver.FLOWCONTROL_RTSCTS)
    } catch {
      case ex: IOException =>
        error(ex.toString) // Not yet implemented for ACM devices
    }
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

  def isTelemetry(dvr: UsbDevice) = dvr.getVendorId == 0x0403 && dvr.getProductId == 0x6001
  def isAPM(dvr: UsbDevice) = dvr.getVendorId == 0x2341 && dvr.getProductId == 0x0010

  def getDevice(implicit context: Context) = {
    val manager = context.getSystemService(Context.USB_SERVICE).asInstanceOf[UsbManager]
    val devices = manager.getDeviceList.asScala.values
    info("Connected devices: " + devices.map { dvr =>
      "%s:vend=%04x:id=%04x".format(dvr.getDeviceName, dvr.getVendorId, dvr.getProductId)
    }.mkString(","))

    val filtered = devices.filter { dvr => isTelemetry(dvr) || isAPM(dvr) }
    if (filtered.size == 0)
      None
    else if (filtered.size != 1)
      throw new IOException("FIXME, multiple devices attached - not yet supported")
    else
      Some(filtered.head)
  }

  def requestAccess(device: UsbDevice, success: UsbDevice => Unit, failure: UsbDevice => Unit)(implicit context: Context) = {
    info("Requesting access")
    val r = new AccessGrantedReceiver(device, success, failure)
    r.requestPermission()
    r
  }
}