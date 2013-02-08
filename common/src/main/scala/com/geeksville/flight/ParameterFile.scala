package com.geeksville.flight

import com.geeksville.logback.Logging
import java.text.SimpleDateFormat
import java.io._
import java.util.Date
class ParameterFile(out: OutputStream, header: String) {
  val pout = new PrintStream(out)

  pout.println("# " + header)

  def writeParams(params: Seq[VehicleMonitor#ParamValue]) {
    params.foreach { p =>
      pout.println(p.getId.get + "," + p.getValue.get)
    }
  }

  def close() {
    out.close()
  }
}

object ParameterFile extends Logging {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

  /// Allocate a filename in the spooldir
  def getFilename(spoolDir: File = new File("param-files")) = {
    if (!spoolDir.exists)
      spoolDir.mkdirs()

    val fname = dateFormat.format(new Date) + ".param"
    new File(spoolDir, fname)
  }

  def create(params: Seq[VehicleMonitor#ParamValue], file: File = getFilename()) = {
    // Preflight to make sure all entries are valid
    if (!params.forall { p => p.getId.isDefined && p.getValue.isDefined })
      throw new Exception("Some parameters are not valid, not safe to write to disk...")

    logger.info("Dumping parameters to " + file.getAbsolutePath)
    val out = new BufferedOutputStream(new FileOutputStream(file, true), 8192)
    val f = new ParameterFile(out, "Auto capture on " + new Date())
    f.writeParams(params)
    f.close()
  }
}