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
import org.mavlink.messages.MAV_MODE
import org.mavlink.messages.MAV_MODE_FLAG
import org.mavlink.messages.MAV_STATE
import scala.collection.mutable.ObservableBuffer
import scala.collection.mutable.SynchronizedBuffer
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeEvent

//
// Messages we publish on our event bus when something happens
//

object MsgStatusChanged {
  val SEVERITY_LOW = 1
  val SEVERITY_MEDIUM = 2
  val SEVERITY_HIGH = 3
  val SEVERITY_CRITICAL = 4
  val SEVERITY_USER_RESPONSE = 5
}

case object MsgSysStatusChanged
case class MsgRcChannelsChanged(m: msg_rc_channels_raw)
case class MsgServoOutputChanged(m: msg_servo_output_raw)

/**
 * The vehicle has changed modes (due to a GCS, RC or local decision)
 */
case class MsgModeChanged(m: String)

/**
 * The vehicle has changed mode due to an RC mode switch position change.  (MsgModeChanged will also be published)
 */
case class MsgRCModeChanged(m: String)

/// Published when our state machine changes states
case class MsgFSMChanged(stateName: String)

/// A non fatal bug has occurred
case class MsgReportBug(m: String)

//
// Messages we expect from others
//

/// Sent from the app layer when a new interface is plugged in
case class OnInterfaceChanged(connected: Boolean)

case class StatusText(str: String, severity: Int) {
  override def toString = str // So ObservableAdapter can get nice strings
}

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleModel(targetSystem: Int = 1) extends VehicleClient(targetSystem) with WaypointModel with FenceModel {

  // We can receive _many_ position updates.  Limit to one update per second (to keep from flooding the gui thread)
  private val locationThrottle = new Throttled(1000)
  private val rcChannelsThrottle = new Throttled(500)
  private val servoOutputThrottle = new Throttled(500)
  private val sysStatusThrottle = new Throttled(5000)
  private val attitudeThrottle = new Throttled(100)

  /// We keep the last 25ish status messages in the model
  val maxStatusHistory = 25
  val statusMessages = new ArrayBuffer[StatusText] with ObservableBuffer[StatusText] with SynchronizedBuffer[StatusText]

  val fsm = new VehicleFSM(this) {
    setDebugFlag(true)
    enterStartState()
    val l = new PropertyChangeListener {
      def propertyChange(ev: PropertyChangeEvent) {
        val oldState = ev.getOldValue.asInstanceOf[VehicleModelState]
        val newState = ev.getNewValue.asInstanceOf[VehicleModelState]
        log.debug("publishing fsm: " + oldState.getName + " -> " + newState.getName)
        eventStream.publish(MsgFSMChanged(newState.getName))
      }
    }
    addStateChangeListener(l)
  }

  var location: Option[Location] = None
  var batteryPercent: Option[Float] = None
  var batteryVoltage: Option[Float] = None
  var radio: Option[msg_radio] = None
  var numSats: Option[Int] = None

  /// Are we connected to any sort of interface hardware
  var hasInterface = false

  /**
   * Horizontal position precision in meters
   */
  var hdop: Option[Float] = None
  var rcChannels: Option[msg_rc_channels_raw] = None
  var servoOutputRaw: Option[msg_servo_output_raw] = None
  var attitude: Option[msg_attitude] = None
  var vfrHud: Option[msg_vfr_hud] = None
  var globalPos: Option[msg_global_position_int] = None

  // We always want to see radio packets (which are hardwired for this sys id)
  val radioSysId = 51
  MavlinkEventBus.subscribe(VehicleModel.this, radioSysId)

  /**
   * We can only detect flight modes in copter currently
   */
  def isFlying = systemStatus.flatMap {
    case MAV_STATE.MAV_STATE_ACTIVE =>
      Some(true)
    case MAV_STATE.MAV_STATE_STANDBY => // Used by 3.1 AC and plane to indicate !flying
      Some(false)
    case MAV_STATE.MAV_STATE_CALIBRATING => // Plane uses this
      Some(false)
    case MAV_STATE.MAV_STATE_CRITICAL => // Plane uses this
      Some(true)
    case _ =>
      None // We don't know
  }

  def isGCSInitializing = {
    fsm.getState.getName match {
      case "VehicleFSM.WantInterface" =>
        true
      case "VehicleFSM.WantVehicle" =>
        true
      case "VehicleFSM.DownloadingWaypoints" =>
        true
      case "VehicleFSM.DownloadingParameters" =>
        true
      case _ =>
        false
    }
  }

  private def onLocationChanged(l: Location) {
    location = Some(l)

    locationThrottle { () =>
      eventStream.publish(l)
    }
  }

  /**
   * Extract a particular rc channel value
   *
   * FIXME - move someplace better
   */
  private def getChannel(m: msg_rc_channels_raw, chNum: Int) = {
    chNum match {
      case 1 => m.chan1_raw
      case 2 => m.chan2_raw
      case 3 => m.chan3_raw
      case 4 => m.chan4_raw
      case 5 => m.chan5_raw
      case 6 => m.chan6_raw
      case 7 => m.chan7_raw
      case 8 => m.chan8_raw
      case _ => 0 // All other channels are assumed to be zero (not available by mavlink)
    }

  }

  /**
   * Does the user have a real RC radio, or just our rc_override based stuff, None for unknown
   */
  def hasRealRc: Option[Boolean] = {
    Some(true)
  }

  /**
   * What mode does the RC transmitter want us to use?  (If known)
   */
  def rcRequestedMode =

    // Use a for comprehension - if any of the following steps generate None the result will be None
    for {
      ch <- rcModeChannel
      rc <- rcChannels

      modeNum <- {
        // Using constants per AC/AP code
        val v = getChannel(rc, ch)
        if (v == 0)
          None // If the pwm value is exactly zero we assume the user has no real RC xmitter - we are just looking at our own RC_OVERRIDE
        else
          Some {
            if (v <= 1230)
              1
            else if (v <= 1360)
              2
            else if (v <= 1490)
              3
            else if (v <= 1620)
              4
            else if (v <= 1749)
              5
            else
              6
          }
      }

      requestedMode <- getFlightMode(modeNum)
    } yield {
      requestedMode
    }

  /**
   * Return the best user visible description of altitude that we have (hopefully from a barometer)
   * In AGL
   */
  def bestAltitude = globalPos.map(_.relative_alt / 1000.0f).getOrElse(location.map(toAGL(_).toFloat).getOrElse(-999f))

  def currentMode = modeToString(customMode.getOrElse(-1))

  /**
   * Return current vehicle mode, or if not armed/missing say that (useful for small status displays)
   */
  def currentModeOrStatus = if (!hasInterface)
    "No interface"
  else if (!hasHeartbeat)
    "No vehicle"
  else if (!isArmed)
    "Disarmed"
  else
    currentMode

  /**
   * The mode names we understand
   */
  def modeNames = modeToCodeMap.keys.toSeq.sorted :+ "unknown"

  protected[flight] def onUndefinedTransition(state: VehicleModelState) {
    val sname = state.getName
    val trans = fsm.getTransition

    val msg = s"Undefined transition $trans in $sname"
    log.error(msg)
    eventStream.publish(MsgReportBug(msg))
  }

  /**
   * Return a restricted set of mode names based just on what the user can do in the current flight mode (if simpleMode)
   * Each pair is a name and a bool to indicate the user should be asked to confirm
   */
  def selectableModeNames = {
    var names = modeNames
    if (isCopter)
      if (!isArmed)
        names = names :+ "Arm"
      else
        names = names :+ "Disarm"

    val flying = isFlying.getOrElse(false)
    log.debug(s"flying=$flying, mode names: " + names.mkString(","))

    val filter = if (isGCSInitializing)
      initializingModes
    else if (flying)
      simpleFlightModes
    else
      simpleGroundModes

    names.flatMap { name =>
      filter.get(name) match {
        case Some(confirm) =>
          Some(name -> confirm)
        case None =>
          None
      }
    }
  }

  private def onStatusChanged(str: String, sc: Int) {
    var s = str

    if (statusMessages.size == maxStatusHistory)
      statusMessages.remove(0)
    val t = StatusText(s, sc)
    statusMessages += t

    // Publish the interesting strings (stripping out bogus APM msgs)
    if (!s.startsWith("command received:")) {
      val prefix = "PreArm: "
      if (s.startsWith(prefix))
        s = "Failure: " + s.substring(prefix.length)

      eventStream.publish(StatusText(s, sc))
    }
  }

  private def onSysStatusChanged() { sysStatusThrottle { () => eventStream.publish(MsgSysStatusChanged) } }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {

    case OnInterfaceChanged(c) =>
      hasInterface = c
      if (c)
        fsm.OnHasInterface()
      else {
        forceLostHeartbeat() // No point in waiting for a HB which will never come...
        fsm.OnLostInterface()
      }

    case DoSetMode(m) =>
      setMode(m)

    case m: msg_attitude =>
      attitude = Some(m)
      attitudeThrottle { () => eventStream.publish(m) }

    case m: msg_rc_channels_raw =>
      val oldMode = rcRequestedMode
      rcChannels = Some(m)
      val newMode = rcRequestedMode
      if (newMode.isDefined && oldMode != newMode) {
        val s = modeToString(newMode.get)
        log.warn(s"RC mode change: $s")
        eventStream.publish(MsgRCModeChanged(s))
      }

      rcChannelsThrottle { () => eventStream.publish(MsgRcChannelsChanged(m)) }

    case m: msg_servo_output_raw =>
      servoOutputRaw = Some(m)
      servoOutputThrottle { () => eventStream.publish(MsgServoOutputChanged(m)) }

    case m: msg_radio =>
      //log.info("Received radio from " + m.sysId + ": " + m)
      radio = Some(m)
      onSysStatusChanged()

    case m: msg_statustext =>
      log.info("Received status: " + m.getText)

      // APM uses some unfortunate word choice and has no notion of failure codes.  Change the terminology here
      var s = m.getText
      onStatusChanged(s, m.severity)

    case msg: msg_sys_status =>
      batteryVoltage = Some(msg.voltage_battery / 1000.0f)
      batteryPercent = if (msg.battery_remaining == -1) None else Some(msg.battery_remaining / 100.0f)
      onSysStatusChanged()

    case msg: msg_gps_raw_int =>
      VehicleSimulator.decodePosition(msg).foreach { loc =>
        //log.debug("Received location: " + loc)
        if (msg.satellites_visible != 255)
          numSats = Some(msg.satellites_visible)
        if (msg.eph != 65535)
          hdop = Some(msg.eph / 100.0f)

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

  override protected def onSystemStatusChanged(m: Int) {
    super.onSystemStatusChanged(m)

    if (isArmed && isFlying.getOrElse(false))
      fsm.HBSaysFlying()
  }

  override def onHeartbeatFound() {
    super.onHeartbeatFound()

    setStreamEnable(true)
    // MavlinkStream.isIgnoreReceive = true // FIXME - for profiling

    fsm.OnHasHeartbeat()
  }

  override def onHeartbeatLost() {
    super.onHeartbeatLost()

    onStatusChanged("Lost contact to vehicle", MsgStatusChanged.SEVERITY_HIGH)
    fsm.OnLostHeartbeat()
  }

  override def onWaypointDownloadFailed() {
    super.onWaypointDownloadFailed()

    // Temp hack until I understand why WP download sometimes fails - it seems like the far radio just isn't listening
    // Force the user to reconnect
    onStatusChanged("WP download failed: please reconnect", MsgStatusChanged.SEVERITY_CRITICAL)
    fsm.OnLostInterface()
  }

  override def onWaypointsDownloaded() {
    super.onWaypointsDownloaded()
    fsm.OnWaypointsDownloaded()

    startParameterDownload()
  }

  override protected def onParametersDownloaded() {
    super.onParametersDownloaded()

    fsm.OnParametersDownloaded()

    /// Select correct next state (because we are guaranteed to already be in one of these states)
    if (isArmed && isFlying.getOrElse(false))
      fsm.HBSaysFlying()
    else if (isArmed)
      fsm.HBSaysArmed()
    else
      fsm.HBSaysDisarmed()
  }

  override protected def onArmedChanged(armed: Boolean) {
    super.onArmedChanged(armed)
    if (armed)
      fsm.HBSaysArmed()
    else
      fsm.HBSaysDisarmed()
  }

  /**
   * Tell vehicle to select a new mode (we use Arm and Disarm as special pseduo modes
   */
  private def setMode(mode: String) {
    mode match {
      case "Arm" =>
        // Temporary hackish safety check until AC can be updated (see https://github.com/diydrones/ardupilot/issues/553)
        val throttleTooFast = (for {
          rc <- rcChannels
          minThrottle <- parametersById("RC3_MIN").getInt
        } yield {
          // If there is no radio AC will claim 0 for all RC channels - treat that as failure
          rc.chan3_raw > minThrottle * 1.10 || rc.chan3_raw == 0
        }).getOrElse(true)

        if (throttleTooFast)
          onStatusChanged("Failure to arm - bad throttle", MsgStatusChanged.SEVERITY_HIGH)
        else
          sendMavlink(commandDoArm(true))

      case "Disarm" =>
        sendMavlink(commandDoArm(false))
      case _ =>
        sendMavlink(setMode(modeToCodeMap(mode)))
    }
  }

}

