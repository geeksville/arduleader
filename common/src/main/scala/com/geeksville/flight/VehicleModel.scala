package com.geeksville.flight

import com.geeksville.mavlink.HeartbeatMonitor
import org.mavlink.messages.ardupilotmega._
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.akka.MockAkka
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.collection.mutable.ArrayBuffer
import com.geeksville.util.Throttled
import com.geeksville.akka.EventStream
import org.mavlink.messages.MAV_TYPE
import com.geeksville.akka.Cancellable
import org.mavlink.messages.MAV_DATA_STREAM
import org.mavlink.messages.MAV_MISSION_RESULT
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashSet
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.mavlink.MavlinkStream
import com.geeksville.util.ThrottledActor

//
// Messages we publish on our event bus when something happens
//
case class MsgStatusChanged(s: String)
case object MsgSysStatusChanged
case class MsgRcChannelsChanged(m: msg_rc_channels_raw)
case class MsgModeChanged(m: String)

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleModel extends VehicleClient with WaypointModel with FenceModel {

  // We can receive _many_ position updates.  Limit to one update per second (to keep from flooding the gui thread)
  private val locationThrottle = new Throttled(1000)
  private val rcChannelsThrottle = new Throttled(500)
  private val sysStatusThrottle = new Throttled(5000)
  private val attitudeThrottle = new Throttled(100)

  var status: Option[String] = None
  var location: Option[Location] = None
  var batteryPercent: Option[Float] = None
  var batteryVoltage: Option[Float] = None
  var radio: Option[msg_radio] = None
  var numSats: Option[Int] = None
  var rcChannels: Option[msg_rc_channels_raw] = None
  var attitude: Option[msg_attitude] = None
  var vfrHud: Option[msg_vfr_hud] = None
  var globalPos: Option[msg_global_position_int] = None

  // We always want to see radio packets (which are hardwired for this sys id)
  val radioSysId = 51
  MavlinkEventBus.subscribe(VehicleModel.this, radioSysId)

  private def onLocationChanged(l: Location) {
    location = Some(l)

    locationThrottle { () =>
      eventStream.publish(l)
    }
  }

  /**
   * Return the best user visible description of altitude that we have (hopefully from a barometer)
   * In AGL
   */
  def bestAltitude = globalPos.map(_.relative_alt / 1000.0f).getOrElse(location.map(toAGL(_).toFloat).getOrElse(-999f))

  def currentMode = modeToString(customMode.getOrElse(-1))

  /**
   * The mode names we understand
   */
  def modeNames = modeToCodeMap.keys.toSeq.sorted :+ "unknown"

  private def onStatusChanged(s: String) { eventStream.publish(MsgStatusChanged(s)) }
  private def onSysStatusChanged() { sysStatusThrottle { () => eventStream.publish(MsgSysStatusChanged) } }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {

    case DoSetMode(m) =>
      setMode(m)

    case m: msg_attitude =>
      attitude = Some(m)
      attitudeThrottle { () => eventStream.publish(m) }

    case m: msg_rc_channels_raw =>
      rcChannels = Some(m)
      rcChannelsThrottle { () => eventStream.publish(MsgRcChannelsChanged(m)) }

    case m: msg_radio =>
      //log.info("Received radio from " + m.sysId + ": " + m)
      radio = Some(m)
      onSysStatusChanged()

    case m: msg_statustext =>
      log.info("Received status: " + m.getText)
      status = Some(m.getText)
      onStatusChanged(m.getText)

    case msg: msg_sys_status =>
      batteryVoltage = Some(msg.voltage_battery / 1000.0f)
      batteryPercent = if (msg.battery_remaining == -1) None else Some(msg.battery_remaining / 100.0f)
      onSysStatusChanged()

    case msg: msg_gps_raw_int =>
      VehicleSimulator.decodePosition(msg).foreach { loc =>
        //log.debug("Received location: " + loc)
        if (msg.satellites_visible != 255)
          numSats = Some(msg.satellites_visible)
        onLocationChanged(loc)
      }

    case msg: msg_global_position_int ⇒
      globalPos = Some(msg)
      val loc = VehicleSimulator.decodePosition(msg)
      //log.debug("Received location: " + loc)
      onLocationChanged(loc)

    case msg: msg_vfr_hud =>
      vfrHud = Some(msg)
  }

  override def onModeChanged(m: Int) {
    super.onModeChanged(m)
    eventStream.publish(MsgModeChanged(currentMode))
  }

  override def onHeartbeatFound() {
    super.onHeartbeatFound()

    setStreamEnable(true)
    // MavlinkStream.isIgnoreReceive = true // FIXME - for profiling
  }

  override def onWaypointsDownloaded() {
    super.onWaypointsDownloaded()

    // FIXME - not quite ready?
    startParameterDownload()
  }

  /**
   * Tell vehicle to select a new mode
   */
  private def setMode(mode: String) {
    sendMavlink(setMode(modeToCodeMap(mode)))
  }
}

