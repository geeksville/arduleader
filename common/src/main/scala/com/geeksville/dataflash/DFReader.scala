package com.geeksville.dataflash

import scala.io.Source
import scala.collection.mutable.HashMap
import com.geeksville.util.ThreadTools

trait Element[T] {
  def value: T

  override def toString = value.toString
}

/// Converts from strings or binary to the appriate native Element
trait ElementConverter {
  def toElement(s: String): Element[_]
}

case object IntConverter extends ElementConverter {
  def toElement(s: String) = new Element[Int] {
    def value = s.toInt
  }
}

case class FloatConverter(scale: Double = 1.0) extends ElementConverter {
  def toElement(s: String) = new Element[Float] {
    def value = s.toFloat // Strings come in prescaled
  }
}

case object StringConverter extends ElementConverter {
  def toElement(s: String) = new Element[String] {
    def value = s
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
        //println(s"Using $converter for $index=$typ")
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

  def apply(name: String) = elements(fmt.nameToIndex(name))

  override def toString = s"${fmt.name}: " + asPairs.mkString(", ")
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
          if (fmt.isFMT)
            ThreadTools.catchIgnore { // This line could be malformated in many different ways
              // Example: FMT, 129, 23, PARM, Nf, Name,Value
              val newfmt = DFFormat(args(0).toInt, args(2), args(1).toInt, args(3), args.drop(4))
              println(s"Adding new format: $newfmt")
              addFormat(newfmt)
            }

          fmt.createMessage(args)
      }
    } else
      None
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