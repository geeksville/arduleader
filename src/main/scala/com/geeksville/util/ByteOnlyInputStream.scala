package com.geeksville.util

import java.io._

/**
 * Read an input stream byte by byte (never doing more efficient multibyte reads).
 * Useful for working with buggy RXTX code
 */
class ByteOnlyInputStream(val s: InputStream) extends InputStream {
  override def read() = s.read()

  override def available() = s.available()
}