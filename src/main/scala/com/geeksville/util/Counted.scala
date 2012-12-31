package com.geeksville.util

/// A helper class that calls a callback every nth invocation
///
/// To use: throttled = new Counted(1000); throttled { i => some code }
class Counted(val interval: Int) {
  private var count = 0

  def apply(fn: Int => Unit) {
    count += 1
    if (count % interval == 0)
      fn(count)
  }
}