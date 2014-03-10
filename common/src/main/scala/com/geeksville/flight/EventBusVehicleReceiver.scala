package com.geeksville.flight

import com.geeksville.mavlink.HeartbeatMonitor
import com.geeksville.mavlink.MavlinkEventBus

/**
 * This is a mixin that can be added to VehicleClient if you'd like to automatically listen to the global event bus to find packets
 * destined to this vehicle.
 */
trait EventBusVehicleReceiver extends VehicleClient {
  // Default to listening to all traffic until we know the id of our vehicle
  // This lets the vehicle model receive messages from its vehicle...
  private var subscriber = MavlinkEventBus.subscribe(self, targetOverride.getOrElse(-1))

  override protected def onHeartbeatFound() {
    if (!targetOverride.isDefined) {
      // We didn't previously have any particular sysId filter installed.  Now that we know our vehicle
      // we can be more selective.  Resubscribe with the new system id
      MavlinkEventBus.removeSubscription(subscriber)
      subscriber = MavlinkEventBus.subscribe(self, targetSystem)
    }
    super.onHeartbeatFound()
  }

  override def postStop() {
    MavlinkEventBus.removeSubscription(subscriber)
    super.postStop()
  }
}