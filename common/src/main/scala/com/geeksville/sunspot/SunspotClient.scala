package com.geeksville.sunspot

import com.geeksville.util.AnalyticsService
import com.geeksville.util.Using._
import java.net.URL
import java.io.IOException

/**
 * Warns about high solar radiation interference by using NOAA data
 * Made by cribbing from https://github.com/ligi/Solar-Activity-Monitor and cleaning a few things up.
 */
object SunspotClient {

  /// If true we will always claim a warning
  var testing = false

  /**
   * A value less than this threshold is okay
   */
  val warnThreshold = 4

  /**
   * A value between warn threshold and critical threshold is 'could be bad'.  critical or above should really yell at user
   */
  val criticalThreshold = 5

  private val url = "http://www.swpc.noaa.gov/ftpdir/lists/geomag/AK.txt"

  /**
   * The current sunspot activity.  Or None for not available
   */
  def getCurrentLevel() = {
    var levelOpt: Option[Int] = None

    try {
      // This is all read lazily
      Option((new URL(url)).openStream).foreach {
        using(_) { stream =>
          val lines = io.Source.fromInputStream(stream).getLines

          /*
       * the line we are interested in looks like:
       * Planetary(estimated Ap)      6     4     1     0     1     1     2     2     0
       */

          lines.filter(_.startsWith("Planetary")).foreach { l =>
            var candidateStr = l.split(')')(1) // Pull out everything after the close paren

            if (testing)
              candidateStr += " 4"

            // Convert to ints
            val candidates = candidateStr.split(' ').filter(!_.isEmpty).map(_.toInt)

            // Find the last entry != to -1 (which means not available)
            val last = candidates.reverse.find(_ != -1)
            if (last.isDefined)
              levelOpt = last
          }
        }
      }
    } catch {
      case ex: IOException =>
        println(s"Ignoring sunspot IOException: $ex")
      case ex: Exception =>
        AnalyticsService.reportException("solar_levels_failed", ex)
    }
    levelOpt
  }

  /// cheezy unit test
  def main(args: Array[String]) {
    println("Current sunspot levels: " + getCurrentLevel)
  }
}