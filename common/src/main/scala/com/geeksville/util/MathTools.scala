package com.geeksville.util

import com.geeksville.logback.Logging

object MathTools extends Logging {

  def average(s: Iterable[Double]) = s.reduce(_ + _) / s.size
  def max(s: Iterable[Double]) = s.reduce { (p, a) => math.max(p, a) }
  def min(s: Iterable[Double]) = s.reduce { (p, a) => math.min(p, a) }

  /**
   * like math.max, but ignores 'insane' values for MKS units (arbitrarily anything >100000)
   * This is useful to prevent being fooled by obviously crap flight data.
   * @param curVal
   * @param newVal
   */
  def saneMax(curVal: Double, newVal: Double) = {
    if(newVal < 100000.0)
      math.max(curVal, newVal)
    else
      curVal
  }

  /**
   * like math.max, but ignores 'insane' values for MKS units (arbitrarily anything <-100000)
   * This is useful to prevent being fooled by obviously crap flight data.
   * @param curVal
   * @param newVal
   */
  def saneMin(curVal: Double, newVal: Double) = {
    if(newVal > -100000.0)
      math.min(curVal, newVal)
    else
      curVal
  }

  /**
   * degrees to radians
   */
  def toRad(d: Double) = d * (math.Pi / 180)

  def toDeg(r: Double) = r * (180 / math.Pi)

  /**
   * @return distance in meters
   *
   * Uses http://en.wikipedia.org/wiki/Haversine_formula
   */
  def distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) = {
    val R = 6371 * 1000.0; // m
    val dLat = toRad(lat2 - lat1)
    val dLon = toRad(lon2 - lon1)
    val lat1r = toRad(lat1)
    val lat2r = toRad(lat2)

    val a = math.sin(dLat / 2) * math.sin(dLat / 2) +
      math.sin(dLon / 2) * math.sin(dLon / 2) * math.cos(lat1r) * math.cos(lat2r)
    val c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    val d = R * c
    d
  }

  /**
   * Per http://www.movable-type.co.uk/scripts/latlong.html
   */
  def bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double) = {
    val dLon = toRad(lon2 - lon1)
    val lat1r = toRad(lat1)
    val lat2r = toRad(lat2)

    var y = math.sin(dLon) * math.cos(lat2r)
    var x = math.cos(lat1r) * math.sin(lat2r) - math.sin(lat1r) * math.cos(lat2r) * math.cos(dLon)
    val r = math.atan2(y, x)

    /* Since atan2 returns values in the range -π ... +π (that is, -180° ... +180°), to normalise the
     * result to a compass bearing (in the range 0° ... 360°, with −ve values transformed into the
     * range 180° ... 360°), convert to degrees and then use (θ+360) % 360, where % is modulo.
     */
    var brng = toDeg(r).toInt

    (brng + 360) % 360
  }

  /**
   * Given a start point, initial bearing, and distance, this will calculate the
   * destination point  along a (shortest distance) great circle arc.
   */
  def applyBearing(lat1d: Double, lon1d: Double, distance: Double, bearing: Int) = {
    val R = 6371 * 1000.0; // m
    val brng = toRad(bearing)
    val lat1 = toRad(lat1d)
    val lon1 = toRad(lon1d)

    val lat2 = math.asin(math.sin(lat1) * math.cos(distance / R) +
      math.cos(lat1) * math.sin(distance / R) * math.cos(brng))
    val lon2 = lon1 + math.atan2(math.sin(brng) * math.sin(distance / R) * math.cos(lat1),
      math.cos(distance / R) - math.sin(lat1) * math.sin(lat2))

    val lonNorm = (lon2 + 3 * math.Pi) % (2 * math.Pi) - math.Pi
    (toDeg(lat2), toDeg(lonNorm))
  }
}
