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

//
// Messages we publish on our event bus when something happens
//
case class MsgStatusChanged(s: String)
case object MsgSysStatusChanged
case class MsgWaypointsDownloaded(wp: Seq[msg_mission_item])
case object MsgParametersDownloaded
case object MsgWaypointsChanged
case class MsgModeChanged(m: Int)
/**
 * Start sending waypoints TO the vehicle
 */
case object SendWaypoints

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
class VehicleMonitor extends HeartbeatMonitor with VehicleSimulator {

  case object RetryExpired
  case object FinishParameters
  case object StartWaypointDownload

  // We can receive _many_ position updates.  Limit to one update per second (to keep from flooding the gui thread)
  private val locationThrottle = new Throttled(1000)
  private val sysStatusThrottle = new Throttled(1000)

  var status: Option[String] = None
  var location: Option[Location] = None
  var batteryPercent: Option[Float] = None
  var batteryVoltage: Option[Float] = None

  var waypoints = Seq[msg_mission_item]()

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
    locationThrottle {
      eventStream.publish(l)
    }
  }

  private def onStatusChanged(s: String) { eventStream.publish(MsgStatusChanged(s)) }
  private def onSysStatusChanged() { sysStatusThrottle { eventStream.publish(MsgSysStatusChanged) } }
  private def onWaypointsDownloaded() { eventStream.publish(MsgWaypointsDownloaded(waypoints)) }
  private def onWaypointsChanged() { eventStream.publish(MsgWaypointsChanged) }
  private def onParametersDownloaded() { eventStream.publish(MsgParametersDownloaded) }

  private val codeToModeMap = Map(0 -> "MANUAL", 1 -> "CIRCLE", 2 -> "STABILIZE",
    5 -> "FLY_BY_WIRE_A", 6 -> "FLY_BY_WIRE_B", 10 -> "AUTO",
    11 -> "RTL", 12 -> "LOITER", 15 -> "GUIDED", 16 -> "INITIALIZING")

  private val modeToCodeMap = codeToModeMap.map { case (k, v) => (v, k) }

  def currentMode = codeToModeMap.getOrElse(customMode.getOrElse(-1), "unknown")

  /**
   * The mode names we understand
   */
  def modeNames = modeToCodeMap.keys

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {
    case RetryExpired =>
      retryExpired()

    case m: msg_statustext =>
      log.info("Received status: " + m.getText)
      status = Some(m.getText)
      onStatusChanged(m.getText)

    case msg: msg_sys_status =>
      batteryVoltage = Some(msg.voltage_battery / 1000.0f)
      batteryPercent = Some(msg.battery_remaining / 100.0f)
      onSysStatusChanged()

    case msg: msg_gps_raw_int =>
      VehicleSimulator.decodePosition(msg).foreach { loc =>
        //log.debug("Received location: " + loc)
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

        val wp = waypoints(msg.seq)
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
          waypoints = Seq()
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
            waypoints = waypoints :+ msg

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
      if (waypoints.count { w =>
        val newval = if (w.seq == msg.seq) 1 else 0
        val changed = newval != w.current
        if (changed)
          w.current = newval
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

  override def onModeChanged(m: Int) {
    super.onModeChanged(m)
    eventStream.publish(MsgModeChanged(m))
  }

  override def onHeartbeatFound() {
    super.onHeartbeatFound()

    val interestingStreams = Seq(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS,
      MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS,
      MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3)

    interestingStreams.foreach { id =>
      sendMavlink(requestDataStream(id))
      sendMavlink(requestDataStream(id))
    }

    // First contact, download any waypoints from the vehicle and get params
    MockAkka.scheduler.scheduleOnce(10 seconds, this, StartWaypointDownload)
  }

  val numRetries = 5
  var retriesLeft = 0
  val retryInterval = 3000
  var expectedResponse: Option[Class[_]] = None
  var retryPacket: Option[MAVLinkMessage] = None
  var retryTimer: Option[Cancellable] = None

  /**
   * Send a packet that expects a certain packet type in response, if the response doesn't arrive, then retry
   */
  private def sendWithRetry(msg: MAVLinkMessage, expected: Class[_]) {
    expectedResponse = Some(expected)
    retriesLeft = numRetries
    retryPacket = Some(msg)
    MockAkka.scheduler.scheduleOnce(retryInterval milliseconds, this, RetryExpired)
    sendMavlink(msg)
  }

  /**
   * Check to see if this satisfies our retry reply requirement, if it does and it isn't a dup return the message, else None
   */
  private def checkRetryReply[T <: MAVLinkMessage](reply: T): Option[T] = {
    expectedResponse.flatMap { e =>
      if (reply.getClass == e) {
        // Success!
        retryPacket = None
        expectedResponse = None
        retryTimer.foreach(_.cancel())
        Some(reply)
      } else
        None
    }
  }

  private def retryExpired() {
    retryPacket.foreach { pkt =>
      if (retriesLeft > 0) {
        log.debug("Retry expired on " + pkt + " trying again...")
        retriesLeft -= 1
        sendMavlink(pkt)
        MockAkka.scheduler.scheduleOnce(retryInterval milliseconds, this, RetryExpired)
      } else
        log.error("No more retries, giving up: " + pkt)
    }
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
      parameters = parameters.sortWith { case (a, b) => a.getId.getOrElse("ZZZ") < b.getId.getOrElse("ZZZ") }
      onParametersDownloaded() // Yay - we have everything!
    }
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
  def setGuided(dest: Location) = {
    val r = missionItem(0, dest, current = 2)
    sendWithRetry(r, classOf[msg_mission_ack])
    r
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