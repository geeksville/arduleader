package com.geeksville.util

/**
 * Extend/implement this trait for the analytics engine of your choice
 */
trait AnalyticsAdapter {
  def reportException(msg: String, ex: Throwable)
}

/**
 * Just reports exceptions via stdout
 */
class SimpleAnalyticsAdapter extends AnalyticsAdapter {
  def reportException(msg: String, ex: Throwable) {
    println(s"Exception occurred (msg=$msg): $ex")
  }
}

object AnalyticsService {

  /**
   * Assign to this to select a non standard handler
   */
  var handler: AnalyticsAdapter = new SimpleAnalyticsAdapter

  /**
   * Report an exception to our analytics
   */
  def reportException(msg: String, ex: Throwable) = handler.reportException(msg, ex)
}