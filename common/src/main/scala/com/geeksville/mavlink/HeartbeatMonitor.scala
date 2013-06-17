package com.geeksville.mavlink

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega._
import LogIncomingMavlink._
import scala.concurrent._
import scala.concurrent.duration._
import com.geeksville.akka.Cancellable
import com.geeksville.akka.EventStream
import org.mavlink.messages.MAV_TYPE

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
  var baseMode: Option[Int] = None

  /// A MAV_TYPE vehicle code
  var vehicleType: Option[Int] = None

  def hasHeartbeat = mySysId.isDefined

  def onReceive = {
    case msg: msg_heartbeat =>
      // We don't care about the heartbeats from a GCS
      val typ = msg.`type`
      if (typ != MAV_TYPE.MAV_TYPE_GCS) {
        val oldVal = customMode
        val oldBase = baseMode
        val newVal = msg.custom_mode.toInt
        customMode = Some(newVal)
        baseMode = Some(msg.base_mode)

        val oldVehicle = vehicleType
        vehicleType = Some(typ)
        if (oldVal != customMode || oldVehicle != vehicleType || baseMode != oldBase)
          onModeChanged(newVal)
        resetWatchdog(msg.sysId)
      }

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

    timer = Some(acontext.system.scheduler.scheduleOnce(10 seconds, self, WatchdogExpired))
  }

  private def cancelWatchdog() {
    timer.foreach(_.cancel())
    timer = None
  }
}

