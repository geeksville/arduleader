package com.geeksville.flight.lead

import java.io._
import java.text.SimpleDateFormat
import scala.io.Source

case class Location(lat: Double, lon: Double, alt: Double, time: Long)

/**
 * Read IGC file locations
 *
 * Borrowed from my old Gaggle java code
 */
class IGCReader(filename: String) {

  private val formater = new SimpleDateFormat("HHmmss");

  // FIXME - I bet this leaks a file descriptor
  val locations = Source.fromFile(filename).getLines.flatMap(toLocation).toSeq

  private def toLocation(line: String): Option[Location] = {
    // Ignore all other record types
    if (line.startsWith("B")) {
      // This is a position record
      // B
      // HHMMSS - time UTC
      // DDMMmmmN(or S) latitude
      // DDDMMmmmE(or W) longitude
      // A (3d valid) or V (2d only)
      // PPPPP pressure altitude (00697 in this case)
      // GGGGG alt above WGS ellipsode (00705 in this case)
      // GSP is 000 here (ground speed in km/hr)
      // B2109233921018N12239641WA0051600526000
      // int hr = Integer.parseInt(line.substring(1, 3));
      // int min = Integer.parseInt(line.substring(3, 5));
      // int sec = Integer.parseInt(line.substring(5, 7));

      val timestr = line.substring(1, 7);

      val latdeg = Integer.parseInt(line.substring(7, 9));
      val latmin = Integer.parseInt(line.substring(9, 11));
      val latminfract = Integer.parseInt(line.substring(11, 14)).toDouble;
      val latdir = line.charAt(14);

      val longdeg = Integer.parseInt(line.substring(15, 18));
      val longmin = Integer.parseInt(line.substring(18, 20));
      val longminfract = Integer.parseInt(line.substring(20, 23)).toDouble
      val longdir = line.charAt(23);

      val alt = Integer.parseInt(line.substring(25, 25 + 5));

      val lat = (latdeg + (latmin + latminfract / 1000) / 60.0) * (if (latdir == 'N') 1 else -1)

      val ltude = (longdeg + (longmin + longminfract / 1000) / 60.0) * (if (longdir == 'E') 1 else -1)

      // Date d = new Date(); // Claim the flight is happening now
      val d = formater.parse(timestr);

      // FIXME - we should also pay attention to the TZ and the date
      // stored in the file header

      val res = Location(lat, ltude, alt, d.getTime)

      // Log.d(TAG, "SimPos: " + lat + "," + ltude + "," + alt);
      return Some(res)
    } else
      None
  }
}