package com.geeksville.util

import scala.collection.mutable._

/// A buffer that guarantees a maximum # of elements
/// If we exceed that count we discard old elements
/// Note: Not actually implemented as a ring buffer 
/// but other than implicitly dropping the oldest items it is similar
class RingBuffer[A](val maxCount: Int) extends BufferProxy[A] {
  override def self = new ListBuffer[A]

  override def +=(x: A) = { makeSpace(); super.+=(x) }

  private def makeSpace() {
    val todrop = math.max(length - maxCount + 1, 0)
    trimStart(todrop)
  }
}