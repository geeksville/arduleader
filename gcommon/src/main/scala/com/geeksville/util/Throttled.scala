package com.geeksville.util

/// A helper class that calls a callback _at most_ every
/// minIntervalMsec. 
///
/// To use: throttled = new Throttled(1000); throttled { some code }
class Throttled(minIntervalMsec: Int) {
  private var lasttimeMsec = 0L

  def apply(fn: => Unit) {
    val now = System.currentTimeMillis

    val span = now - lasttimeMsec
    if (span >= minIntervalMsec || span < 0) {
      fn
      lasttimeMsec = now
    }
  }
}