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
import akka.actor.Actor
import akka.actor.Cancellable

// Heartbeat sender expects its mixin to be able to send this message
case class SendMessage(msg: MAVLinkMessage)

/**
 * A mixin that adds periodic sending of heartbeats
 *
 */
trait HeartbeatSender extends Actor with VehicleSimulator {
  // FIXME - the following should work but does not: self: VehicleSimulator =>

  import context._

  // Send a heartbeat every few seconds 
  var heartbeatSender = {
    val msg = SendMessage(heartbeat)
    context.system.scheduler.schedule(1 seconds, 3 seconds, self, msg)
  }

  override def postStop() {
    //println("cancelling heartbeat sender")
    heartbeatSender.cancel()
    super.postStop()
  }
}

