package com.geeksville.akka

import scala.collection.mutable.Publisher
import akka.actor.Actor
import akka.actor.ActorRef

/**
 * Similar to the classic akka EventBus but simplified
 */
class EventStream extends Publisher[Any] {

  private val publisher = new {}

  // Send to actor when our event comes in
  class Subscriber(val dest: ActorRef) extends Sub {
    override def notify(p: Pub, evt: Any) {
      if (!dest.isTerminated)
        dest ! evt
      else
        removeSubscription(this) // Our actor died - remove our filter
    }
  }

  /**
   * @param isInterested - if not specified we look at the actor's partial function to see what it understands
   */
  def subscribe(a: ActorRef, receiver: PartialFunction[Any, Unit]): Subscriber = subscribe(a, receiver.isDefinedAt _)

  /**
   * @param isInterested - if not specified we look at the actor's partial function to see what it understands
   */
  def subscribe(a: ActorRef, isInterested: Any => Boolean): Subscriber = {
    val isInt = if (isInterested != null)
      isInterested
    else { x: Any => true }

    val sub = new Subscriber(a)
    super.subscribe(sub, isInt)
    sub
  }

  /**
   * @param isInterested - if not specified we look at the actor's partial function to see what it understands
   */
  def subscribe(a: Actor): Subscriber = subscribe(a.self, a.receive)

  override def publish(x: Any) { super.publish(x) }
}
