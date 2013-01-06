package com.geeksville.flight.lead

import java.util.{ Calendar, TimeZone }

class NMEAParser {
  private val handlers = Map(
    "GPGGA" -> GPGGA _,
    "GPGGL" -> GPGGL _,
    "GPRMC" -> GPRMC _,
    "GPVTG" -> GPVTG _,
    "GPRMZ" -> GPRMZ _)

  /**
   * milliseconds at midnight
   */
  private val midnight = {
    // FIXME, we should use the date info from the NMEA stream
    // http://aprs.gids.nl/nmea/#zda
    val c = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"))
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    c.getTimeInMillis
  }

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

  private def parseTime(s: String) = {
    // Parse hhmmss.ss (in UTC)
    val h = s.substring(0, 2).toInt
    val m = s.substring(2, 4).toInt
    val secs = s.substring(4).toDouble

    (midnight + (h * 60 * 60 * 1000L) + (m * 60 * 1000L) + (secs * 1000L)).toLong
  }

  private def latitude2Decimal(lat: String, NS: String) = {
    val med = lat.substring(2).toFloat / 60.0f + lat.substring(0, 2).toFloat
    if (NS.startsWith("S"))
      -med
    else
      med
  }

  private def longitude2Decimal(lat: String, NS: String) = {
    val med = lat.substring(3).toFloat / 60.0f + lat.substring(0, 3).toFloat;
    if (NS.startsWith("W"))
      -med;
    else
      med
  }

  private def GPGGA(tokens: Array[String]) =
    Location(
      time = parseTime(tokens(1)),
      lat = latitude2Decimal(tokens(2), tokens(3)),
      lon = latitude2Decimal(tokens(4), tokens(5)),
      quality = tokens(6).toInt,
      alt = tokens(9).toDouble)

  private def GPGGL(tokens: Array[String]) =
    Location(
      time = parseTime(tokens(5)),
      lat = latitude2Decimal(tokens(1), tokens(2)),
      lon = latitude2Decimal(tokens(3), tokens(4)))

  private def GPRMC(tokens: Array[String]) = {
    val v = tokens(7).toDouble

    // The rest of our software wants to track vx & vy separately, so for now just assume x & y are equal and solve
    // c^2 = vx^2 + vy^2 for vx/vy
    val vxy = v * 2 / math.sqrt(2)
    Location(
      time = parseTime(tokens(1)),
      lat = latitude2Decimal(tokens(3), tokens(4)),
      lon = latitude2Decimal(tokens(5), tokens(6)),
      vx = Some(vxy),
      vy = Some(vxy),
      dir = Some(tokens(8).toDouble))
  }

  // FIXME - merge this with a position record?
  private def GPVTG(tokens: Array[String]) =
    Location(
      dir = Some(tokens(3).toDouble))

  private def GPRMZ(tokens: Array[String]) =
    Location(
      alt = tokens(1).toDouble)
}
