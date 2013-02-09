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
case class MsgWaypointsDownloaded(wp: Seq[Waypoint])
case object MsgParametersDownloaded
case object MsgWaypointsChanged
case class MsgRcChannelsChanged(m: msg_rc_channels_raw)
case class MsgModeChanged(m: Int)

// Commands we accept in our actor queue
case class DoGotoGuided(m: msg_mission_item)

/**
 * Start sending waypoints TO the vehicle
 */
case object SendWaypoints

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleMonitor extends HeartbeatMonitor with VehicleSimulator {

  case class RetryExpired(ctx: RetryContext)
  case object FinishParameters
  case object StartWaypointDownload

  // We can receive _many_ position updates.  Limit to one update per second (to keep from flooding the gui thread)
  private val locationThrottle = new Throttled(1000)
  private val rcChannelsThrottle = new Throttled(500)
  private val sysStatusThrottle = new Throttled(5000)
  private val attitudeThrottle = new Throttled(100)

  private val retries = HashSet[RetryContext]()

  var status: Option[String] = None
  var location: Option[Location] = None
  var batteryPercent: Option[Float] = None
  var batteryVoltage: Option[Float] = None
  var radio: Option[msg_radio] = None
  var numSats: Option[Int] = None
  var rcChannels: Option[msg_rc_channels_raw] = None
  var attitude: Option[msg_attitude] = None
  var guidedDest: Option[Waypoint] = None

  var waypoints = IndexedSeq[Waypoint]()

  private var numWaypointsRemaining = 0
  private var nextWaypointToFetch = 0

  var parameters = new Array[ParamValue](0)
  private var retryingParameters = false

  val MAVLINK_TYPE_CHAR = 0
  val MAVLINK_TYPE_UINT8_T = 1
  val MAVLINK_TYPE_INT8_T = 2
  val MAVLINK_TYPE_UINT16_T = 3
  val MAVLINK_TYPE_INT16_T = 4
  val MAVLINK_TYPE_UINT32_T = 5
  val MAVLINK_TYPE_INT32_T = 6
  val MAVLINK_TYPE_UINT64_T = 7
  val MAVLINK_TYPE_INT64_T = 8
  val MAVLINK_TYPE_FLOAT = 9
  val MAVLINK_TYPE_DOUBLE = 10

  private val planeCodeToModeMap = Map(0 -> "MANUAL", 1 -> "CIRCLE", 2 -> "STABILIZE",
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
  MavlinkEventBus.subscribe(this, radioSysId)

  /**
   * Wrap the raw message with clean accessors, when a value is set, apply the change to the target
   */
  class ParamValue {
    private[VehicleMonitor] var raw: Option[msg_param_value] = None

    def getId = raw.map(_.getParam_id)

    def getValue = raw.map { v =>
      val asfloat = v.param_value

      raw.get.param_type match {
        case MAVLINK_TYPE_FLOAT => asfloat: AnyVal
        case _ => asfloat.toInt: AnyVal
      }
    }

    def setValue(v: Float) {
      val p = raw.getOrElse(throw new Exception("Can not set uninited param"))

      p.param_value = v
      log.debug("Telling device to set value: " + this)
      sendMavlink(paramSet(p.getParam_id, p.param_type, v))
    }

    override def toString = (for { id <- getId; v <- getValue } yield { id + " = " + v }).getOrElse("undefined")
  }

  override def systemId = 253 // We always claim to be a ground controller (FIXME, find a better way to pick a number)

  private def onLocationChanged(l: Location) {
    locationThrottle { () =>
      eventStream.publish(l)
    }
  }

  def isPlane = vehicleType.map(_ == MAV_TYPE.MAV_TYPE_FIXED_WING).getOrElse(false)
  def isCopter = vehicleType.map { t =>
    (t == MAV_TYPE.MAV_TYPE_QUADROTOR) || (t == MAV_TYPE.MAV_TYPE_HELICOPTER) || (t == MAV_TYPE.MAV_TYPE_HEXAROTOR) || (t == MAV_TYPE.MAV_TYPE_OCTOROTOR)
  }.getOrElse(true)

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
  private def onWaypointsDownloaded() { eventStream.publish(MsgWaypointsDownloaded(waypoints)) }
  private def onWaypointsChanged() { eventStream.publish(MsgWaypointsChanged) }
  private def onParametersDownloaded() { eventStream.publish(MsgParametersDownloaded) }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {
    case DoGotoGuided(m) =>
      gotoGuided(m)

    case RetryExpired(ctx) =>
      ctx.doRetry()

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

    //
    // Messages for uploading waypoints
    //
    case SendWaypoints =>
      sendWithRetry(missionCount(waypoints.size), classOf[msg_mission_request])

    case msg: msg_mission_request =>
      if (msg.target_system == systemId) {
        log.debug("Vehicle requesting waypoint %d".format(msg.seq))
        checkRetryReply(msg) // Cancel any retries that were waiting for this message

        val wp = waypoints(msg.seq).msg
        // Make sure that the target system is correct (FIXME - it seems like this is not correct)
        wp.target_system = msg.sysId
        wp.target_component = msg.componentId
        wp.sysId = systemId
        wp.componentId = componentId
        log.debug("Sending wp: " + wp)
        sendMavlink(wp)
      }

    //
    // Messages for downloading waypoints from vehicle
    //

    case StartWaypointDownload =>
      startWaypointDownload()
    //startParameterDownload() // For testing

    case msg: msg_mission_count =>
      if (msg.target_system == systemId) {
        log.info("Vehicle has %d waypoints, downloading...".format(msg.count))
        checkRetryReply(msg).foreach { msg =>
          // We were just told how many waypoints the target has, now fetch them (one at a time)
          numWaypointsRemaining = msg.count
          nextWaypointToFetch = 0
          waypoints = IndexedSeq()
          requestNextWaypoint()
        }
      }

    case msg: msg_mission_item =>
      if (msg.target_system == systemId) {
        log.debug("Receive: " + msg)
        if (msg.seq != nextWaypointToFetch)
          log.error("Ignoring duplicate waypoint response")
        else
          checkRetryReply(msg).foreach { msg =>
            waypoints = waypoints :+ Waypoint(msg)

            /*
 * MISSION_ITEM {target_system : 255, target_component : 190, seq : 0, frame : 0, command : 16, current : 1, autocontinue : 1, param1 : 0.0, param2 : 0.0, param3 : 0.0, param4 : 0.0, x : 37.5209159851, y : -122.309059143, z : 143.479995728}
 */
            nextWaypointToFetch += 1
            requestNextWaypoint()
          }
      }

    case msg: msg_mission_ack =>
      if (msg.target_system == systemId) {
        log.debug("Receive: " + msg)
        checkRetryReply(msg)
        if (msg.`type` == MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED)
          // The target expects us to ack his ack...
          sendMavlink(missionAck(MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED))
      }

    case msg: msg_mission_current =>
      // Update the current waypoint
      checkRetryReply(msg)
      if (waypoints.count { w =>
        val newval = if (w.seq == msg.seq) 1 else 0
        val changed = newval != w.msg.current
        if (changed)
          w.msg.current = newval
        changed
      } > 0)
        onWaypointsChanged()

    //
    // Messages for downloading parameters from vehicle

    case msg: msg_param_value =>
      // log.debug("Receive: " + msg)
      checkRetryReply(msg)
      if (msg.param_count != parameters.size)
        // Resize for new parameter count
        parameters = ArrayBuffer.fill(msg.param_count)(new ParamValue).toArray

      var index = msg.param_index
      if (index == 65535) { // Apparently means unknown, find by name (kinda slow - FIXME)
        index = parameters.zipWithIndex.find {
          case (p, i) =>
            p.getId.getOrElse("") == msg.getParam_id
        }.get._2
      }
      parameters(index).raw = Some(msg)
      if (retryingParameters)
        readNextParameter()

    case FinishParameters =>
      readNextParameter()
  }

  override def postStop() {
    // Close off any retry timers
    retries.toList.foreach(_.close())

    super.postStop()
  }

  override def onModeChanged(m: Int) {
    super.onModeChanged(m)
    eventStream.publish(MsgModeChanged(m))
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
        val f = if (VehicleMonitor.isUsbBusted) 1 else freqHz
        sendMavlink(requestDataStream(id, f))
        sendMavlink(requestDataStream(id, f))
    }

    // MavlinkStream.isIgnoreReceive = true // FIXME - for profiling

    // First contact, download any waypoints from the vehicle and get params
    MockAkka.scheduler.scheduleOnce(5 seconds, this, StartWaypointDownload)
  }

  case class RetryContext(val retryPacket: MAVLinkMessage, val expectedResponse: Class[_]) {
    val numRetries = 5
    var retriesLeft = numRetries
    val retryInterval = 3000
    var retryTimer: Option[Cancellable] = None

    doRetry()

    def close() {
      //log.debug("Closing a retry")
      retryTimer.foreach(_.cancel())
      retries.remove(this)
    }

    /**
     * Return true if we handled it
     */
    def handleRetryReply[T <: MAVLinkMessage](reply: T) = {
      if (reply.getClass == expectedResponse) {
        // Success!
        close()
        true
      } else
        false
    }

    def doRetry() {
      if (retriesLeft > 0) {
        log.debug("Retry expired on " + retryPacket + " trying again...")
        retriesLeft -= 1
        sendMavlink(retryPacket)
        retryTimer = Some(MockAkka.scheduler.scheduleOnce(retryInterval milliseconds, VehicleMonitor.this, RetryExpired(this)))
      } else {
        log.error("No more retries, giving up: " + retryPacket)
        close()
      }
    }
  }

  /**
   * Send a packet that expects a certain packet type in response, if the response doesn't arrive, then retry
   */
  private def sendWithRetry(msg: MAVLinkMessage, expected: Class[_]) {
    retries.add(RetryContext(msg, expected))
  }

  /**
   * Check to see if this satisfies our retry reply requirement, if it does and it isn't a dup return the message, else None
   */
  private def checkRetryReply[T <: MAVLinkMessage](reply: T): Option[T] = {
    val numHandled = retries.count(_.handleRetryReply(reply))
    if (numHandled > 0) {
      Some(reply)
    } else
      None
  }

  private def startWaypointDownload() {
    sendWithRetry(missionRequestList(), classOf[msg_mission_count])
  }

  private def startParameterDownload() {
    retryingParameters = false
    log.debug("Requesting vehicle parameters")
    sendWithRetry(paramRequestList(), classOf[msg_param_value])
    MockAkka.scheduler.scheduleOnce(20 seconds, this, FinishParameters)
  }

  /**
   * If we are still missing parameters, try to read again
   */
  private def readNextParameter() {
    val wasMissing = parameters.zipWithIndex.find {
      case (v, i) =>
        val hasData = v.raw.isDefined
        if (!hasData)
          sendWithRetry(paramRequestRead(i), classOf[msg_param_value])

        !hasData // Stop here?
    }.isDefined

    retryingParameters = wasMissing
    if (!wasMissing) {
      log.info("Downloaded " + parameters.size + " parameters!")
      parameters = parameters.sortWith { case (a, b) => a.getId.getOrElse("ZZZ") < b.getId.getOrElse("ZZZ") }
      onParametersDownloaded() // Yay - we have everything!
    }
  }

  /**
   * Convert the specified altitude into an AGL altitude
   * FIXME - currently we just use the home location - eventually we should use local terrain alt
   */
  def toAGL(l: Location) = {
    val groundAlt = if (!waypoints.isEmpty) {
      val wp = waypoints(0)
      if (wp.isMSL)
        wp.altitude
      else
        0.0f
    } else
      0.0f

    l.alt - groundAlt
  }

  /**
   * Tell vehicle to select a new mode
   */
  def setMode(mode: String) {
    sendMavlink(setMode(modeToCodeMap(mode)))
  }

  /**
   * FIXME - we currently assume dest has a relative altitude
   */
  private def gotoGuided(m: msg_mission_item) {
    sendWithRetry(m, classOf[msg_mission_ack])
    guidedDest = Some(Waypoint(m))
    onWaypointsChanged()
  }

  def setCurrent(seq: Int) {
    val m = missionSetCurrent(seq)
    sendWithRetry(m, classOf[msg_mission_current])
  }

  /**
   * FIXME - support timeouts
   */
  private def requestNextWaypoint() {
    if (numWaypointsRemaining > 0) {
      numWaypointsRemaining -= 1
      sendWithRetry(missionRequest(nextWaypointToFetch), classOf[msg_mission_item])
    } else {
      onWaypointsDownloaded()

      // FIXME - not quite ready?
      startParameterDownload()
    }
  }
}

object VehicleMonitor {
  /**
   * Some android clients don't have working USB and therefore have very limited bandwidth.  This nasty global allows the android builds to change 'common' behavior.
   */
  var isUsbBusted = false
}