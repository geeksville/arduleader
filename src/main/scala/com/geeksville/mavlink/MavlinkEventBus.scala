package com.geeksville.mavlink

import akka.event._
import akka.actor.ActorRef

object MavlinkEventBus extends ActorEventBus with LookupClassification {
  type Event = MavlinkReceived

  /**
   * messages are classfied by their sysId
   */
  type Classifier = Int

  protected def mapSize() = 4

  protected def classify(event: Event): Classifier =
    event.message.sysId

  protected def publish(event: Event, subscriber: ActorRef) = {
    if (subscriber.isTerminated) unsubscribe(subscriber)
    else subscriber ! event
  }
}

