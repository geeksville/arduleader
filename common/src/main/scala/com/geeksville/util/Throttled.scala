package com.geeksville.util

/// A helper class that calls a callback _at most_ every
/// minIntervalMsec. 
///
/// To use: throttled = new Throttled(1000); throttled { some code }
class Throttled(minIntervalMsec: Int) {
  private var lasttimeMsec = 0L

  def apply(fn: () => Unit) {
    val now = System.currentTimeMillis

    val span = now - lasttimeMsec
    if (span >= minIntervalMsec || span < 0) {
      fn()
      lasttimeMsec = now
    }
  }

  /**
   * Use this variant to be informed of how many msecs have passed since the last call of callback
   */
  def apply(fn: Long => Unit) {
    val now = System.currentTimeMillis

    val span = now - lasttimeMsec
    if (span >= minIntervalMsec || span < 0) {
      fn(span)
      lasttimeMsec = now
    }
  }
}

/**
 * Throttle - only invoking callback if value has changed to a different delta sized bucket.
 */
class ThrottleByBucket(bucketSize: Int) {
  private var lastVal = 0

  def apply(newVal: Int)(fn: Int => Unit) {
    val oldBucket = lastVal / bucketSize
    val newBucket = newVal / bucketSize

    if (oldBucket != newBucket) {
      fn(newVal)
      lastVal = newVal
    }
  }
}

