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
import com.geeksville.akka.PoisonPill

// with SerialPortEventListener

/**
 * Talks mavlink out a serial port
 */
class MavlinkStream(val out: OutputStream, val instream: InputStream) extends InstrumentedActor with MavlinkReceiver {

  log.debug("MavlinkStream starting")

  val rxThread = ThreadTools.createDaemon("streamRx")(rxWorker)

  rxThread.start()

  // Mission control does this, seems to be necessary to keep device from hanging up on us
  out.write("\r\n\r\n\r\n".map(_.toByte).toArray)

  def onReceive = {
    case msg: MAVLinkMessage ⇒
      //log.debug("Sending ser (sysId=%d): %s".format(msg.sysId, msg))

      try {
        val bytes = msg.encode()
        out.write(bytes)
        out.flush()
      } catch {
        case ex: IOException =>
          log.error("Error sending packet: " + ex.getMessage)
      }
  }

  override def postStop() {
    // FIXME - close the serial device?

    // This should cause the rx thread to bail
    instream.close()

    super.postStop()
  }

  private def rxWorker() {
    log.debug("MavlinkStream thread running")
    using(instream) { stream =>
      val reader = new MAVLinkReader(new DataInputStream(stream), IMAVLinkMessage.MAVPROT_PACKET_START_V10)

      var lostBytes = 0

      while (!self.isTerminated) {
        try {
          //log.debug("Reading next packet")
          val msg = Option(reader.getNextMessage())
          msg.foreach { s =>
            //log.debug("RxSer: " + s)
            if (reader.getLostBytes > lostBytes) {
              // The android version of the library lets an extra two bytes sneak in.  FIXME.  For now
              // ignore silently because it seems okay (I bet the bytes are ftdi header bytes)
              if (reader.getLostBytes != lostBytes + 2)
                log.warn("Serial RX has dropped %d bytes in total...".format(reader.getLostBytes))
              lostBytes = reader.getLostBytes
            }

            handlePacket(s)
          }
        } catch {
          case ex: EOFException =>
            // Kill our actor if our port gets closed
            self ! PoisonPill
        }
      }
    }
    log.debug("Exiting mavlink reader: " + this)
  }
}

