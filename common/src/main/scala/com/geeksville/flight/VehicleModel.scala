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
class VehicleModel extends VehicleClient with WaypointModel with ParametersModel {

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

  private val planeCodeToModeMap = Map(0 -> "MANUAL", 1 -> "CIRCLE", 2 -> "STABILIZE",
    3 -> "TRAINING",
    5 -> "FBW_A", 6 -> "FBW_B", 10 -> "AUTO",
    11 -> "RTL", 12 -> "LOITER", 15 -> "GUIDED", 16 -> "INITIALIZING")

  private val copterCodeToModeMap = Map(
    0 -> "STABILIZE",
    1 -> "ACRO",
    2 -> "ALT_HOLD",
    3 -> "AUTO",
    4 -> "GUIDED",
    5 -> "LOITER",
    6 -> "RTL",
    7 -> "CIRCLE",
    8 -> "POSITION",
    9 -> "LAND",
    10 -> "OF_LOITER",
    11 -> "TOY_A",
    12 -> "TOY_B")

  private val planeModeToCodeMap = planeCodeToModeMap.map { case (k, v) => (v, k) }
  private val copterModeToCodeMap = copterCodeToModeMap.map { case (k, v) => (v, k) }

  // We always want to see radio packets (which are hardwired for this sys id)
  val radioSysId = 51
  MavlinkEventBus.subscribe(VehicleModel.this, radioSysId)

  private def onLocationChanged(l: Location) {
    locationThrottle { () =>
      eventStream.publish(l)
    }
  }

  def isPlane = vehicleType.map(_ == MAV_TYPE.MAV_TYPE_FIXED_WING).getOrElse(false)
  def isCopter = vehicleType.map { t =>
    (t == MAV_TYPE.MAV_TYPE_QUADROTOR) || (t == MAV_TYPE.MAV_TYPE_HELICOPTER) || (t == MAV_TYPE.MAV_TYPE_HEXAROTOR) || (t == MAV_TYPE.MAV_TYPE_OCTOROTOR)
  }.getOrElse(true)

  // FIXME handle other vehicle types
  override def vehicleTypeName = if (isCopter) "ArduCopter" else "ArduPlane"

  private def codeToModeMap = if (isPlane)
    planeCodeToModeMap
  else if (isCopter)
    copterCodeToModeMap
  else
    Map[Int, String]()

  private def modeToCodeMap = if (isPlane)
    planeModeToCodeMap
  else if (isCopter)
    copterModeToCodeMap
  else
    Map[String, Int]()

  def currentMode = codeToModeMap.getOrElse(customMode.getOrElse(-1), "unknown")

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
        location = Some(loc)
        onLocationChanged(loc)
      }

    case msg: msg_global_position_int ⇒
      val loc = VehicleSimulator.decodePosition(msg)
      //log.debug("Received location: " + loc)
      location = Some(loc)
      onLocationChanged(loc)

  }

  override def onModeChanged(m: Int) {
    super.onModeChanged(m)
    eventStream.publish(MsgModeChanged(currentMode))
  }

  override def onHeartbeatFound() {
    super.onHeartbeatFound()

    val defaultFreq = 1
    val interestingStreams = Seq(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS -> defaultFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS -> defaultFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS -> 2,
      MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION -> defaultFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1 -> 10, // faster AHRS display use a bigger #
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2 -> defaultFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3 -> defaultFreq)

    interestingStreams.foreach {
      case (id, freqHz) =>
        val f = if (VehicleModel.isUsbBusted) 1 else freqHz
        sendMavlink(requestDataStream(id, f))
        sendMavlink(requestDataStream(id, f))
    }

    // MavlinkStream.isIgnoreReceive = true // FIXME - for profiling

    // First contact, download any waypoints from the vehicle and get params
    MockAkka.scheduler.scheduleOnce(5 seconds, VehicleModel.this, StartWaypointDownload)
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

object VehicleModel {
  /**
   * Some android clients don't have working USB and therefore have very limited bandwidth.  This nasty global allows the android builds to change 'common' behavior.
   */
  var isUsbBusted = false
}