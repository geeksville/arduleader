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

// with SerialPortEventListener

/**
 * Talks mavlink out a serial port
 */
class MavlinkSerial(val portName: String) extends InstrumentedActor with MavlinkReceiver {
  private val portIdentifier = CommPortIdentifier.getPortIdentifier(portName)
  if (portIdentifier.isCurrentlyOwned)
    throw new IOException("Error: Port is currently in use")

  private val port = portIdentifier.open(this.getClass.getName, 2000).asInstanceOf[SerialPort]

  port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
  port.setInputBufferSize(16384)
  port.setOutputBufferSize(16384)
  port.disableReceiveFraming()
  port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE)
  //port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT)
  // port.enableReceiveTimeout(500)

  private val out = new BufferedOutputStream(port.getOutputStream, 8192)
  private val instream = new DataInputStream(new ByteOnlyInputStream(port.getInputStream))

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
    port.close() // This should cause the rx thread to bail
    super.postStop()
  }

  private def rxWorker() {
    using(instream) { stream =>
      val reader = new MAVLinkReader(stream, IMAVLinkMessage.MAVPROT_PACKET_START_V10)

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

