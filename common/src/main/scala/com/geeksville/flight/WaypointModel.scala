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
case object MsgWaypointsChanged

// Commands we accept in our actor queue
case class DoGotoGuided(m: msg_mission_item)
case class DoSetMode(s: String)
case class DoSetCurrent(n: Int)

/**
 * Start sending waypoints TO the vehicle
 */
case object SendWaypoints

/**
 * Client side mixin for modelling waypoints on a vehicle
 */
trait WaypointModel extends VehicleClient {

  case object StartWaypointDownload

  var guidedDest: Option[Waypoint] = None

  var waypoints = IndexedSeq[Waypoint]()

  private var numWaypointsRemaining = 0
  private var nextWaypointToFetch = 0

  private def onWaypointsChanged() { eventStream.publish(MsgWaypointsChanged) }
  protected def onWaypointsDownloaded() { onWaypointsChanged() }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {
    case DoGotoGuided(m) =>
      gotoGuided(m)

    case DoSetCurrent(n) =>
      setCurrent(n)

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
  }

  private def startWaypointDownload() {
    log.info("Downloading waypoints")
    sendWithRetry(missionRequestList(), classOf[msg_mission_count])
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

    l.alt.get - groundAlt
  }

  /**
   * FIXME - we currently assume dest has a relative altitude
   */
  private def gotoGuided(m: msg_mission_item) {
    sendWithRetry(m, classOf[msg_mission_ack])
    guidedDest = Some(Waypoint(m))
    onWaypointsChanged()
  }

  private def setCurrent(seq: Int) {
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
      log.debug("All waypoints downloaded")
      onWaypointsDownloaded() // Success
    }
  }

  /**
   * An expanded version of waypoints (i.e. resolving jumps), but removing any waypoints that don't have position
   */
  def waypointsForMap = {
    var index = 0
    val inspected = Array.fill(waypoints.size)(false)

    // No matter what we never want to emit more waypoints than we started with
    (0 until waypoints.size).flatMap { loopNum =>
      if (!inspected(index)) {
        val wp = waypoints(index)
        inspected(index) = true

        if (wp.isJump) {
          index = wp.jumpSequence
          None
        } else {
          index += 1
          if (!wp.isForMap)
            None
          else
            Some(wp)
        }
      } else {
        // Already seen it
        index += 1
        None
      }
    }
  }
}

