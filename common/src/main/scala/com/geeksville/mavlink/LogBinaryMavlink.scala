package com.geeksville.mavlink

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import LogIncomingMavlink._
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import com.geeksville.logback.Logging
import com.geeksville.util.Throttled

/**
 * Output a mission planner compatible tlog file
 *
 * File format seems to be time in usec as a long (big endian), followed by packet.
 */
class LogBinaryMavlink(val file: File, val deleteIfBoring: Boolean) extends InstrumentedActor {

  private val tempFile = new File(file.getCanonicalPath() + ".tmp")
  private val out = new BufferedOutputStream(new FileOutputStream(tempFile, true), 8192)

  val messageThrottle = new Throttled(60 * 1000)
  var oldNumPacket = 0L
  var numPacket = 0L

  /**
   * If false we think we've seen enough interesting action to keep this file around
   */
  var isBoring = true

  logger.info("Logging to " + file.getAbsolutePath)

  private val buf = ByteBuffer.allocate(8)
  buf.order(ByteOrder.BIG_ENDIAN)

  override def postStop() {
    log.info("Closing log file...")
    out.close()
    if (deleteIfBoring && isBoring) {
      log.error("Deleting boring file " + file)
      tempFile.delete()
    } else {
      log.info("Renaming to " + file)
      tempFile.renameTo(file)
    }

    super.postStop()
  }

  def onReceive = {
    case msg: MAVLinkMessage ⇒
      // def str = "Rcv" + msg.sysId + ": " + msg
      //log.debug("Binary write: " + msg)
      numPacket += 1

      messageThrottle { dt =>
        val numSec = dt / 1000.0

        val mPerSec = (numPacket - oldNumPacket) / numSec
        oldNumPacket = numPacket

        log.info("msg write per sec %s".format(mPerSec))
      }

      // Time in usecs
      val time = System.currentTimeMillis * 1000
      buf.clear()
      buf.putLong(time)
      out.write(buf.array)

      // Payload
      out.write(msg.encode)
  }
}

object LogBinaryMavlink extends Logging {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

  /// Allocate a filename in the spooldir
  def getFilename(spoolDir: File = new File("logs")) = {
    if (!spoolDir.exists)
      spoolDir.mkdirs()

    val fname = dateFormat.format(new Date) + ".tlog"
    new File(spoolDir, fname)
  }

  // Create a new log file 
  def create(deleteIfBoring: Boolean, file: File = getFilename()) = {
    new LogBinaryMavlink(file, deleteIfBoring)
  }
}