package com.geeksville.mavlink

import org.mavlink.messages.MAVLinkMessage

/**
 * published on our eventbus when a new packet arrives from the outside world
 */

/**
 * Common code for any gateway that receives mavlink messages into our actor system
 */
trait MavlinkReceiver {

  /**
   * For now we pipe all our notifications through the system event stream - we might refine this later
   */
  // val destEventBus = MavlinkEventBus

  /**
   * Where msg is a SendYoungest or a MAVLinkMessage
   */
  protected def handleIncomingPacket(msg: Any) {
    assert(msg != null)
    MavlinkEventBus.publish(msg)
  }

}

/**
 * Use this mixin if you want any mavlink sent by an object to be automatically published on the local eventbus
 */
trait SendsMavlinkToEventbus extends MavlinkReceiver with CanSendMavlink {
  /// Just forward anything we send to the event bus
  override def handlePacket(a: Any) = handleIncomingPacket(a)
}