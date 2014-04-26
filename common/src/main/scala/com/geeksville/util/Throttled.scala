package com.geeksville.util

/// A helper class that calls a callback _at most_ every
/// minIntervalMsec. 
///
/// To use: throttled = new Throttled(1000); throttled { some code }
/// @param minIntervalMsec use -1 to disable
class Throttled(minIntervalMsec: Int) {
  private var lasttimeMsec = 0L

  private var numVetoed = 0

  def isEnabled = minIntervalMsec != -1

  /**
   * A new event has occured, should it pass the throttle?
   */
  protected def isAllowed() = {
    val now = System.currentTimeMillis

    val span = now - lasttimeMsec
    if (span >= minIntervalMsec || span < 0) {
      lasttimeMsec = now
      true
    } else {
      numVetoed += 1
      false
    }
  }

  def apply(fn: () => Unit) {
    if (isEnabled && isAllowed)
      fn()
  }

  /**
   * Use this variant to be informed of how many msecs have passed since the last call of callback
   */
  def apply(fn: Long => Unit) {
    if (isEnabled) {
      val now = System.currentTimeMillis

      val span = now - lasttimeMsec
      if (span >= minIntervalMsec || span < 0) {
        fn(span)
        lasttimeMsec = now
      }
    }
  }

  /**
   * Call this version to find number of calls ignored since last time
   */
  def withIgnoreCount(fn: Int => Unit) {
    if (isEnabled isAllowed) {
      fn(numVetoed)
      numVetoed = 0
    }
  }
}

/**
 * Throttle - only invoking callback if value has changed to a different delta sized bucket.
 */
class ThrottleByBucket(bucketSize: Int) {
  private var lastVal = Int.MinValue

  def apply(newVal: Int)(fn: Int => Unit) {
    val oldBucket = lastVal / bucketSize
    val newBucket = newVal / bucketSize
    def delta = math.abs(newVal - lastVal)

    // Don't speak unless we move one full bucket away
    if (oldBucket != newBucket && delta >= bucketSize) {
      fn(newVal)
      lastVal = newVal
    }
  }
}

