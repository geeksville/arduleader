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
import java.io.InputStream
import scala.io.Source

//
// Messages we publish on our event bus when something happens
//
case object MsgWaypointsChanged
case class MsgWaypointCurrentChanged(seq: Int)
// Tell front end the waypoint state is now 'dirty' and must be sent to vehicle
case class MsgWaypointDirty(isDirty: Boolean)

// Commands we accept in our actor queue
case class DoGotoGuided(m: msg_mission_item, withRetry: Boolean = true)
case class DoSetMode(s: String)
case class DoSetCurrent(n: Int)
case class DoAddWaypoint(w: Waypoint)
case class DoDeleteWaypoint(seqnum: Int)
case object DoMarkDirty

/**
 * Upload a sequence of waypoints to the vehicle (being careful to not replace home)
 */
case class DoLoadWaypoints(pts: Seq[Waypoint])

/**
 * Start sending waypoints TO the vehicle
 */
case object SendWaypoints

/**
 * Client side mixin for modelling waypoints on a vehicle
 */
trait WaypointModel extends VehicleClient with WaypointsForMap {

  case object StartWaypointDownload

  var guidedDest: Option[Waypoint] = None

  var waypoints = IndexedSeq[Waypoint]()

  var hasRequestedWaypoints = false

  private var numWaypointsRemaining = 0
  private var nextWaypointToFetch = 0

  private var dirty = false

  private def onWaypointsChanged() { eventStream.publish(MsgWaypointsChanged) }
  private def onWaypointsCurrentChanged(n: Int) { eventStream.publish(MsgWaypointCurrentChanged(n)) }

  protected def onWaypointsDownloaded() {
    onWaypointsChanged()
    setDirty(false)
  }

  override def onReceive = mReceive.orElse(super.onReceive)

  def isDirty = dirty

  private def mReceive: Receiver = {
    case DoMarkDirty =>
      setDirty(true)

    case DoGotoGuided(m, withRetry) =>
      gotoGuided(m, withRetry)

    case DoSetCurrent(n) =>
      setCurrent(n)

    case DoAddWaypoint(w) =>
      // Always add at the end
      if (w.msg.seq == 0)
        w.msg.seq = waypoints.size
      waypoints = waypoints :+ w
      setDirty(true)

    case DoDeleteWaypoint(seqnum) =>
      deleteWaypoint(seqnum)

    case DoLoadWaypoints(wpts) =>
      loadWaypoints(wpts)

    //
    // Messages for uploading waypoints
    //
    case SendWaypoints =>
      log.info("Sending " + waypoints.size + " waypoints")
      sendWithRetry(missionCount(waypoints.size), classOf[msg_mission_request])
      onWaypointsChanged() // Tell any local observers about our new waypoints

    case msg: msg_mission_request =>
      if (msg.target_system == systemId) {
        log.debug("Vehicle requesting waypoint %d".format(msg.seq))
        checkRetryReply(msg) // Cancel any retries that were waiting for this message

        if (msg.seq < waypoints.size) {
          val wp = waypoints(msg.seq).msg
          // Make sure that the target system is correct (FIXME - it seems like this is not correct)
          wp.target_system = msg.sysId
          wp.target_component = msg.componentId
          wp.sysId = systemId
          wp.componentId = componentId
          log.debug("Sending wp: " + wp)
          sendMavlink(wp)
        } else
          error("Ignoring validate wp req from vehicle")

        if (msg.seq >= waypoints.size - 1)
          setDirty(false) // We've now sent all waypoints
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
      perhapsRequestWaypoints()

      val newWpSeq = msg.seq
      if (waypoints.count { w =>
        val newval = if (w.seq == newWpSeq) 1 else 0
        val changed = newval != w.msg.current
        if (changed)
          w.msg.current = newval
        changed
      } > 0)
        onWaypointsCurrentChanged(newWpSeq)
  }

  private def startWaypointDownload() {
    log.info("Downloading waypoints")
    sendWithRetry(missionRequestList(), classOf[msg_mission_count])
  }

  /**
   * @return number of nodes deleted
   */
  private def deleteByFilter(keepmefn: Waypoint => Boolean) = {
    var numdeleted = 0
    waypoints = waypoints.filter { w =>
      val keepme = keepmefn(w)

      // For items after the msg we are deleting, we need to fixup their sequence numbers
      if (!keepme)
        numdeleted += 1
      else
        w.msg.seq -= numdeleted

      keepme
    }

    if (numdeleted > 0)
      setDirty(true)

    numdeleted
  }

  private def setDirty(v: Boolean) {
    if (v != dirty) {
      dirty = v
      eventStream.publish(MsgWaypointDirty(v))
    }
  }

  private def deleteWaypoint(seqnum: Int) = deleteByFilter { w => w.seq != seqnum }

  /**
   * During development sometimes bogus waypoints get downloaded to target.  Clean up that rubbish here
   */
  private def deleteInvalidWaypoints() = {
    val numdel = deleteByFilter { w => w.isCommandValid }
    if (numdel > 0) {
      log.error(numdel + " bogus waypoints found and deleted")
      self ! SendWaypoints
    }
  }

  private def perhapsRequestWaypoints() {

    // First contact, download any waypoints from the vehicle and get params
    if (!hasRequestedWaypoints) {
      hasRequestedWaypoints = true
      self ! StartWaypointDownload
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

    l.alt.get - groundAlt
  }

  /**
   * FIXME - we currently assume dest has a relative altitude
   */
  private def gotoGuided(m: msg_mission_item, withRetry: Boolean) {
    if (withRetry)
      sendWithRetry(m, classOf[msg_mission_ack])
    else
      sendMavlink(m)
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
      deleteInvalidWaypoints()
      log.debug("All waypoints: " + waypoints.mkString(", "))
      log.debug("Map waypoints: " + waypointsForMap.mkString(", "))
      onWaypointsDownloaded() // Success
    }
  }

  /// Keep only home from the old waypoints, but replace everything else
  private def loadWaypoints(wpts: Seq[Waypoint]) {
    if (waypoints.size < 1)
      log.error("Can't load waypoints - we don't yet have a home") // Silently fail - FIXME
    else {
      log.info("loading waypoints: " + wpts.size)
      val home = waypoints.head
      waypoints = IndexedSeq(home) ++ wpts.filter(!_.isHome)
    }
  }

  /**
   * It is callers responsibility to close the stream
   *
   * QGC WPL 110
   * 0       1       0       16      0       0       0       0       37.521152       -122.308739     132.000000      1
   * 1       0       3       22      15.000000       0.000000        0.000000        0.000000        0.000000        0.000000        10.000000
   * 1
   * 2       0       3       16      0.000000        0.000000        0.000000        0.000000        37.491919       -122.173355     50.000000
   * 1
   *
   */
  def pointsFromStream(is: InputStream) = {
    val lines = Source.fromInputStream(is).getLines.toSeq

    val header = lines.head
    if (header != "QGC WPL 110")
      throw new Exception("Unsupported waypoint file format")

    val points = lines.tail
    val Line = """(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)""".r
    points.flatMap { l =>
      l.trim match {
        case Line(seq, current, frame, command, p1, p2, p3, p4, x, y, z, cont) =>
          val msg = new msg_mission_item(systemId, componentId)
          msg.seq = seq.toInt
          msg.current = current.toInt
          msg.frame = frame.toInt
          msg.command = command.toInt
          msg.param1 = p1.toFloat
          msg.param2 = p2.toFloat
          msg.param3 = p3.toFloat
          msg.param4 = p4.toFloat
          msg.x = x.toFloat
          msg.y = y.toFloat
          msg.z = z.toFloat
          msg.autocontinue = cont.toInt
          val wp = Waypoint(msg)
          Some(wp)
        case x @ _ =>
          throw new Exception("Malformed waypoint: " + x)
      }
    }.toArray
  }
}

object WaypointModel {
}
