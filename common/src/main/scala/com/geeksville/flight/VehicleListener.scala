package com.geeksville.flight

import com.geeksville.akka.InstrumentedActor

/**
 * Used to eavesdrop on location/state changes for our vehicle
 */
abstract class VehicleListener(val v: VehicleModel) extends InstrumentedActor {

  val subscription = v.eventStream.subscribe(this)

  override def postStop() {
    v.eventStream.removeSubscription(subscription)
    super.postStop()
  }

}