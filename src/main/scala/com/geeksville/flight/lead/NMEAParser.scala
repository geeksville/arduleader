package com.geeksville.flight.lead

case class GPSPosition(var time: Double = 0.0, var lat: Double = 0.0, var lon: Double = 0.0,
  var quality: Int = 0, var dir: Double = 0, var altitude: Double = 0, var velocity: Double = 0) {

  def fixed = quality > 0
}

class NMEAParser {
  private val handlers = Map(
    "GPGGA" -> GPGGA _,
    "GPGGL" -> GPGGL _,
    "GPRMC" -> GPRMC _,
    "GPVTG" -> GPVTG _,
    "GPRMZ" -> GPRMZ _)

  def parse(line: String) = {
    if (line.startsWith("$")) {
      val nmea = line.substring(1)
      val tokens = nmea.split(",")
      val typ = tokens(0)
      val r = handlers.get(typ).map { f => Some(f(tokens)) }.getOrElse {
        println("Warning, unknown NMEA type: " + typ)
        None
      }
      r
    } else {
      println("Warning, invalid NMEA line: " + line)
      None
    }
  }

  private def latitude2Decimal(lat: String, NS: String) = {
    var med = lat.substring(2).toFloat / 60.0f;
    med += lat.substring(0, 2).toFloat;
    if (NS.startsWith("S")) {
      med = -med;
    }
    med
  }

  private def longitude2Decimal(lat: String, NS: String) = {
    var med = lat.substring(3).toFloat / 60.0f;
    med += lat.substring(0, 3).toFloat;
    if (NS.startsWith("W")) {
      med = -med;
    }
    med
  }

  private def GPGGA(tokens: Array[String]) =
    GPSPosition(time = (tokens(1)).toDouble,
      lat = latitude2Decimal(tokens(2), tokens(3)),
      lon = latitude2Decimal(tokens(4), tokens(5)),
      quality = tokens(6).toInt,
      altitude = tokens(9).toDouble)

  private def GPGGL(tokens: Array[String]) =
    GPSPosition(time = (tokens(5)).toDouble,
      lat = latitude2Decimal(tokens(1), tokens(2)),
      lon = latitude2Decimal(tokens(3), tokens(4)))

  private def GPRMC(tokens: Array[String]) =
    GPSPosition(time = (tokens(1)).toDouble,
      lat = latitude2Decimal(tokens(3), tokens(4)),
      lon = latitude2Decimal(tokens(5), tokens(6)),
      velocity = tokens(7).toDouble,
      dir = tokens(8).toDouble)

  private def GPVTG(tokens: Array[String]) =
    GPSPosition(
      dir = tokens(3).toDouble)

  private def GPRMZ(tokens: Array[String]) =
    GPSPosition(
      altitude = tokens(1).toDouble)
}
