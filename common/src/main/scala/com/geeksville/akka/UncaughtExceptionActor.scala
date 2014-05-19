package com.geeksville.akka

import akka.actor._
import com.geeksville.util.AnalyticsService

/**
 * For uncaught exceptions report via our analytics plugin.
 *
 * No need to mixin this actor directly - you probably want DebuggableActor instead
 */
trait UncaughtExceptionActor extends Actor {
  self: ActorLogging =>

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    log.error(reason, "Unhandled exception for message: {}", message)
    AnalyticsService.reportException(message.getOrElse("unknown").toString, reason)
  }
}

