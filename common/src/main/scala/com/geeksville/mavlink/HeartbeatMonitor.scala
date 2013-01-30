package com.geeksville.mavlink

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import LogIncomingMavlink._
import scala.concurrent._
import scala.concurrent.duration._
import com.geeksville.akka.Cancellable
import com.geeksville.akka.EventStream

case class MsgHeartbeatLost(id: Int)
case class MsgHeartbeatFound(id: Int)

/**
 * Watches for arrival of a heartbeat, if we don't see one we print an error message
 */
class HeartbeatMonitor extends InstrumentedActor {

  private case object WatchdogExpired

  val eventStream = new EventStream

  private var timer: Option[Cancellable] = None
  private var mySysId: Option[Int] = None

  var customMode: Option[Int] = None

  def hasHeartbeat = mySysId.isDefined

  def onReceive = {
    case msg: msg_heartbeat =>
      val oldVal = customMode.getOrElse(-1)
      val newVal = msg.custom_mode.toInt
      customMode = Some(newVal)
      if (oldVal != newVal)
        onModeChanged(newVal)
      resetWatchdog(msg.sysId)

    case WatchdogExpired =>
      mySysId.foreach { id => eventStream.publish(MsgHeartbeatLost(id)) }
      mySysId = None
      onHeartbeatLost()
  }

  override def postStop() {
    cancelWatchdog()
    super.postStop()
  }

  protected def onModeChanged(m: Int) {
    log.error("Received new mode: " + m)
  }

  protected def onHeartbeatLost() {
    log.error("Lost heartbeat")
  }

  protected def onHeartbeatFound() {
    mySysId.foreach { id => log.info("Contact established with sysId " + id) }
  }

  private def resetWatchdog(sysId: Int) {
    if (!mySysId.isDefined) {
      mySysId = Some(sysId)
      onHeartbeatFound()
      eventStream.publish(MsgHeartbeatFound(sysId))
    }
    cancelWatchdog()

    timer = Some(context.system.scheduler.scheduleOnce(10 seconds, self, WatchdogExpired))
  }

  private def cancelWatchdog() {
    timer.foreach(_.cancel())
    timer = None
  }
}

