package com.geeksville.dataflash

import scala.io.Source
import scala.collection.mutable.HashMap
import com.geeksville.util.ThreadTools
import com.geeksville.util.AnalyticsService
import java.util.Date

trait Element[T] {
  def value: T

  // Try to get this as a double if we can
  def asDouble: Double = throw new Exception("Magic double conversion not supported")

  override def toString = value.toString
}

/// Converts from strings or binary to the appriate native Element
trait ElementConverter {
  def toElement(s: String): Element[_]
}

case object IntConverter extends ElementConverter {
  def toElement(s: String) = new Element[Int] {
    val value = s.toInt
    override def asDouble = value
  }
}

case class FloatConverter(scale: Double = 1.0) extends ElementConverter {
  def toElement(s: String) = new Element[Double] {
    val value = s.toDouble // Strings come in prescaled
    override def asDouble = value
  }
}

case object StringConverter extends ElementConverter {
  def toElement(s: String) = new Element[String] {
    val value = s
  }
}

/// Describes the formating for a particular message type
case class DFFormat(typ: Int, name: String, len: Int, format: String, columns: Seq[String]) {

  val nameToIndex = Map(columns.zipWithIndex.map { case (name, i) => name -> i }: _*)

  def isFMT = name == "FMT"

  /// Decode string arguments and generate a message (if possible)
  def createMessage(args: Seq[String]): Option[DFMessage] = {
    val elements = args.zipWithIndex.map {
      case (arg, index) =>
        //println(s"Looking for $index in $this")
        val typ = if (index < format.size)
          format(index) // find the type code letter
        else
          'Z' // If we have too many args passed in, treat the remainder as strings

        val converter = DFFormat.typeCodes.getOrElse(typ, throw new Exception(s"Unknown type code '$typ'"))
        //println(s"Using $converter for ${if (index < format.size) columns(index) else "unknown"}/$index=$typ")
        converter.toElement(arg)
    }
    Some(new DFMessage(this, elements))
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
    'b' -> IntConverter,
    'B' -> IntConverter,
    'h' -> IntConverter,
    'H' -> IntConverter,
    'i' -> IntConverter,
    'I' -> IntConverter,
    'f' -> FloatConverter(),
    'n' -> StringConverter,
    'N' -> StringConverter,
    'Z' -> StringConverter,
    'c' -> FloatConverter(0.01),
    'C' -> FloatConverter(0.01),
    'e' -> FloatConverter(0.01),
    'E' -> FloatConverter(0.01),
    'L' -> FloatConverter(1.0e-7),
    'M' -> StringConverter,
    'q' -> IntConverter,
    'Q' -> IntConverter)
}

/// A dataflash message
case class DFMessage(fmt: DFFormat, elements: Seq[Element[_]]) {
  def fieldNames = fmt.columns
  def asPairs = fieldNames.zip(elements)

  def get[T](name: String) = elements(fmt.nameToIndex(name)).asInstanceOf[Element[T]].value
  def getOpt[T](name: String) = fmt.nameToIndex.get(name).map(elements(_).asInstanceOf[Element[T]].value)
  def getOptDouble(name: String) = fmt.nameToIndex.get(name).map(elements(_).asDouble)

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

    val t = gpsTimeToTime(weekOpt.get, timeMSopt.get * 0.001)

    //println(s"GPS date is " + new Date((t * 1e3).toLong))

    (t * 1e6).toLong
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

  def timeMSopt = getOpt[Int]("TimeMS")
}

object DFMessage {
  final val GPS = "GPS"
  final val PARM = "PARM"
  final val MODE = "MODE"
  final val ATT = "ATT"
  final val IMU = "IMU"
  final val CMD = "CMD"
}

class DFReader {

  val textToFormat = HashMap[String, DFFormat]()

  /// We initially only understand FMT message, we learn the rest
  Seq {
    DFFormat(0x80, "FMT", 89, "BBnNZ", Seq("Type", "Length", "Name", "Format", "Columns"))
  }.foreach(addFormat)

  def addFormat(f: DFFormat) {
    textToFormat(f.name) = f
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

            // If it is a new format type, then add it
            if (fmt.isFMT) {
              // Example: FMT, 129, 23, PARM, Nf, Name,Value
              val newfmt = DFFormat(args(0).toInt, args(2), args(1).toInt, args(3), args.drop(4))
              //println(s"Adding new format: $newfmt")
              addFormat(newfmt)
            }

            fmt.createMessage(args)
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
}

object DFReader {
  def main(args: Array[String]) {
    val reader = new DFReader

    // FIXME - this leaks file descriptors
    val filename = "/home/kevinh/tmp/test.log"
    for (line <- reader.parseText(Source.fromFile(filename))) {
      println(line)
    }
  }
}