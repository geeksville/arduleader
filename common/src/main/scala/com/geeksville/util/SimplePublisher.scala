package com.geeksville.util

import scala.collection.mutable.Subscriber

/**
 * Simplified version of scala.collection.mutable.Publisher which is much
 *  more efficient.
 */
trait SimplePublisher[Evt] {

  type Pub <: SimplePublisher[Evt]
  type Sub = Subscriber[Evt, Pub]

  /**
   * The publisher itself of type `Pub'. Implemented by a cast from `this' here.
   *  Needs to be overridden if the actual publisher is different from `this'.
   */
  protected val self: Pub = this.asInstanceOf[Pub]

  private var subscribers = collection.immutable.ListSet.empty[Sub]

  def subscribe(sub: Sub) { subscribers += sub }
  def removeSubscription(sub: Sub) { subscribers -= sub }
  def removeSubscriptions() { subscribers = collection.immutable.ListSet.empty }

  def publish(event: Evt) {
    subscribers.foreach(_.notify(self, event))
  }

  /**
   * Checks if two publishers are structurally identical.
   *
   *  @return true, iff both publishers contain the same sequence of elements.
   */
  override def equals(obj: Any): Boolean = obj match {
    case that: SimplePublisher[_] => subscribers == that.subscribers
    case _ => false
  }
}
