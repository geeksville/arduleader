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

case class TimestampedMessage(time: Long, msg: MAVLinkMessage) {
  def timeMsec = time / 1000
  def timeAsDate = new Date(timeMsec)
}

/**
 *
 * Reads tlog files
 */
class BinaryMavlinkReader(bytes: Array[Byte]) extends Iterable[TimestampedMessage] {

  /**
   * Start reading from scratch every time someone accesses the records iterator
   */
  def iterator = new Iterator[TimestampedMessage] {
    private var n: Option[TimestampedMessage] = None
    private val stream = new DataInputStream(new ByteArrayInputStream(bytes))

    private val reader = new MAVLinkReader(stream, IMAVLinkMessage.MAVPROT_PACKET_START_V10)

    /// Try to read the next message return Option(time -> msg)
    private def readNext() = {
      try {
        val time = stream.readLong
        val msg = Option(reader.getNextMessage())

        msg.map { raw =>
          TimestampedMessage(time, raw)
        }
      } catch {
        case ex: IOException =>
          println("Error reading mavlink file: " + ex)
          None
      }
    }

    def hasNext = {
      // Prefetch the next valid record
      while (stream.available > 0 && !n.isDefined) {
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