package com.geeksville.akka

import scala.collection.mutable.Publisher

class EventStream extends Publisher[Any] {

  private val publisher = new {}

  // Send to actor when our event comes in
  class Subscriber(val dest: InstrumentedActor) extends Sub {
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
  def subscribe(a: InstrumentedActor, isInterested: Any => Boolean = null) = {
    val isInt = if (isInterested != null)
      isInterested
    else { evt => a.onReceive.isDefinedAt(evt) }

    val sub = new Subscriber(a)
    super.subscribe(sub, isInt)
    sub
  }

  override def publish(x: Any) { super.publish(x) }
}