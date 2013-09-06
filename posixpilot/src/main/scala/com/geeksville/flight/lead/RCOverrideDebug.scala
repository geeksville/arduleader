package com.geeksville.flight.lead

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import com.geeksville.mavlink.MavlinkEventBus

class RCOverrideDebug(sysId: Int) extends InstrumentedActor {
  MavlinkEventBus.subscribe(self, sysId)

  var expectedSend = -1L

  def onReceive = {
    case msg: msg_rc_channels_override =>
      val sent = ((msg.chan4_raw & 0xffffffffL) << 48L) | ((msg.chan3_raw & 0xffffffffL) << 32L) | ((msg.chan2_raw & 0xffffffffL) << 16L) | (msg.chan1_raw & 0xffffffffL)
      val now = System.currentTimeMillis()
      if (sent == expectedSend) {
        log.info(s"RC override latency seq=${msg.sequence} sent=$sent " + (now - sent) + " ms")
        expectedSend = -1
      }
  }
}

