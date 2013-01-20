package com.geeksville.ftdi

import net.java.dev.sna.SNA
import com.sun.jna.ptr._
import com.sun.jna._
import java.nio.ByteBuffer
import scala.reflect.Manifest

class FtdiException(msg: String) extends Exception(msg)

/**
 * Native glue for the libftdi library
 */
object LibFtdi {

  type ENUM = Int
  type INT = Int
  type BYTE = Byte
  type UINT = Int

  type ftdi_context = Pointer

  val SIO_DISABLE_FLOW_CTRL = 0x0
  val SIO_RTS_CTS_HS = (0x1 << 8)
  val SIO_DTR_DSR_HS = (0x2 << 8)
  val SIO_XON_XOFF_HS = (0x4 << 8)

  val SIO_SET_DTR_MASK = 0x1
  val SIO_SET_DTR_HIGH = (1 | (SIO_SET_DTR_MASK << 8))
  val SIO_SET_DTR_LOW = (0 | (SIO_SET_DTR_MASK << 8))
  val SIO_SET_RTS_MASK = 0x2
  val SIO_SET_RTS_HIGH = (2 | (SIO_SET_RTS_MASK << 8))
  val SIO_SET_RTS_LOW = (0 | (SIO_SET_RTS_MASK << 8))

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
  def ftdi_setflowctrl(ftdi: ftdi_context, ctrl: INT): INT
  @native
  def ftdi_setdtr(ftdi: ftdi_context, ctrl: INT): INT
  @native
  def ftdi_setrts(ftdi: ftdi_context, ctrl: INT): INT
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
    new FtdiDevice(vendor, product)
  }

  /// For testing
  def main(args: Array[String]) {
    println("Starting")

    val context = open(0x0403, 0x6001)
    context.close()
  }
}

