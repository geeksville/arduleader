package com.geeksville.aserial

import com.hoho.android.usbserial.driver.UsbSerialDriver
import scala.collection.mutable.ListBuffer
import android.hardware.usb.UsbRequest
import java.nio.ByteBuffer
import com.ridemission.scandroid.AndroidLogger
import java.io.IOException
import scala.collection.mutable.HashSet
import java.util.Arrays
import android.hardware.usb.UsbEndpoint
import android.os.Build

/**
 * I'm currently building upon the USB android serial library, which has a number of problems.
 * Until I get around to merging my changes with that library, I add most of my smarts here.
 *
 * That project is mostly affected by http://b.android.com/28023, but for my case I can work around it.
 */
class AsyncSerial(val dev: UsbSerialDriver, val bytesToSkip: Int = 0) extends AndroidLogger {
  import AsyncSerial._

  // 64 buffers, 64 bytes each gives 72 packets/sec
  // using bigger than 64 byte buffers causes sequence errors (presumably because ftdi framing is being inserted every 64 bytes?)
  val numBuffers = 64
  val bufferSize = 64

  assert(dev.mConnection != null)
  private val connection = dev.mConnection

  private lazy val readBuffers = ListBuffer.fill(numBuffers)(new Request(true))

  private val activeBuffers = HashSet[Request]()

  private val emptyArray = Array.fill(bufferSize)(0xff.toByte)

  class Request(val isRead: Boolean, val len: Int = bufferSize) extends UsbRequest {

    // Write buffers are transient, so it doesn't make sense to use expensive Direct buffers
    val buffer = if (isRead) ByteBuffer.allocateDirect(len) else ByteBuffer.allocate(len)

    /**
     * Clear our buffer and deallocate any relation to the endpoint
     */
    def clear() {
      close() // Take back from USB ownership

      if (isUsbBusted) {
        buffer.clear()
        buffer.put(emptyArray, 0, len)
      }

      buffer.clear()
    }

    /**
     * Mark that USB no longer owns this buffer
     */
    def setCompletion() {
      activeBuffers.remove(this)
    }

    /**
     * Enqueue this request onto our endpoint
     *
     * @return true for success
     */
    def start() = {
      initialize(connection, if (isRead) dev.mReadEndpoint else dev.mWriteEndpoint)

      val success = queue(buffer, len)
      if (success)
        activeBuffers.add(this)

      success
    }

    def clearAndStart() = {
      clear()
      if (!start()) {
        error("Returning EOF for shutdown endpoint")
        false
      } else
        true
    }

  }

  def close() {
    // Take back any buffers we gave to USB
    activeBuffers.foreach(_.cancel)
    activeBuffers.clear()
  }

  /**
   * Make sure we have enough reads queued up that we never drop bytes
   */
  private def startReads() {
    readBuffers.foreach(_.start())
    readBuffers.clear()
  }

  /**
   * Write a packet
   * FIXME - have caller pass in a bytebuffer directly
   */
  def write(src: Array[Byte], timeoutMsec: Int) = {
    if(src.size != 0) {
      val pkt = new Request(isRead = false, len = src.length)
      pkt.buffer.put(src) // Copy the src array - because it may be going away
      pkt.buffer.limit(pkt.buffer.position)
      pkt.start()
    }
  }

  /**
   * Read a buffer - caller must call returnBuffer after they are finished processing
   */
  def readBuffer(timeoutMsec: Int) = {

    startReads()

    // FIXME - support timeouts
    val pktOpt = Option(connection.requestWait().asInstanceOf[Request])

    pktOpt.map { pkt =>
      pkt.setCompletion()
      if (pkt.isRead) {
        if (isUsbBusted) {
          // http://b.android.com/28023 - we can't know number of bytes, so assume packet is full
          pkt.buffer.limit(pkt.len)
          pkt.buffer.position(0)
        } else
          pkt.buffer.flip()

        var numBytes = pkt.buffer.remaining - bytesToSkip

        if (numBytes < 0) {
          debug("Short: " + pkt.buffer.array.take(pkt.buffer.limit).map { b => "%02x".format(b) }.mkString(","))
          None // Oops - not enough header bytes to discard
        } else {
          pkt.buffer.position(bytesToSkip)

          Some(pkt)
        }
      } else {
        // pkt.clear() // We just let the garbage collector handle write buffers

        None // Try again - we just got back results from a write
      }
    }.getOrElse {
      error("null for requestWait")
      None
    }
  }

  def read(dest: Array[Byte], timeoutMsec: Int) = {

    val pktOpt = readBuffer(timeoutMsec)

    pktOpt.map { pkt =>
      val numBytes = pkt.buffer.remaining

      pkt.buffer.get(dest, 0, numBytes)

      // Return to pool of active reads
      if (!pkt.clearAndStart()) {
        -1
      } else
        numBytes

    }.getOrElse {
      0 // Tell client to try again
    }
  }
}

object AsyncSerial {
  // Per http://code.google.com/p/android/issues/detail?id=28023 - fixed in 4.2, before then you can't depend on position in returned buffers
  val isUsbBusted = Build.VERSION.SDK_INT < 17
}
