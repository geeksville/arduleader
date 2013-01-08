package com.geeksville.mavlink

import gnu.io._
import java.io._
import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.util.ThreadTools
import com.geeksville.util.Using._
import org.mavlink._
import com.geeksville.util.DebugInputStream
import com.geeksville.util.ByteOnlyInputStream
import com.geeksville.util.Throttled
import com.geeksville.ftdi.LibFtdi
import com.geeksville.logback.Logging

// with SerialPortEventListener

/**
 * Talks mavlink out a serial port
 */
class MavlinkStream(val out: OutputStream, val instream: InputStream) extends InstrumentedActor with MavlinkReceiver {

  val rxThread = ThreadTools.createDaemon("SerRx")(rxWorker)

  rxThread.start()

  def receive = {
    case msg: MAVLinkMessage ⇒
      log.debug("Sending ser: " + msg)

      val bytes = msg.encode()
      out.write(bytes)
      out.flush()
  }

  override def postStop() {
    // FIXME - close the serial device?

    // This should cause the rx thread to bail
    instream.close()

    super.postStop()
  }

  private def rxWorker() {
    using(instream) { stream =>
      val reader = new MAVLinkReader(new DataInputStream(stream), IMAVLinkMessage.MAVPROT_PACKET_START_V10)

      var lostBytes = 0

      while (!self.isTerminated) {
        val msg = Option(reader.getNextMessage())
        msg.foreach { s =>
          log.debug("RxSer: " + s)
          if (reader.getLostBytes > lostBytes) {
            lostBytes = reader.getLostBytes
            log.warning("Serial RX has dropped %d bytes in total...".format(lostBytes))
          }

          handlePacket(s)
        }
      }
    }
  }
}

object MavlinkStream extends Logging {
  def openSerial(portName: String, baudRate: Int) = {
    val portIdentifier = CommPortIdentifier.getPortIdentifier(portName)
    if (portIdentifier.isCurrentlyOwned)
      throw new IOException("Error: Port is currently in use")

    val port = portIdentifier.open(this.getClass.getName, 2000).asInstanceOf[SerialPort]

    port.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    port.setInputBufferSize(16384)
    port.setOutputBufferSize(16384)
    port.disableReceiveFraming()
    port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE)

    val out = new BufferedOutputStream(port.getOutputStream, 8192)
    val instream = new ByteOnlyInputStream(port.getInputStream)
    new MavlinkStream(out, instream)
  }

  def openFtdi(portName: String, baudRate: Int) = {
    logger.info("Opening ftdi")
    val dev = LibFtdi.open(0x0403, 0x6001)
    dev.setLatencyTimer(1)
    dev.setReadDataChunksize(1024) // Possibly shrink even further - default was 4096       
    dev.setWriteDataChunksize(256)
    dev.setFlowControl(LibFtdi.SIO_RTS_CTS_HS) // Not sure what is the right flow control option

    // dev.setTimeouts(Integer.MAX_VALUE, 5000)
    // dev.setTimeouts(10, 5000) // Need a short read timeout if using streams

    dev.setBaudRate(baudRate)
    new MavlinkStream(dev.out, dev.in)
  }
}
