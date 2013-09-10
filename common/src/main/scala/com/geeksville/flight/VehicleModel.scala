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
case class MsgModeChanged(m: String)

/// Published when our state machine changes states
case class MsgFSMChanged(stateName: String)

//
// Messages we expect from others
//

/// Sent from the app layer when a new interface is plugged in
case object OnInterfaceConnected

case class StatusText(str: String, severity: Int) {
  override def toString = str // So ObservableAdapter can get nice strings
}

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleModel extends VehicleClient with WaypointModel with FenceModel {

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
        val newState = ev.getNewValue.asInstanceOf[VehicleModelState]
        log.debug("publishing fsm change: " + newState.getName)
        eventStream.publish(MsgFSMChanged(newState.getName))
      }
    }
    addStateChangeListener(l)
  }

  var status: Option[String] = None
  var location: Option[Location] = None
  var batteryPercent: Option[Float] = None
  var batteryVoltage: Option[Float] = None
  var radio: Option[msg_radio] = None
  var numSats: Option[Int] = None
  
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
  def isFlying = systemStatus.flatMap { s =>
    if (isCopter)
      Some(s == MAV_STATE.MAV_STATE_ACTIVE)
    else
      None
  }

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

  /**
   * Return a restricted set of mode names based just on what the user can do in the current flight mode (if simpleMode)
   */
  def selectableModeNames(simpleMode: Boolean) = {
    var names = modeNames
    if (isCopter)
      if (!isArmed)
        names = names :+ "Arm"
      else
        names = names :+ "Disarm"

    val flying = isFlying.getOrElse(false)
    if (!simpleMode || !isCopter) // Simple modes are only supported for copter right now
      names
    else {
      val filter = if (flying) simpleFlightModes else simpleGroundModes
      names.filter(filter.contains)
    }
  }

  private def onStatusChanged(s: String, sc: Int) {
    if (statusMessages.size == maxStatusHistory)
      statusMessages.remove(0)
    val t = StatusText(s, sc)
    statusMessages += t
    eventStream.publish(t)
  }

  private def onSysStatusChanged() { sysStatusThrottle { () => eventStream.publish(MsgSysStatusChanged) } }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {

    case OnInterfaceConnected =>
      fsm.OnHasInterface()
  
    case DoSetMode(m) =>
      setMode(m)

    case m: msg_attitude =>
      attitude = Some(m)
      attitudeThrottle { () => eventStream.publish(m) }

    case m: msg_rc_channels_raw =>
      rcChannels = Some(m)
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
      status = Some(m.getText)
      onStatusChanged(m.getText, m.severity)

    case msg: msg_sys_status =>
      batteryVoltage = Some(msg.voltage_battery / 1000.0f)
      batteryPercent = if (msg.battery_remaining == -1) None else Some(msg.battery_remaining / 100.0f)
      onSysStatusChanged()

    case msg: msg_gps_raw_int =>
      VehicleSimulator.decodePosition(msg).foreach { loc =>
        //log.debug("Received location: " + loc)
        if (msg.satellites_visible != 255)
          numSats = Some(msg.satellites_visible)
          if(msg.eph != 65535)
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

    if (isFlying.getOrElse(false))
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

    fsm.OnLostHeartbeat()
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
    if(isFlying.getOrElse(false))
      fsm.HBSaysFlying()
    else if(isArmed)
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
      case"Arm" => sendMavlink(commandDoArm(true))
      case"Disarm" => sendMavlink(commandDoArm(false))
      case _=> sendMavlink(setMode(modeToCodeMap(mode)))
    }
  }

}

