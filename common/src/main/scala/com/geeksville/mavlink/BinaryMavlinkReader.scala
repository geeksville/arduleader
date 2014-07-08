package com.geeksville.mavlink

import java.io.InputStream
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
  def fields: Seq[(String, Any)]

  def messageType: String
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