package com.geeksville.util

object MathTools {

  /**
   * degrees to radians
   */
  def toRad(d: Double) = d * (math.Pi / 180)

  /**
   * @return distance in meters
   *
   * Uses http://en.wikipedia.org/wiki/Haversine_formula
   */
  def distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {
    val R = 6371 * 1000.0; // m
    val dLat = toRad(lat2 - lat1)
    val dLon = toRad(lon2 - lon1)
    val lat1r = toRad(lat1)
    val lat2r = toRad(lat2)

    val a = math.sin(dLat / 2) * math.sin(dLat / 2) +
      math.sin(dLon / 2) * math.sin(dLon / 2) * math.cos(lat1r) * math.cos(lat2r);
    val c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a));
    val d = R * c
    d
  }
}