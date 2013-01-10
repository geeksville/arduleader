package com.geeksville.ftdi

import java.io.OutputStream

class FtdiOutputStream(val dev: FtdiDevice) extends OutputStream {
  override def write(c: Int) {
    val a = Array[Byte](c.toByte)
    write(a)
  }

  override def write(a: Array[Byte], off: Int, len: Int) {
    assert(off == 0)
    dev.write(a, len)
  }
}