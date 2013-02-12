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
class MavlinkStream(outgen: => OutputStream, ingen: => InputStream) extends InstrumentedActor with MavlinkReceiver {

  log.debug("MavlinkStream starting")
  MavlinkStream.isIgnoreReceive = false

  /**
   * We use generators to init these variables, because android doesn't allow network access from the
   * 'main' thread
   */
  private lazy val out = outgen
  private lazy val instream = ingen

  val rxThread = ThreadTools.createDaemon("streamRx")(rxWorker)

  rxThread.setPriority(Thread.MAX_PRIORITY)
  rxThread.start()

  // Mission control does this, seems to be necessary to keep device from hanging up on us
  //out.write("\r\n\r\n\r\n".map(_.toByte).toArray)

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
    log.debug("MavlinkStream postStop")

    // This should cause the rx thread to bail
    instream.close()

    super.postStop()
  }

  private def rxWorker() {
    log.debug("MavlinkStream thread running")
    using(instream) { stream =>
      val reader = new MAVLinkReader(new DataInputStream(stream), IMAVLinkMessage.MAVPROT_PACKET_START_V10)

      var lostBytes = 0
      var badSeq = 0

      val messageThrottle = new Throttled(60 * 1000)
      var oldLost = 0L
      var oldNumPacket = 0L
      var numPacket = 0L

      while (!self.isTerminated) {
        try {
          //log.debug("Reading next packet")
          val msg = Option(reader.getNextMessage())
          msg.foreach { s =>
            numPacket += 1

            //log.debug("RxSer: " + s)
            if (reader.getLostBytes > lostBytes) {
              // The android version of the library lets an extra two bytes sneak in.  FIXME.  For now
              // ignore silently because it seems okay (I bet the bytes are ftdi header bytes)
              // if (reader.getLostBytes != lostBytes + 2)
              //log.warn("Serial RX has dropped %d bytes in total...".format(reader.getLostBytes))
              lostBytes = reader.getLostBytes
            }

            if (reader.getBadSequence > badSeq) {
              badSeq = reader.getBadSequence
              log.warn("Serial RX has %d bad sequences in total...".format(badSeq))
            }

            messageThrottle { dt =>
              val numSec = dt / 1000.0

              val newLost = reader.getLostBytes
              val dropPerSec = (newLost - oldLost) / numSec
              oldLost = newLost

              val mPerSec = (numPacket - oldNumPacket) / numSec
              oldNumPacket = numPacket

              log.info("msgs per sec %s, bytes dropped per sec=%s".format(mPerSec, dropPerSec))
            }

            //  for profiling
            if (!MavlinkStream.isIgnoreReceive)
              handlePacket(s)
          }
        } catch {
          case ex: EOFException =>
            // Kill our actor if our port gets closed
            self ! PoisonPill

          case ex: IOException =>
            if (!self.isTerminated)
              throw ex // Ignore errors while shutting down
        }
      }
    }
    log.debug("Exiting mavlink reader: " + this)
  }
}

object MavlinkStream {
  var isIgnoreReceive = false
}