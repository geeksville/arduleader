package com.geeksville.flight

import com.geeksville.logback.Logging
import java.text.SimpleDateFormat
import java.io._
import java.util.Date
import com.geeksville.util.FileTools
import com.geeksville.util.Using._

class ParameterFile(out: OutputStream, header: String) {
  val pout = new PrintStream(out)

  pout.println("# " + header)

  def writeParams(params: Seq[VehicleModel#ParamValue]) {
    params.foreach { p =>
      pout.println(p.getId.get + "," + p.getValue.get)
    }
  }

  def close() {
    out.close()
  }
}

object ParameterFile extends Logging {

  /// Allocate a filename in the spooldir
  def getFilename(spoolDir: File = new File("param-files")) = FileTools.getDatestampFilename(".param", spoolDir)

  def create(params: Seq[VehicleModel#ParamValue], file: File = getFilename()) = {
    // Preflight to make sure all entries are valid
    if (!params.forall { p => p.getId.isDefined && p.getValue.isDefined })
      throw new Exception("Some parameters are not valid, not safe to write to disk...")

    logger.info("Dumping parameters to " + file.getAbsolutePath)
    using(new BufferedOutputStream(new FileOutputStream(file, true), 8192)) { out =>
      val f = new ParameterFile(out, "Auto capture on " + new Date())
      f.writeParams(params)
    }
  }
}