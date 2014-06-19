package com.geeksville.dataflash

import scala.io.Source
import scala.collection.mutable.HashMap
import com.geeksville.util.ThreadTools
import com.geeksville.util.AnalyticsService
import java.util.Date
import java.io.InputStream
import java.io.BufferedInputStream
import java.io.FileInputStream
import com.geeksville.util.Using
import java.io.DataInputStream
import java.io.EOFException

trait Element[T] {
  def value: T

  // Try to get this as a double if we can
  def asDouble: Double = throw new Exception("Magic double conversion not supported")
  def asInt: Int = throw new Exception("Magic int conversion not supported")
  def asString: String = throw new Exception("Magic string conversion not supported")

  override def toString = value.toString
}

/// Converts from strings or binary to the appriate native Element
trait ElementConverter {
  def toElement(s: String): Element[_]

  /// @return a tuple with an element and the # of bytes
  def readBinary(in: DataInputStream): (Element[_], Int)
}

class LongElement(val value: Long) extends Element[Long] {
  override def asDouble = value
  override def asInt = value.toInt
}
class DoubleElement(val value: Double) extends Element[Double] { override def asDouble = value }
class StringElement(val value: String) extends Element[String] {
  override def asString = value
}

case class IntConverter(reader: DataInputStream => Long, numBytes: Int) extends ElementConverter {
  def toElement(s: String) = new LongElement(s.toLong)
  def readBinary(in: DataInputStream) = (new LongElement(reader(in)), numBytes)
}

case class FloatConverter(reader: DataInputStream => Double, numBytes: Int, scale: Double = 1.0) extends ElementConverter {
  def toElement(s: String) = new DoubleElement(s.toDouble)
  def readBinary(in: DataInputStream) = (new DoubleElement(reader(in) * scale), numBytes)
}

case class StringConverter(numBytes: Int) extends ElementConverter {

  private val buf = new Array[Byte](numBytes)

  def toElement(s: String) = new StringElement(s)
  def readBinary(in: DataInputStream) = {
    in.read(buf)
    val str = buf.takeWhile(_ != 0).map(_.toChar).mkString
    (new StringElement(str), numBytes)
  }
}

case class ModeConverter() extends ElementConverter {
  def toElement(s: String) = new StringElement(s)
  def readBinary(in: DataInputStream) = {
    throw new Exception("MODE codes not implemented")
    val mode = in.readByte()
    val str = "FIXME"
    (new StringElement(str), 1)
  }
}

/// Describes the formating for a particular message type
/// @param len length of binary packet in bytes - including the three byte header
case class DFFormat(typ: Int, name: String, len: Int, format: String, columns: Seq[String]) {

  val nameToIndex = Map(columns.zipWithIndex.map { case (name, i) => name -> i }: _*)

  def isFMT = name == "FMT"

  private def converter(typ: Char) = DFFormat.typeCodes.getOrElse(typ, throw new Exception(s"Unknown type code '$typ'"))

  /// Decode string arguments and generate a message (if possible)
  def createMessage(args: Seq[String]): Option[DFMessage] = {
    val elements = args.zipWithIndex.map {
      case (arg, index) =>
        //println(s"Looking for $index in $this")
        val typ = if (index < format.size)
          format(index) // find the type code letter
        else
          'Z' // If we have too many args passed in, treat the remainder as strings

        //println(s"Using $converter for ${if (index < format.size) columns(index) else "unknown"}/$index=$typ")
        converter(typ).toElement(arg)
    }
    Some(new DFMessage(this, elements))
  }

  /// Decode a binary blob, read ptr at entry is just after the header
  def createBinary(in: DataInputStream): Option[DFMessage] = {
    var totalBytes = 0
    val elements = format.map { f =>
      val conv = converter(f)
      val (elem, numBytes) = conv.readBinary(in)
      totalBytes += numBytes
      elem
    }

    // Check that we got the right amount of payload
    val expectedBytes = len - 3
    if (totalBytes <= expectedBytes) {
      if (totalBytes < expectedBytes) {
        println(s"packet too short for $this")
        in.skipBytes(expectedBytes - totalBytes)
      }

      val r = new DFMessage(this, elements)
      println(s"Returning msg $r")
      Some(r)
    } else {
      println(s"Error packet too long for $this")
      None
    }
  }
}

object DFFormat {

  /*
Format characters in the format string for binary log messages
  b   : int8_t
  B   : uint8_t
  h   : int16_t
  H   : uint16_t
  i   : int32_t
  I   : uint32_t
  f   : float
  n   : char[4]
  N   : char[16]
  Z   : char[64]
  c   : int16_t * 100
  C   : uint16_t * 100
  e   : int32_t * 100
  E   : uint32_t * 100
  L   : int32_t latitude/longitude
  M   : uint8_t flight mode
 */

  private val typeCodes = Map[Char, ElementConverter](
    'b' -> IntConverter(_.readByte(), 1),
    'B' -> IntConverter(_.readUnsignedByte(), 1),
    'h' -> IntConverter(_.readShort(), 2),
    'H' -> IntConverter(_.readUnsignedShort(), 2),
    'i' -> IntConverter(_.readInt(), 4),
    'I' -> IntConverter(_.readInt().toLong & 0xffffffff, 4),
    'f' -> FloatConverter(_.readFloat(), 4),
    'n' -> StringConverter(4),
    'N' -> StringConverter(16),
    'Z' -> StringConverter(64),
    'c' -> FloatConverter(_.readShort(), 2, 0.01),
    'C' -> FloatConverter(_.readUnsignedShort(), 2, 0.01),
    'e' -> FloatConverter(_.readInt(), 4, 0.01),
    'E' -> FloatConverter(_.readInt().toLong & 0xffffffff, 4, 0.01),
    'L' -> FloatConverter(_.readInt(), 4, 1.0e-7),
    'M' -> ModeConverter(),
    'q' -> IntConverter(_.readLong(), 8),
    'Q' -> IntConverter(_.readLong(), 8))
}

/// A dataflash message
case class DFMessage(fmt: DFFormat, elements: Seq[Element[_]]) {
  def fieldNames = fmt.columns
  def asPairs = fieldNames.zip(elements)

  private def getElement(name: String) = fmt.nameToIndex.get(name).map(elements(_))
  def getOpt[T](name: String) = getElement(name).map(_.asInstanceOf[Element[T]].value)
  def getOptDouble(name: String) = getElement(name).map(_.asDouble)
  def get[T](name: String) = getOpt[T](name).get

  override def toString = s"$typ: " + asPairs.mkString(", ")

  /// The typename 
  def typ = fmt.name

  // Syntatic sugar

  // CMD
  def ctotOpt = getOpt[Int]("CTot")
  def cnumOpt = getOpt[Int]("CNum")
  def cidOpt = getOpt[Int]("CId")
  def prm1Opt = getOptDouble("Prm1")
  def prm2Opt = getOptDouble("Prm2")
  def prm3Opt = getOptDouble("Prm3")
  def prm4Opt = getOptDouble("Prm4")

  // GPS
  def latOpt = getOpt[Double]("Lat")
  def lngOpt = getOpt[Double]("Lng")
  def altOpt = getOpt[Double]("Alt")
  def spdOpt = getOpt[Double]("Spd")
  def weekOpt = getOpt[Int]("Week")

  /// Return time in usecs since 1970
  def gpsTimeUsec = {

    // Returns seconds since 1970
    def gpsTimeToTime(week: Int, sec: Double) = {
      val epoch = 86400 * (10 * 365 + (1980 - 1969) / 4 + 1 + 6 - 2)
      epoch + 86400 * 7 * week + sec - 15
    }

    for {
      week <- weekOpt
      time <- timeMSopt
    } yield {
      val t = gpsTimeToTime(week, time * 0.001)

      //println(s"GPS date is " + new Date((t * 1e3).toLong))

      (t * 1e6).toLong
    }

    /*
    def find_time_base_new(self, gps):
        '''work out time basis for the log - new style'''
        t = self._gpsTimeToTime(gps.Week, gps.TimeMS*0.001)
        self.timebase = t - gps.T*0.001
        self.new_timestamps = True



    def _find_time_base(self):
        '''work out time basis for the log'''
        self.timebase = 0
        if self._zero_time_base:
            return
        gps1 = self.recv_match(type='GPS', condition='getattr(GPS,"Week",0)!=0 or getattr(GPS,"GPSTime",0)!=0')
        if gps1 is None:
            self._rewind()
            return
            
                    if 'T' in gps1._fieldnames:
            # it is a new style flash log with full timestamps
            self._find_time_base_new(gps1)
            self._rewind()
            return
*/
    /* FIXME - support PX4 native
        def _find_time_base_px4(self, gps):
	        '''work out time basis for the log - PX4 native'''
	        t = gps.GPSTime * 1.0e-6
	        self.timebase = t - self.px4_timebase
	        self.px4_timestamps = True
        
        if 'GPSTime' in gps1._fieldnames:
            self._find_time_base_px4(gps1)
            self._rewind()
            return
            * 
            */

  }

  // CURR
  def thrOut = get[Int]("ThrOut")

  // MODE
  def mode = get[String]("Mode")

  // PARM
  def name = get[String]("Name")
  def value = get[Double]("Value")

  // NTUN
  def arspdOpt = getOpt[Double]("Arspd")

  def timeMSopt = getOpt[Int]("TimeMS")
}

object DFMessage {
  final val GPS = "GPS"
  final val PARM = "PARM"
  final val MODE = "MODE"
  final val ATT = "ATT"
  final val IMU = "IMU"
  final val CMD = "CMD"
  final val NTUN = "NTUN"
  final val MSG = "MSG"
}

class DFReader {

  val textToFormat = HashMap[String, DFFormat]()
  val typToFormat = HashMap[Int, DFFormat]()

  /// We initially only understand FMT message, we learn the rest
  Seq {
    DFFormat(0x80, "FMT", 89, "BBnNZ", Seq("Type", "Length", "Name", "Format", "Columns"))
  }.foreach(addFormat)

  def addFormat(f: DFFormat) {
    textToFormat(f.name) = f
    typToFormat(f.typ) = f
  }

  def tryParseLine(s: String): Option[DFMessage] = {
    // println(s"Parsing $s")
    try { // This line could be malformated in many different ways
      val splits = s.split(',').map(_.trim)
      /* 
        * FMT, 128, 89, FMT, BBnNZ, Type,Length,Name,Format
        * FMT, 129, 23, PARM, Nf, Name,Value
*/
      if (splits.length >= 2) {
        val typ = splits(0)
        textToFormat.get(typ) match {
          case None =>
            println(s"Unrecognized format: $typ")
            None
          case Some(fmt) =>
            val args = splits.tail
            if (args.size < fmt.columns.size) {
              println("Not enough elements - line probably corrupted")
              None
            } else {
              // If it is a new format type, then add it
              if (fmt.isFMT) {
                // Example: FMT, 129, 23, PARM, Nf, Name,Value
                val newfmt = DFFormat(args(0).toInt, args(2), args(1).toInt, args(3), args.drop(4))
                //println(s"Adding new format: $newfmt")
                addFormat(newfmt)
              }

              fmt.createMessage(args)
            }

        }
      } else
        None
    } catch {
      case ex: Exception =>
        println(s"Malformed log: $s", ex)
        None
    }
  }

  ///should just map from source to records - so callers can read lazily
  def parseText(in: Source): Iterator[DFMessage] = {
    var hasSeenFMT = false

    in.getLines.zipWithIndex.flatMap {
      case (l, i) =>
        if (i > 100 && !hasSeenFMT)
          throw new Exception("This doesn't seem to be a dataflash log")

        val msgOpt = tryParseLine(l)
        msgOpt.foreach { msg =>
          hasSeenFMT |= msg.fmt.isFMT
        }
        msgOpt
    }
  }

  def warn(s: String) {
    println(s)
  }

  def parseBinary(instream: InputStream) = new Iterator[DFMessage] {
    // We use EOFException to terminate
    private val in = new DataInputStream(instream)
    private var closed = false

    private var numRead = 0
    private var hasSeenFMT = false

    // Read next valid DFForamt
    private def getHeader(): Option[DFFormat] = {
      numRead += 1

      if (in.readByte() != 0xa3.toByte) {
        warn("Bad header1 byte")
        None
      } else if (in.readByte() != 0x95.toByte) {
        warn("Bad header2 byte")
        None
      } else {
        val code = in.readByte().toInt & 0xff
        println(s"Looking for format $code")
        typToFormat.get(code)
      }
    }

    private def getMessage() = {
      try {
        val r = getHeader().flatMap(_.createBinary(in))

        r.foreach { msg =>
          hasSeenFMT |= msg.fmt.isFMT

          // If it is a new format type, then add it
          if (msg.fmt.isFMT) {
            val elements = msg.elements
            val colnames = elements(4).asString.split(',')

            val newfmt = DFFormat(elements(0).asInt, elements(2).asString, elements(0).asInt, elements(3).asString, colnames)
            println(s"Adding new format: $newfmt")
            addFormat(newfmt)
          }
        }

        if (numRead > 20 && !hasSeenFMT)
          throw new Exception("This doesn't seem to be a dataflash log")

        r
      } catch {
        case ex: EOFException =>
          in.close()
          closed = true
          None
      }
    }

    private var msgopt: Option[DFMessage] = None

    def hasNext = {
      while (!msgopt.isDefined && !closed)
        msgopt = getMessage()

      msgopt.isDefined
    }

    def next = {
      val r = msgopt.orNull
      msgopt = None
      r
    }
  }
}

object DFReader {
  def main(args: Array[String]) {
    val reader = new DFReader

    // FIXME - this leaks file descriptors
    val filename = "/home/kevinh/tmp/test.bin"
    for (line <- reader.parseBinary(new BufferedInputStream(new FileInputStream(filename)))) {
      println(line)
    }
  }
}