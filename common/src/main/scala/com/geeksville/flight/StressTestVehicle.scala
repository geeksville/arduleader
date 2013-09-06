package com.geeksville.flight

import scala.concurrent.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.util.Counted
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.logback.Logging
import com.geeksville.akka.MockAkka

/**
 * Source a bunch of fake traffic - direct to a port
 */
class DirectSending(sysId: Int) extends VehicleSimulator with HeartbeatSender {

  override def systemId = sysId

  var sendingInterface: Option[InstrumentedActor] = None

  override def sendMavlink(m: MAVLinkMessage) = {
    sendingInterface.foreach(_ ! m)
  }

  override def sendMavlink(m: SendYoungest) = sendingInterface.foreach(_ ! m.msg)

  def onReceive = {
    case l: Location =>
  }
}

/**
 * Source a bunch of fake traffic - direct to a port
 */
class StressTestVehicle(sysId: Int) extends DirectSending(sysId) {

  val scheduled = acontext.system.scheduler.schedule(1 seconds, 100 milliseconds)(sendPackets)

  override def postStop() {
    log.debug("cancelling stress sender")
    scheduled.cancel()
    super.postStop()
  }

  def sendPackets() {
    //log.debug("Sending packets")

    sendingInterface.foreach { iface =>
      if (iface.debugMailboxSize < 16) {
        val l = new Location(12, 14, Some(0))
        iface ! sendMavlink(makePosition(l))
        iface ! sendMavlink(makeGPSRaw(l))
      }
    }
  }
}
