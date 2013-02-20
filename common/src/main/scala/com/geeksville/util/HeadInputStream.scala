package com.geeksville.util

import java.io._

/// An input stream wrapper which will return maxBytes of data
class HeadInputStream(in: InputStream, maxBytes: Int) extends FilterInputStream(in) {
  var numLeft = maxBytes

  /// @return number of chars that can be read at this time (or -1 for eof)
  private def decLeft(d: Int) = {
    assert(d >= 0)

    val m = math.min(numLeft, d)

    numLeft -= m

    if (m == 0)
      -1 // eof
    else
      m
  }

  override def available = math.min(super.available, numLeft)

  /// We don't support mark (for now)
  override def markSupported = false

  override def read() = {
    val m = decLeft(1)
    if (m > 0)
      super.read()
    else
      m
  }

  override def read(b: Array[Byte], off: Int, len: Int) = {
    val m = decLeft(len)
    if (m > 0)
      super.read(b, off, m)
    else
      m
  }
}