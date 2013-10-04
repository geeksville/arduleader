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
import org.mavlink.messages.MAV_TYPE
import scala.collection.mutable.HashSet

/**
 * Output a mission planner compatible tlog file
 *
 * File format seems to be time in usec as a long (big endian), followed by packet.
 */
class LogBinaryMavlink(private var file: File, val deleteIfBoring: Boolean) extends InstrumentedActor {

  private val tempFile = new File(file.getCanonicalPath() + ".tmp")
  private val out = new BufferedOutputStream(new FileOutputStream(tempFile, true), 8192)

  /**
   * What sysIds have we seen while writing this file?  used to make a prettier file name
   */
  val vehiclesSeen = HashSet[Int]()

  val messageThrottle = new Throttled(60 * 1000)
  var oldNumPacket = 0L
  var numPacket = 0L

  private var numMovingPoints = 0

  /**
   * If false we think we've seen enough interesting action to keep this file around
   */
  def isBoring = numMovingPoints < 20

  logger.info("Logging to " + file.getAbsolutePath)

  private val buf = ByteBuffer.allocate(8)
  buf.order(ByteOrder.BIG_ENDIAN)

  /**
   * If possible, try to include vehicle sysid in filename
   */
  private def improveFilename() {
    val newsuffix = if (vehiclesSeen.size < 1) {
      log.error("Can't improve filename, no vehicles")
      ""
    } else if (vehiclesSeen.size == 1 && vehiclesSeen(0) == 1) {
      log.warn("Not improving filename, sysId is 1")
      ""
    } else {
      val r = vehiclesSeen.take(3).mkString("-ids-", "-", "")
      log.warn(s"Not improving filename to $r")
      r
    }

    if (!newsuffix.isEmpty) {
      val fname = LogBinaryMavlink.dateFormat.format(new Date) + newsuffix + ".tlog"
      file = new File(file.getParentFile, fname)
    }
  }

  override def postStop() {
    log.info("Closing log file...")
    out.close()
    if (deleteIfBoring && isBoring) {
      log.error("Deleting boring file " + file)
      tempFile.delete()
    } else {
      improveFilename()
      log.info("Renaming to " + file)
      tempFile.renameTo(file)
    }

    super.postStop()
  }

  private def handleMessage(msg: MAVLinkMessage) {
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

  def onReceive = {
    case vfr: msg_vfr_hud =>
      // Crude check for motion
      if (vfr.groundspeed > 3)
        numMovingPoints += 1
      handleMessage(vfr)

    case msg: msg_heartbeat ⇒
      val typ = msg.`type`
      if (typ != MAV_TYPE.MAV_TYPE_GCS)
        vehiclesSeen += msg.sysId
      handleMessage(msg)

    case msg: MAVLinkMessage ⇒
      handleMessage(msg)
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