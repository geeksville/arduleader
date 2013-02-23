package com.geeksville.mavlink

import java.io.InputStream
import org.mavlink.MAVLinkReader
import org.mavlink.IMAVLinkMessage
import java.io.DataInputStream
import org.mavlink.messages.MAVLinkMessage
import java.util.Date
import org.mavlink.messages.ardupilotmega.msg_global_position_int

case class TimestampedMessage(time: Long, msg: MAVLinkMessage) {
  def timeMsec = time / 1000
  def timeAsDate = new Date(timeMsec)
}

/**
 *
 * Reads tlog files
 */
class BinaryMavlinkReader(is: InputStream) {
  private val stream = new DataInputStream(is)

  private val reader = new MAVLinkReader(stream, IMAVLinkMessage.MAVPROT_PACKET_START_V10)

  /// Try to read the next message return Option(time -> msg)
  private def readNext() = {
    val time = stream.readLong
    val msg = Option(reader.getNextMessage())

    msg.map { raw =>
      TimestampedMessage(time, raw)
    }
  }

  private val iterator = new Iterator[TimestampedMessage] {
    private var n: Option[TimestampedMessage] = None

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

  // FIXME - currently we preread everything
  val records = iterator.toSeq

  stream.close()
}