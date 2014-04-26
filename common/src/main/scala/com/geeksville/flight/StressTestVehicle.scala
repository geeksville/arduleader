package com.geeksville.flight

import scala.concurrent.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.util.Counted
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.logback.Logging
import com.geeksville.akka.MockAkka
import akka.actor.ActorRef

/**
 * Source a bunch of fake traffic - direct to a port
 */
class DirectSending(sysId: Int) extends InstrumentedActor with VehicleSimulator with HeartbeatSender {

  override def systemId = sysId

  var sendingInterface: Option[ActorRef] = None

  override def handlePacket(m: Any) = {
    m match {
      case SendYoungest(m) =>
        sendingInterface.foreach(_ ! m)
      case x @ _ =>
        sendingInterface.foreach(_ ! x)
    }
  }

  def onReceive = {
    case l: Location =>
  }
}

/**
 * Source a bunch of fake traffic - direct to a port
 */
class StressTestVehicle(sysId: Int) extends DirectSending(sysId) {
  import context._

  val scheduled = context.system.scheduler.schedule(1 seconds, 100 milliseconds)(sendPackets)

  override def postStop() {
    log.debug("cancelling stress sender")
    scheduled.cancel()
    super.postStop()
  }

  def sendPackets() {
    //log.debug("Sending packets")

    throw new Exception("Busted with classic akka")
    /*
    sendingInterface.foreach { iface =>
      if (iface.debugMailboxSize < 16) {
        val l = new Location(12, 14, Some(0))
        iface ! sendMavlink(makePosition(l))
        iface ! sendMavlink(makeGPSRaw(l))
      }
    }
    */
  }
}
