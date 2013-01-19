package com.geeksville.mavlink

import java.io._
import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.util.ThreadTools
import com.geeksville.util.Using._
import org.mavlink._
import com.geeksville.util.DebugInputStream
import com.geeksville.util.ByteOnlyInputStream
import com.geeksville.util.Throttled
import com.geeksville.logback.Logging

// with SerialPortEventListener

/**
 * Talks mavlink out a serial port
 */
class MavlinkStream(val out: OutputStream, val instream: InputStream) extends InstrumentedActor with MavlinkReceiver {

  val rxThread = ThreadTools.createDaemon("streamRx")(rxWorker)

  rxThread.start()

  def onReceive = {
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
        try {
          val msg = Option(reader.getNextMessage())
          msg.foreach { s =>
            log.debug("RxSer: " + s)
            if (reader.getLostBytes > lostBytes) {
              lostBytes = reader.getLostBytes
              log.warn("Serial RX has dropped %d bytes in total...".format(lostBytes))
            }

            handlePacket(s)
          }
        } catch {
          case ex: EOFException =>
          // If we were shutting down, ignore the problem
        }
      }
    }
    log.debug("Exiting mavlink reader: " + this)
  }
}

