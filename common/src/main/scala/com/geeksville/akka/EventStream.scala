package com.geeksville.akka

import scala.collection.mutable.Publisher

class EventStream extends Publisher[Any] {

  private val publisher = new {}

  // Send to actor when our event comes in
  private class Subscriber(val dest: InstrumentedActor) extends Sub {
    override def notify(p: Pub, evt: Any) {
      if (!dest.isTerminated)
        dest ! evt
    }
  }

  def subscribe(a: InstrumentedActor, isInterested: Any => Boolean) {
    val sub = new Subscriber(a)
    super.subscribe(sub, isInterested)
  }

  override def publish(x: Any) { super.publish(x) }
}