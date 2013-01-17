package com.geeksville.mavlink

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import LogIncomingMavlink._
import scala.concurrent._
import scala.concurrent.duration._
import com.geeksville.akka.Cancellable

/**
 * Watches for arrival of a heartbeat, if we don't see one we print an error message
 */
class HeartbeatMonitor extends InstrumentedActor {

  private case object WatchdogExpired

  private var timer: Option[Cancellable] = None
  private var mySysId: Option[Int] = None

  def onReceive = {
    case msg: msg_heartbeat =>
      resetWatchdog(msg.sysId)

    case WatchdogExpired =>
      mySysId.foreach { id => log.error("Lost contact with sysId " + id) }
      mySysId = None
  }

  private def resetWatchdog(sysId: Int) {
    if (!mySysId.isDefined) {
      mySysId = Some(sysId)
      log.info("Contact established with sysId " + sysId)
    }
    cancelWatchdog()

    timer = Some(context.system.scheduler.scheduleOnce(10 seconds, self, WatchdogExpired))
  }

  private def cancelWatchdog() {
    timer.foreach(_.cancel)
    timer = None
  }
}

