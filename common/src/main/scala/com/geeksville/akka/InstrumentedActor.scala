package com.geeksville.akka

import scala.actors.Actor
import com.geeksville.logback.Logging

object Context {
  def system = MockAkka
}

object PoisonPill

/**
 * Try to make scala actors look as much like akka actors as possible
 */
trait InstrumentedActor extends Actor with Logging {

  type Receiver = PartialFunction[Any, Unit]

  private var isDead = false

  // Akka calls it log
  def log = logger

  // Approximately the same
  def self = this

  // Called context in Akka, but that word is heavily used in android so I call it acontext instead (for akka context)
  def acontext = Context

  def isTerminated = isDead

  /// For debugging it is sometimes useful to peek at this
  def debugMailboxSize = mailboxSize

  /**
   * The replacement for the akka receive method
   */
  def onReceive: Receiver

  def myReceive: Receiver = {
    case PoisonPill =>
      isDead = true
      postStop()
      exit()
  }

  private def onUnhandled: Receiver = {
    case x @ _ =>
      // log.debug("Unhandled: " + x)
      ;
  }

  def act() {
    log.info("Actor running: " + this)
    loop {
      if (mailboxSize >= 64)
        log.warn("getting behind %s (%d messages)".format(this, mailboxSize))

      try {
        react(myReceive.orElse(onReceive).orElse(onUnhandled))
      } catch {
        case ex: Exception =>
          log.error("Actor exception: " + ex)
      }
    }
  }

  def postStop() {
    log.info("Actor terminated: " + this)
  }
}