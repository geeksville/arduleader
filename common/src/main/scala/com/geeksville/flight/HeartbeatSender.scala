package com.geeksville.flight

import com.geeksville.flight._
import scala.concurrent.duration._
import scala.language.postfixOps
import org.mavlink.messages.ardupilotmega._
import org.mavlink.messages._
import java.util.GregorianCalendar
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.SendYoungest
import com.geeksville.util.ThreadTools._

/**
 * A mixin that adds periodic sending of heartbeats
 *
 */
trait HeartbeatSender extends InstrumentedActor with VehicleSimulator {
  import context._

  // Send a heartbeat every few seconds 
  val heartbeatSender = context.system.scheduler.schedule(1 seconds, 3 seconds) { () =>
    //self.log.debug("Sending heartbeat")
    handlePacket(heartbeat)
  }

  override def postStop() {
    log.debug("cancelling heartbeat sender")
    heartbeatSender.cancel()
    super.postStop()
  }
}

