package com.geeksville.flight

import java.io.OutputStream
import java.io.PrintWriter
import java.util.GregorianCalendar
import java.util.TimeZone
import java.util.Locale
import java.util.Calendar

/**
 * Write ICS format files
 */
class IGCWriter(outs: OutputStream, val pilotName: String, val gliderType: String, val pilotId: String) {
  import IGCWriter._

  private val out = new PrintWriter(outs)
  private val cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"))

  emitProlog()

  def close() {
    out.close()
  }

  /**
   *
   * @param time
   *            UTC time of this fix, in milliseconds since January 1, 1970.
   * @param latitude
   * @param longitude
   *
   *            sect 4.1, B=fix plus extension data mentioned in I
   */
  def emitPosition(l: Location, timeusec: Long) {
    // B
    // HHMMSS - time UTC
    // DDMMmmmN(or S) latitude
    // DDDMMmmmE(or W) longitude
    // A (3d valid) or V (2d only)
    // PPPPP pressure altitude (00697 in this case)
    // GGGGG alt above WGS ellipsode (00705 in this case)
    // GSP is 000 here (ground speed in km/hr)
    // B1851353728534N12151678WA0069700705000

    // Get time in UTC
    cal.setTimeInMillis(timeusec / 1000);

    val is3D = l.alt.isDefined

    val hours = cal.get(Calendar.HOUR_OF_DAY);
    val latitude = l.lat
    val longitude = l.lon

    // GSP / groundspeed.  Convert from m/s to km/hr
    val vel = (l.velocity.getOrElse(0.0) * 3.6).toInt

    val line = "B%02d%02d%02d%s%s%c%05d%05d%03d".formatLocal(Locale.US, hours, cal
      .get(Calendar.MINUTE), cal.get(Calendar.SECOND),
      degreeStr(latitude, true), degreeStr(longitude, false),
      if (is3D) 'A' else 'V', l.alt.getOrElse(0.0).toInt,
      l.alt.getOrElse(0.0).toInt,
      vel)
    out.println(line);

    // Don't store vertical speed info until I can find an example data
    // file.
    if (false) {
      /*
			if (!hasJRecord) {
				// less frequent extension - vario data
				out.println("J010812VAR");
				hasJRecord = true;
			}

			out.format(Locale.US, "K%02d%02d%02d%03d", hours,
					cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
					(int) vspd * 10);
			out.println();
			* 
			*/
    }
  }

  def emitProlog() {

    val versionString = "DShare"
    out.println("AXGG" + versionString); // AFLY06122 - sect 3.1, A=mfgr info,
    // mfgr=FLY, serial num=06122

    // sect 3.3.1, H=file header
    val dstr = "HFDTE%02d%02d%02d".formatLocal(Locale.US,
      cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
      (cal.get(Calendar.YEAR) - 1900) % 100); // date

    out.println(dstr); // date

    out.println("HFFXA100"); // accuracy in meters - required
    out.println("HFPLTPILOT:" + pilotName); // pilot (required)
    out.println("HFGTYGLIDERTYPE:" + gliderType); // glider type (required)
    out.println("HFGIDGLIDERID:" + pilotId); // glider ID required
    out.println("HFDTM100GPSDATUM:WGS84"); // datum required - must be wgs84
    //out.println("HFGPSGPS:" + android.os.Build.MODEL); // info on gps manufacturer
    out.println("HFRFWFIRMWAREVERSION:" + versionString); // sw version of app
    out.println("HFRHWHARDWAREVERSION:" + versionString); // hw version
    out.println("HFFTYFRTYPE:Geeksville,Droneshare"); // required: manufacturer
    // (me) and model num

    // sect 3.4, I=fix extension list
    out.println("I013638GSP"); // one extension, starts at byte 36, ends at
    // 38, extension type is ground speed (was TAS)
  }
}

object IGCWriter {
  /**
   * Return a degress in IGC format
   *
   * @param degIn
   * @return
   */
  def degreeStr(degIn: Double, isLatitude: Boolean) = {
    val isPos = degIn >= 0;
    val dirLetter = if (isLatitude) (if (isPos) 'N' else 'S') else (if (isPos) 'E' else 'W');

    var deg = math.abs(degIn);
    val minutes = 60 * (deg - Math.floor(deg));
    deg = math.floor(deg);
    val minwhole = minutes.toInt;
    val minfract = ((minutes - minwhole) * 1000).toInt;

    // DDMMmmmN(or S) latitude
    // DDDMMmmmE(or W) longitude
    ((if (isLatitude) "%02d" else "%03d")
      + "%02d%03d%c").formatLocal(Locale.US, deg.toInt, minwhole, minfract, dirLetter);
  }
}