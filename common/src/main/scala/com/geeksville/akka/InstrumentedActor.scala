package com.geeksville.akka

import akka.actor.Actor
import akka.actor.ActorLogging

/*
object Context {
  def system = MockAkka
}
*/

object InstrumentedActor {
  type Receiver = PartialFunction[Any, Unit]
}

/**
 * Try to make scala actors look as much like akka actors as possible
 */
trait InstrumentedActor extends DebuggableActor {
  import InstrumentedActor._

  /**
   * The replacement for the akka receive method
   */
  def onReceive: Receiver

  override def receive = onReceive

}