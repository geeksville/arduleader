package com.geeksville.mavlink

import org.mavlink.messages.MAVLinkMessage

/**
 * published on our eventbus when a new packet arrives from the outside world
 */

/**
 * Common code for any gateway that receives mavlink messages into our actor system
 */
trait MavlinkReceiver extends CanSendMavlink {

  /**
   * For now we pipe all our notifications through the system event stream - we might refine this later
   */
  val destEventBus = MavlinkEventBus

  /**
   * Where msg is a SendYoungest or a MAVLinkMessage
   */
  override protected def handlePacket(msg: Any) {
    destEventBus.publish(msg)
  }

}