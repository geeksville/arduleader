package com.geeksville.ftdi

import java.io.InputStream

class FtdiInputStream(val dev: FtdiDevice) extends InputStream {
  override def read() = dev.read
  override def available() = 1 // FIXME, we lie
}