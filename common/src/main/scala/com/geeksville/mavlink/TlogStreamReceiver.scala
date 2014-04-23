package com.geeksville.mavlink

import java.io.BufferedInputStream
import java.io.InputStream

object TlogStreamReceiver {
  /**
   * Read from a tlog in the classpath, i.e. "/path/file.ext"
   */
  def open(s: InputStream, speedup: Double = 1.0, autoStart: Boolean = true) = {
    println(s"Opened $s " + s.available)
    new MavlinkStreamReceiver(s, tlogSpeedup = Some(speedup), autoStart = autoStart)
  }

}