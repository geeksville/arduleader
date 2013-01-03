package com.geeksville.util

import java.io._

/**
 * A debugging tool to let you watch bytes as they are read
 */
class DebugInputStream(s: InputStream) extends FilterInputStream(s) {
  override def read() = {
    val b = super.read()
    // if (b != -1)
    printf("Rx %02x\n", b)
    b
  }
}