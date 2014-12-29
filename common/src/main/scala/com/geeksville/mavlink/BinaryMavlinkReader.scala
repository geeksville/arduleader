package com.geeksville.mavlink

import java.io.InputStream
import com.geeksville.dataflash.DFMessage
import com.geeksville.flight.VehicleSimulator
import org.mavlink.MAVLinkReader
import org.mavlink.IMAVLinkMessage
import java.io.DataInputStream
import org.mavlink.messages.MAVLinkMessage
import java.util.Date
import org.mavlink.messages.ardupilotmega.msg_global_position_int
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArrayBuilder
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.EOFException

/**
 * A common self describing baseclass for TLOG or Log messages
 */
trait AbstractMessage {
  def fields: Map[String, Any]

  def messageType: String
}

/// Dataflash ERR records
case class ErrorCode(val subsysCode: Int, val errorCode: Int) {

  def subsystem = ErrorCode.subsystems.getOrElse(subsysCode, s"Subsys$subsysCode")
  def code = ErrorCode.codes.getOrElse(errorCode, s"Error$errorCode")

  /// A critical fault?
  def isFailsafe = subsystem.contains("FAILSAFE")
  def isPending = code != "RESOLVED"
}

object ErrorCode {
  /// Construct from DFMessages
  def apply(msg: DFMessage): ErrorCode = ErrorCode(msg.subsys, msg.eCode)

  val subsystems = Map(
    1 -> "MAIN", 2 -> "RADIO", 3 -> "COMPASS", 4 -> "OPTFLOW", 5 -> "FAILSAFE_RADIO", 6 -> "FAILSAFE_BATT",
    7 -> "FAILSAFE_GPS", 8 -> "FAILSAFE_GCS", 9 -> "FAILSAFE_FENCE",
    10 -> "FLIGHT_MODE", 11 -> "GPS", 12 -> "CRASH_CHECK", 13 -> "FLIP", 14 -> "AUTOTUNE", 15 -> "PARACHUTE",
    16 -> "EKFINAV_CHECK", 17 -> "FAILSAFE_EKFINAV", 18 -> "BARO", 19 -> "CPU"
  )

  val codes = Map(0 -> "RESOLVED", 1 -> "OCCURRED", 2 -> "GLITCH")

  /* FIXME - use more correct error codes
  // general error codes
  #define ERROR_CODE_ERROR_RESOLVED           0
  #define ERROR_CODE_FAILED_TO_INITIALISE     1
  // subsystem specific error codes -- radio
  #define ERROR_CODE_RADIO_LATE_FRAME         2
  // subsystem specific error codes -- failsafe_thr, batt, gps
  #define ERROR_CODE_FAILSAFE_RESOLVED        0
  #define ERROR_CODE_FAILSAFE_OCCURRED        1
  // subsystem specific error codes -- compass
  #define ERROR_CODE_COMPASS_FAILED_TO_READ   2
  // subsystem specific error codes -- gps
  #define ERROR_CODE_GPS_GLITCH               2
  // subsystem specific error codes -- main
  #define ERROR_CODE_MAIN_INS_DELAY           1
  // subsystem specific error codes -- crash checker
  #define ERROR_CODE_CRASH_CHECK_CRASH        1
  #define ERROR_CODE_CRASH_CHECK_LOSS_OF_CONTROL 2
  // subsystem specific error codes -- flip
  #define ERROR_CODE_FLIP_ABANDONED           2
  // subsystem specific error codes -- autotune
  #define ERROR_CODE_AUTOTUNE_BAD_GAINS       2
  // parachute failed to deploy because of low altitude
  #define ERROR_CODE_PARACHUTE_TOO_LOW        2
  // EKF check definitions
  #define ERROR_CODE_EKFINAV_CHECK_BAD_VARIANCE       2
  #define ERROR_CODE_EKFINAV_CHECK_VARIANCE_CLEARED   0
  // Baro specific error codes
  #define ERROR_CODE_BARO_GLITCH              2
  */
}
case class SimpleMessage(val messageType: String, val fields: Map[String, Any]) extends AbstractMessage

/**
 * An abstract message backed by a mavlink tlog style msg.
 *
 * FIXME: Currently we just do this via a ptr to the src msg, really we should refactor the MAVLinkMessage
 * class so that it is more self describing and can natively implement this interface and support all message
 * types.
 *
 * @param msg
 */
object MavlinkBasedMessage {
  def tryCreate(mIn: MAVLinkMessage): Option[AbstractMessage] = {
    val r = mIn match {
      case m: msg_global_position_int =>
        val l = VehicleSimulator.decodePosition(m)
        Some(SimpleMessage("MAVLINK_MSG_ID_GLOBAL_POSITION_INT", Map("lat" -> l.lat, "lon" -> l.lon, "alt" -> l.alt)))
      case _ =>
        None
    }

    r
  }
}

/**
 * Eventually this will be used for both tlog and log messages - currently only logs
 */
case class TimestampedAbstractMessage(val time: Long, val msg: AbstractMessage) {
  def timeMsec = time / 1000
  def timeSeconds = time / (1e6)
  def timeAsDate = TimestampedMessage.usecsToDate(time)
}

/**
 * @param time is in usecs since 1970
 */
case class TimestampedMessage(time: Long, msg: MAVLinkMessage) {
  def timeMsec = time / 1000
  def timeSeconds = time / (1e6)
  def timeAsDate = TimestampedMessage.usecsToDate(time)
}

object TimestampedMessage {
  def usecsToDate(t: Long) = new Date(t / 1000)
  def usecsToMsecs(t: Long) = t / 1000
  def usecsToSeconds(t: Long) = t / 1e6
}

/**
 * Reads tlog files
 */
class BinaryMavlinkReader(is: InputStream) extends Iterable[TimestampedMessage] {

  def this(bytes: Array[Byte]) = this(new ByteArrayInputStream(bytes))

  /**
   * Start reading from scratch every time someone accesses the records iterator
   */
  def iterator = new Iterator[TimestampedMessage] {
    private var n: Option[TimestampedMessage] = None
    private val stream = new DataInputStream(is)

    private val reader = new MAVLinkReader(stream, IMAVLinkMessage.MAVPROT_PACKET_START_V10)
    private var atEOF = false

    /// Try to read the next message return Option(time -> msg)
    private def readNext() = {
      try {
        val time = stream.readLong
        val msg = Option(reader.getNextMessage())

        msg.map { raw =>
          TimestampedMessage(time, raw)
        }
      } catch {
        case ex: EOFException =>
          atEOF = true
          stream.close()
          None

        case ex: IOException =>
          println("Error reading mavlink file: " + ex)
          atEOF = true
          stream.close()
          None
      }
    }

    def hasNext = {
      // Prefetch the next valid record
      while (!atEOF && !n.isDefined) {
        n = readNext()
      }
      n.isDefined
    }

    def next = {
      val r = n.get
      n = None
      r
    }
  }

  /*
  // FIXME - currently we preread everything
  val records = {
    val builder = ArrayBuilder.make[TimestampedMessage]
    builder.sizeHint(20000)
    builder ++= iterator
    val r = builder.result
    println("Read " + r.size + " messages")
    r
  }
  */
}
