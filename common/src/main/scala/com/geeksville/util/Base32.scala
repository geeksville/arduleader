package com.geeksville.util

/**
 * Utilities for encoding in base32
 */
object Base32 {

  private val symbols = ('a' to 'z') ++ ('2' to '7')
  private val symToVal = Map(symbols.zipWithIndex: _*)

  def encode(nIn: Long) = {
    var n = nIn
    var out = List[Char]()
    while (n != 0) {
      val c = symbols((n & 0x1f).toInt)
      n = (n >> 5) & 0x07ffffffffffffffL
      out = c :: out
    }
    out.mkString
  }

  def decode(s: String) = {
    var n = 0L
    s.foreach { c =>
      n = (n << 5) | symToVal(c)
    }
    n
  }

  /*
  def main(a: Array[String]) {
    println(encode(4403L))
    println(encode(1234567890))
    println(decode(encode(4403L)))
    println(decode(encode(1234567890)))
  }
  * */
}