package com.geeksville.mavlink

import org.mavlink.messages.MAVLinkMessage

/**
 * A small interface which is common to all objects that have a sendMavlink method (probably just VehicleSimulator)
 */
trait CanSendMavlink {
  /**
   * m must be a SendYoungest or a MAVLinkMessage
   */
  protected def handlePacket(m: Any)
}