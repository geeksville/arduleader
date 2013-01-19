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

  def context = Context

  def isTerminated = isDead

  /**
   * The replacement for the akka receive method
   */
  def onReceive: Receiver

  def myReceive: Receiver = {
    case PoisonPill =>
      isDead = true
      exit()
  }

  def act() {
    log.info("Actor running: " + this)
    loop {
      react(myReceive.orElse(onReceive))
    }
  }

  def postStop() {
    log.error("postStop not yet implemented")
  }
}