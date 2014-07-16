package com.geeksville.flight

import java.io._
import java.text.SimpleDateFormat
import scala.io.Source
import com.geeksville.util.MathTools
import com.geeksville.logback.Logging

/**
 * @param alt is in meters
 */
case class Location(lat: Double = 0.0, lon: Double = 0.0, alt: Option[Double] = None, time: Long = 0,
  dir: Option[Double] = None, vx: Option[Double] = None, vy: Option[Double] = None, quality: Int = 0) {
  def fixed = quality > 0

  def velocity = for { x <- vx; y <- vy } yield { math.sqrt(x * x + y * y) }

  def distance(l: Location) = MathTools.distance(lat, lon, l.lat, l.lon)

  /// Does lat/lon look reasonable?
  def isValid = {
    val s = 0.1 // Don't just ignore zeros - ignore any location that is around zero
    ((math.abs(lat) > s || math.abs(lon) > s) && lat <= 90.0 && lat >= -90.0 && lon <= 180.0 && lon >= -180.0)
  }
}

object Location extends Logging {

  def filterByVelocity(maxSpeed: Double, locIn: Seq[Location]) = locIn.filter { l =>
    val okay = l.velocity.get <= maxSpeed

    if (!okay)
      logger.warn("Velocity too high, filtering: " + l.velocity.get + " m/s")

    okay
  }

  /**
   * Given a sequence of locations - enhance them by adding bearing/velocity information
   */
  def addVelAndBearing(locIn: Seq[Location]) = {
    val start = locIn(0)

    logger.debug("start=" + start)

    // Find typical # of degrees per meter in the ew direction for our position on the planet
    val offsetsX = MathTools.applyBearing(start.lat, start.lon, 1, 90)
    val offsetsY = MathTools.applyBearing(start.lat, start.lon, 1, 0)

    logger.debug("offsetsX=" + offsetsX)
    logger.debug("offsetsY=" + offsetsY)

    // Scaling factor 
    val degPerMeterLat = math.abs(offsetsY._1 - start.lat)
    val degPerMeterLon = math.abs(offsetsX._2 - start.lon)

    logger.debug("degPerMeter=" + degPerMeterLat + "," + degPerMeterLon)

    locIn.sliding(2).map {
      case Seq(prev, cur) =>

        val secs = (cur.time - prev.time) / 1000.0

        // We prefer the values in the source data, if they are populated
        val vx = cur.vx.getOrElse {
          val dLon = cur.lon - prev.lon

          (dLon / degPerMeterLon) / secs
        }

        val vy = cur.vy.getOrElse {
          val dLat = cur.lat - prev.lat
          (dLat / degPerMeterLat) / secs
        }

        val bearing = cur.dir.getOrElse(MathTools.bearing(prev.lat, prev.lon, cur.lat, cur.lon).toDouble)

        val r = Location(cur.lat, cur.lon, cur.alt, cur.time, vx = Some(vx), vy = Some(vy), dir = Some(bearing))

        //logger.debug("secs %s, vels = %s, %s, v = %s, dir = %s".format(secs, vx, vy, r.velocity, r.dir))

        if (r.velocity == Double.PositiveInfinity)
          throw new Exception("Invalid velocity")
        r
    }
  }
}