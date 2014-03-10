package com.geeksville.mavlink

import org.mavlink.messages.MAVLinkMessage

/**
 * A mixin for VehicleSimulator to enable sending on the global event bus
 */
trait EventBusVehicleSender extends CanSendMavlink {
  protected def sendMavlinkAlways(m: Any) {
    MavlinkEventBus.publish(m)
  }

}