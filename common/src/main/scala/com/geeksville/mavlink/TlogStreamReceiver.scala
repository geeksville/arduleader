package com.geeksville.mavlink

import java.io.BufferedInputStream
import java.io.InputStream

object TlogStreamReceiver {
  /**
   * Read from a tlog in the classpath, i.e. "/path/file.ext"
   */
  def open(s: InputStream) = {
    println(s"Opened $s " + s.available)
    new MavlinkStreamReceiver(s, tlogSpeedup = Some(1.0))
  }

}