package com.geeksville.util

import scala.io.Source
import java.io.{ InputStream, Reader, InputStreamReader }
import Source.DefaultBufSize

/**
 * A source that just reads a reader - with no buffering
 */
class UnbufferedSource(reader: Reader) extends Source {
  private val isWindows = false // FIXME - add back if we ever care
  private val eofChar: Int = (if (isWindows) 'Z' else 'D') - 'A' + 1

  override val iter = {
    Iterator continually (reader.read()) takeWhile { c => c != -1 && c != eofChar } map (_.toChar)
  }
}

/**
 * A source that just reads a input stream - with no buffering
 */
class UnbufferedStreamSource(reader: InputStream) extends Source {
  override val iter = {
    Iterator continually (reader.read()) takeWhile { c => c != -1 } map (_.toChar)
  }
}
