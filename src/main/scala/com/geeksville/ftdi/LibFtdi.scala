package com.geeksville.ftdi

import net.java.dev.sna.SNA
import com.sun.jna.ptr._
import com.sun.jna._
import java.nio.ByteBuffer
import scala.reflect.Manifest

class FtdiException(msg: String) extends Exception(msg)

/// Native glue for the libftdi library
object LibFtdi {

  class Device(vendor: Int, product: Int) {
    val handle = ftdi_new()

    private var isClosed = false

    private var readRetriesLeft = 10

    /// A lock object to make sure we are out of read before exiting
    private val inRead = new Object

    private val rxBuf = ByteBuffer.allocateDirect(8192)
    rxBuf.limit(0)

    checkError(ftdi_usb_open(handle, vendor, product))
    checkError(ftdi_usb_purge_buffers(handle))

    /// Make sure we tear down our FTDI object cleanly when our app exits (otherwise we blow chunks on Windows)
    Runtime.getRuntime().addShutdownHook(new Thread() {
      override def run() { close(); }
    });

    def close() {
      if (!isClosed) {
        isClosed = true

        inRead.synchronized {
          // Wait here until any previously running read exits
        }

        // We do not want to check for errors here (usb bulk read
        // errors expected)
        ftdi_usb_close(handle)

        // We don't call free, because there might still be another thread using this data structure
        // This means we have a very small leak, but it saves us from doing the close in two stages.  FIXME
        // ftdi_free(handle)
      }
    }

    def read(buf: ByteBuffer, size: Int): Int = {
      inRead.synchronized {
        // If the port is already closed don't even attempt a read
        while (!isClosed) {
          val result = ftdi_read_data(handle, buf, size)

          if (!isClosed)
            // Got an error?
            if (result < 0) {
              val msg = ftdi_get_error_string(handle)

              // The following failure seems to occur rarely on Windows - try to retry
              // LibFTDI error usb bulk read failed
              if (msg.contains("bulk read failed") && readRetriesLeft > 0) {
                println("Ignoring mystery " + msg)
                readRetriesLeft -= 1
              } else
                throw new FtdiException("LibFTDI error " + msg)
            } else
              return result
        }

        -1
      }
    }

    def write(buf: Array[Byte], size: Int) { checkError(ftdi_write_data(handle, buf, size)) }

    def setLatencyTimer(latency: Int) = checkError(ftdi_set_latency_timer(handle, latency.toByte))

    def setReadDataChunksize(size: Int) { checkError(ftdi_read_data_set_chunksize(handle, size)) }
    def setWriteDataChunksize(size: Int) { checkError(ftdi_write_data_set_chunksize(handle, size)) }
    def setBaudRate(size: Int) { checkError(ftdi_set_baudrate(handle, size)) }

    /// Read a single character, or -1 if the stream is closed
    def read(): Int = {
      // Libftdi seems to blow chunks if you quit with a pending read running
      while (!rxBuf.hasRemaining && !isClosed) {
        rxBuf.rewind()

        val numRead = read(rxBuf, rxBuf.capacity)

        if (numRead == -1)
          return -1

        rxBuf.limit(numRead)
      }

      if (isClosed)
        -1
      else
        rxBuf.get().toInt & 0xff
    }

    /// @return whatever was passed in
    def checkError(errCode: INT) = {
      if (errCode < 0)
        throw new FtdiException("LibFTDI error " + ftdi_get_error_string(handle))
      errCode
    }
  }

  type ENUM = Int
  type INT = Int
  type BYTE = Byte
  type UINT = Int

  type ftdi_context = Pointer

  val snaLibrary = if (!Platform.isWindows) "ftdi" else "libftdi"

  Native.register(snaLibrary)

  @native
  def ftdi_new(): ftdi_context;
  @native
  def ftdi_free(ftdi: ftdi_context): Unit
  @native
  def ftdi_usb_open(ftdi: ftdi_context, vendor: INT, product: INT): INT
  @native
  def ftdi_usb_close(ftdi: ftdi_context): INT
  @native
  def ftdi_read_data(ftdi: ftdi_context, buf: ByteBuffer, size: INT): INT
  @native
  def ftdi_write_data(ftdi: ftdi_context, buf: Array[Byte], size: INT): INT
  @native
  def ftdi_set_latency_timer(ftdi: ftdi_context, latency: BYTE): INT
  @native
  def ftdi_read_data_set_chunksize(ftdi: ftdi_context, size: UINT): INT
  @native
  def ftdi_write_data_set_chunksize(ftdi: ftdi_context, size: UINT): INT
  @native
  def ftdi_set_baudrate(ftdi: ftdi_context, baud: INT): INT
  @native
  def ftdi_usb_purge_buffers(ftdi: ftdi_context): INT

  @native
  def ftdi_get_error_string(ftdi: ftdi_context): String

  def open(vendor: Int, product: Int) = {
    new Device(vendor, product)
  }

  /// For testing
  def main(args: Array[String]) {
    println("Starting")

    val context = open(0x0403, 0x6001)
    context.close()
  }
}

