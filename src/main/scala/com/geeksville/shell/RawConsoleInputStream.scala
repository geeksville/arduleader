package com.geeksville.shell

import java.io._

class RawConsoleInputStream extends InputStream {
  val reader = System.console.reader

  override def read() = reader.read()

  override def available() = if (reader.ready) 1 else 0
}