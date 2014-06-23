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
import java.net.ConnectException
import scala.concurrent._
import scala.util.Random
import java.net.SocketTimeoutException
import akka.actor.PoisonPill
import akka.actor.Actor

// with SerialPortEventListener

/**
 * Receives mavlink from an input stream
 *
 * @param sysIdOverride if set, we will replace any received sysIds with this alternative (useful for remapping sysId based on interface)
 * @param tlogSpeedup if specified, we expect to see an 8 byte timestamp before each msg.  We will play the read data back at the rate we find in the file.
 */
class MavlinkStreamReceiver(
  ingen: => InputStream,
  val sysIdOverride: Option[Int] = None,
  val tlogSpeedup: Option[Double] = None, autoStart: Boolean = true) extends InstrumentedActor with MavlinkReceiver {

  log.debug("MavlinkStream starting")
  MavlinkStreamReceiver.isIgnoreReceive = false

  /**
   * We use generators to init these variables, because android doesn't allow network access from the
   * 'main' thread
   */
  private lazy val instream = ingen // new DebugInputStream(ingen)

  /// This skanky hack is to make sure that we only touch the inputstream if it has already been created
  private var isInstreamValid = false

  /// The id we expect for vehicles on this port (possibly will be overridden)
  val expectedSysId = 1

  val rxThread = ThreadTools.createDaemon("streamRx")(rxWorker)

  /**
   * If true we will pretend to drop many packets
   */
  var simulateUnreliable = false

  private val rand = new Random(System.currentTimeMillis)

  private var shuttingDown = false

  //rxThread.setPriority(Thread.MAX_PRIORITY)

  if (autoStart) {
    log.info("Autostarting reads")
    self ! MavlinkStreamReceiver.StartMsg
  }

  // Mission control does this, seems to be necessary to keep device from hanging up on us
  //out.write("\r\n\r\n\r\n".map(_.toByte).toArray)

  private def shouldDrop = simulateUnreliable && rand.nextInt(10) < 2

  override def onReceive = {
    case MavlinkStreamReceiver.StartMsg =>
      log.info("Received start message")
      rxThread.start()
  }

  override def postStop() {
    log.debug("MavlinkStream postStop")

    shuttingDown = true

    // This should cause the rx thread to bail
    if (isInstreamValid)
      instream.close()

    super.postStop()
  }

  private def rxWorker() {
    println("MavlinkStream thread running")
    try {
      using(instream) { stream =>
        isInstreamValid = true

        val dataStream = new DataInputStream(stream)
        val reader = new MAVLinkReader(dataStream, IMAVLinkMessage.MAVPROT_PACKET_START_V10)

        var lostBytes = 0
        var badSeq = 0

        val messageThrottle = new Throttled(60 * 1000)
        var oldLost = 0L
        var oldNumPacket = 0L
        var numPacket = 0L
        var prevSeq = -1

        val overrideId = sysIdOverride.getOrElse(-1)

        var startTimestamp = 0L
        var startTick = System.currentTimeMillis

        try {
          while (!shuttingDown) {
            //log.debug("Reading next packet")

            // Sleep if needed to simulate the time delay
            tlogSpeedup.foreach { speedup =>
              val nowStamp = (dataStream.readLong / speedup).toLong
              if (startTimestamp == 0L) {
                startTimestamp = nowStamp
              }
              val desired = (nowStamp - startTimestamp) + startTick
              val delay = desired - System.currentTimeMillis
              if (delay > 0) {
                //log.debug(s"Sleeping for $delay")
                Thread.sleep(delay)
              }
            }

            val msg = Option(reader.getNextMessage())
            //println(s"Read packet: $msg")
            msg.foreach { s =>
              numPacket += 1

              // Reassign sysId if requested
              if (overrideId != -1 && s.sysId == expectedSysId)
                s.sysId = overrideId

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
                //log.warn("Serial RX has %d bad sequences in total...".format(badSeq))
              }

              messageThrottle { dt: Long =>
                val numSec = dt / 1000.0

                val newLost = reader.getLostBytes
                val dropPerSec = (newLost - oldLost) / numSec
                oldLost = newLost

                val mPerSec = (numPacket - oldNumPacket) / numSec
                oldNumPacket = numPacket

                log.info("msgs per sec %s, bytes dropped per sec=%s".format(mPerSec, dropPerSec))
              }

              // Dups are normal, the 3dr radio will duplicate packets if it has nothing better to do
              if (s.sequence != prevSeq && !MavlinkStreamReceiver.isIgnoreReceive) //  for profiling
                if (!shouldDrop)
                  handleIncomingPacket(s)

              prevSeq = s.sequence
            }
          }

          // This catch clause is only used _after_ we've successfully opened our socket
        } catch {

          case ex: EOFException =>
            // Kill our actor if our port gets closed
            log.info("Exiting stream reader due to EOF")
            self ! PoisonPill

          case ex: IOException =>
            if (!shuttingDown) {
              log.error("Killing mavlink stream due to: " + ex)
              self ! PoisonPill
            }
        }
      }

      // This catch clause covers connection time problems
    } catch {
      case ex: IOException =>
        log.error("Failure to connect: " + ex.getMessage)
        self ! PoisonPill

      case ex: SocketTimeoutException =>
        log.error("Socket timeout: " + ex.getMessage)
        self ! PoisonPill
    }

    log.debug("Exiting mavlink reader: " + this)
  }
}

object MavlinkStreamReceiver {
  /// Start reading from stream
  case object StartMsg

  var isIgnoreReceive = false
}