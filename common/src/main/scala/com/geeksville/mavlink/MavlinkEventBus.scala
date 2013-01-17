package com.geeksville.mavlink

import org.mavlink.messages.MAVLinkMessage
import com.geeksville.akka.EventStream
import com.geeksville.akka.InstrumentedActor

object MavlinkEventBus extends EventStream {

  def subscribe(a: InstrumentedActor, sysId: Int) = {
    def interested(ev: Any) = ev match {
      case m: MAVLinkMessage => m.sysId == sysId
      case _ => false
    }

    super.subscribe(a, interested _)
  }
}

