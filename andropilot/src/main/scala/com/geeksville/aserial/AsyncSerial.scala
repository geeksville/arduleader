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

/**
 * I'm currently building upon the USB android serial library, which has a number of problems.
 * Until I get around to merging my changes with that library, I add most of my smarts here.
 *
 * That project is mostly affected by http://b.android.com/28023, but for my case I can work around it.
 */
class AsyncSerial(val dev: UsbSerialDriver, val bytesToSkip: Int = 0) extends AndroidLogger {

  // 64 buffers, 64 bytes each gives 72 packets/sec
  val numBuffers = 64
  val bufferSize = 64

  private val connection = dev.mConnection

  private lazy val readBuffers = ListBuffer.fill(numBuffers)(new Request(true))

  private val activeBuffers = HashSet[Request]()

  private val emptyArray = Array.fill(bufferSize)(0xff.toByte)

  class Request(val isRead: Boolean, len: Int = bufferSize) extends UsbRequest {
    val buffer = ByteBuffer.allocate(len)

    /**
     * Clear our buffer and deallocate any relation to the endpoint
     */
    def clear() {
      close() // Take back from USB ownership

      // Work around/hack for the android bug - fill with zeros so we don't ever replay an old packet
      Arrays.fill(buffer.array, 0.toByte)
      //buffer.clear()
      //buffer.put(emptyArray, 0, len)

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
    val pkt = new Request(isRead = false, len = src.length)
    pkt.buffer.put(src) // Copy the src array - because it may be going away
    pkt.buffer.limit(pkt.buffer.position)
    pkt.start()
  }

  def read(dest: Array[Byte], timeoutMsec: Int) = {

    startReads()

    // FIXME - support timeouts
    val pktOpt = Option(connection.requestWait().asInstanceOf[Request])

    pktOpt.map { pkt =>
      pkt.setCompletion()

      if (pkt.isRead) {
        if (pkt.buffer.position == 0) {
          // error("Android position bug fixup")
          // http://b.android.com/28023
          pkt.buffer.position(bufferSize)
        }

        var numBytes = pkt.buffer.position - bytesToSkip

        if (numBytes < 0) {
          debug("Short: " + pkt.buffer.array.take(pkt.buffer.position).map { b => "%02x".format(b) }.mkString(","))
          numBytes = 0 // Oops - not enough header bytes to discard
        }

        if (numBytes > 0) {
          //debug("Rx: " + pkt.buffer.array.map { b => "%02x".format(b) }.mkString(","))

          // FIXME: Super skanky - once this works just return the byte buffer instead
          System.arraycopy(pkt.buffer.array, bytesToSkip, dest, 0, numBytes)
        }

        // Return to pool of active reads
        pkt.clear()
        if (!pkt.start()) {
          error("Returning EOF for shutdown endpoint")
          -1
        } else
          numBytes
      } else {
        pkt.clear() // Dealloc any resources this request was using

        0 // Try again - we just got back results from a write
      }
    }.getOrElse {
      error("null for requestWait")
      0
    }
  }
}