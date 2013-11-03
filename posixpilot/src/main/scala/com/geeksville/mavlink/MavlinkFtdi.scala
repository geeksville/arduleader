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

object MavlinkPosix extends Logging {
  def openSerial(portName: String, baudRate: Int) = {
    val portIdentifier = CommPortIdentifier.getPortIdentifier(portName)
    if (portIdentifier.isCurrentlyOwned)
      throw new IOException("Error: Port is currently in use")

    val port = portIdentifier.open(this.getClass.getName, 2000).asInstanceOf[SerialPort]

    port.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    port.setInputBufferSize(16384)
    port.setOutputBufferSize(16384)
    port.disableReceiveFraming()
    port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT)

    val out = new BufferedOutputStream(port.getOutputStream, 8192)
    val instream = new ByteOnlyInputStream(port.getInputStream) {
      override def close() {
        logger.debug("Closing port")
        port.close()
        super.close()
      }
    }
    new MavlinkStream(out, instream)
  }

  def openFtdi(portName: String, baudRate: Int) = {
    logger.info("Opening ftdi")
    val dev = LibFtdi.open(0x0403, 0x6015, portName)
    logger.info("Ftdi open")
    dev.setLatencyTimer(1)
    dev.setReadDataChunksize(512) // Possibly shrink even further - default was 4096       
    dev.setWriteDataChunksize(256)
    dev.setFlowControl(LibFtdi.SIO_RTS_CTS_HS) // Not sure what is the right flow control option
    dev.setDTR(LibFtdi.SIO_SET_DTR_HIGH)
    // dev.setTimeouts(Integer.MAX_VALUE, 5000)
    // dev.setTimeouts(10, 5000) // Need a short read timeout if using streams

    dev.setBaudRate(baudRate)
    new MavlinkStream(dev.out, dev.in)
  }
}
