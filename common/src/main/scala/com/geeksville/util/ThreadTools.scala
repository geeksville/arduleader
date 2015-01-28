package com.geeksville.util

import scala.language.implicitConversions

object ThreadTools {
  /**
   * Generate runnables
   * (Note if you findyourself tempted to change handler to a => Any you are probably making a mistake that won't work well with reference arguments
   */
  implicit def toRunable(handler: () => Any) = new Runnable {
    override def run() = handler()
  }

  /// Run a bit of code in a background thread - the caller will need to call start
  def createDaemon(name: String)(block: () => Unit): Thread = {
    val t = new Thread(block, name)

    reportUncaughtExceptions(t)
    t.setDaemon(true)
    t
  }

  /// Run a bit of code in a forground thread, it starts running immediately
  def start(name: String)(block: () => Unit): Thread = {
    val t = new Thread(block, name)

    reportUncaughtExceptions(t)
    t.start
    t
  }

  def reportUncaughtExceptions(t: Thread) {
    setUncaughtExceptionHandler {
      case (t, ex) =>
        AnalyticsService.reportException(s"Uncaught in $t", ex)
    }
  }

  /// Try to run some closure, but if an expected exception type occurs then retry up to a small number of times
  def retryOnException[ExceptionType <: Exception, ResType](numRetries: => Int)(block: => ResType): ResType =
  {
    var lastEx: ExceptionType = null

    for(i <- 0 until numRetries)
      try {
        return block
      } catch {
        case ex: ExceptionType =>
          lastEx = ex
          AnalyticsService.reportException("Retrying ", ex)
      }

    throw lastEx
  }

  /// Ignore exceptions (with a warning).  Usage: catchIgnore { some code }
  def catchIgnore[ResType](block: => ResType) =
    {
      try {
        block
      } catch {
        case ex: Exception =>
          AnalyticsService.reportException("Ignoring Exception", ex)
      }
    }

  /// Ignore exceptions (with a warning).  Usage: catchIgnore { some code }
  def catchOrElse[ResType](elseValue: => ResType = Unit)(block: => ResType) =
    {
      try {
        block
      } catch {
        case ex: Exception =>
          AnalyticsService.reportException("Returning else value", ex)
          elseValue
      }
    }

  /// Ignore exceptions silently.  Usage: catchSilently { some code }
  def catchSilently[ResType](block: => ResType) =
    {
      try {
        block
      } catch {
        case ex: Exception =>
      }
    }

  /// Install an uncaught exception handler
  def setUncaughtExceptionHandler(handler: (Thread, Throwable) => Unit) {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      def uncaughtException(t: Thread, e: Throwable) = handler(t, e)
      //System.out.println("*****Yeah, Caught the Exception*****");
      // e.printStackTrace(); // you can use e.printStackTrace ( printstream ps )
    })
  }
}
