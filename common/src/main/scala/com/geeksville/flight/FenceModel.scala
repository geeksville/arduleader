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
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashSet
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.mavlink.MavlinkStream
import org.mavlink.messages.FENCE_ACTION
import java.io.InputStream
import scala.io.Source
import com.geeksville.akka.InstrumentedActor

//
// Messages we publish on our event bus when something happens
//
case object MsgFenceChanged

/// Published if the user busts through a fence
case object MsgFenceBreached

case class DoSetFence(pts: Seq[Location], newMode: Int)

/**
 * Client side mixin for modelling waypoints on a vehicle
 */
trait FenceModel extends ParametersModel {

  private var fencePoints = ListBuffer[msg_fence_point]()

  private var fenceStatus: Option[msg_fence_status] = None

  /**
   * Where the vehicle will go if the fence is broken
   */
  def fenceReturnPoint = fencePoints.headOption.map { p => Location(p.lat, p.lng) }

  /**
   * If this fence has a bounding box, this sequence will be non-empty
   */
  def fenceBoundary = if (fencePoints.isEmpty) Seq() else fencePoints.tail.map { p => Location(p.lat, p.lng) }

  def fenceNumBreach = fenceStatus.map(_.breach_count).getOrElse(0)

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: InstrumentedActor.Receiver = {

    case DoSetFence(pts, newMode) =>
      uploadFence(pts, newMode)

    case msg: msg_fence_point =>
      // If it is the point we are waiting for, add it
      // We check for msg.target_system == 0 as a workaround for an ardupilot bug: https://github.com/diydrones/ardupilot/issues/152
      if ((msg.target_system == systemId || msg.target_system == 0) && msg.idx == fencePoints.size) {
        checkRetryReply(msg) // Cancel any retries that were waiting for this message

        log.debug("FenceRx: " + msg)
        fencePoints += msg
        if (msg.idx == msg.count - 1) {
          log.info("Downloaded fence, numPoints " + msg.count)
          onFenceChanged() // Done!
        } else
          requestPoint(msg.idx + 1)
      }

    case msg: msg_fence_status =>
      val oldCount = fenceNumBreach
      fenceStatus = Some(msg)
      if (fenceNumBreach > oldCount)
        eventStream.publish(MsgFenceBreached)
  }

  override protected def onParametersDownloaded() {
    super.onParametersDownloaded()
    startFenceDownload()
  }

  private def onFenceChanged() { eventStream.publish(MsgFenceChanged) }

  private def startFenceDownload() {
    fencePoints.clear()
    if (fenceTotal < 1)
      log.info("No fence on device - skipping download")
    else {
      log.info("Downloading fence")
      requestPoint(0)
    }
  }

  private def uploadFence(locs: Seq[Location], newMode: Int) {
    if (!isFenceAvailable)
      log.error("Fence not yet available")
    else {
      log.info("Uploading fence, numPoints=" + locs.size)

      fenceAction = FENCE_ACTION.FENCE_ACTION_NONE // Must be off before updating
      fenceTotal = locs.size

      val outgoing = locs.zipWithIndex.map {
        case (l, i) =>
          fencePoint(i, locs.size, l.lat.toFloat, l.lon.toFloat)
      }

      // Unfortunately the device doesn't ack fence points.  So we just read it back and count on the user to notice
      outgoing.foreach { p =>
        log.debug("Sending: " + p)
        sendMavlink(p)
      }

      // FIXME - this seems to have no effect - WTF?
      fenceAction = newMode
      startFenceDownload() // Read it back to force a GUI update and confirm the device will do the right thing
    }
  }

  private def requestPoint(n: Int) = sendWithRetry(fenceFetchPoint(n), classOf[msg_fence_point])

  // These parameters are for the plane stype of fence

  private def fenceTotal = parametersById.get("FENCE_TOTAL").map(_.getInt.get).getOrElse(0)
  def fenceAction = parametersById.get("FENCE_ACTION").map(_.getInt.get).getOrElse(FENCE_ACTION.FENCE_ACTION_NONE)

  def isFenceAvailable = parametersById.get("FENCE_ACTION").isDefined

  /**
   * Channel #, or 0 for disabled
   */
  def fenceChannel = parametersById.get("FENCE_CHANNEL").map(_.getInt.get).getOrElse(0)

  private def fenceAction_=(n: Int) {
    parametersById.get("FENCE_ACTION").get.setValueNoAck(n)
  }

  private def fenceTotal_=(n: Int) {
    parametersById.get("FENCE_TOTAL").get.setValueNoAck(n)
  }

  // These parameters for for copter style fence

  // @Param: ENABLE
  // @DisplayName: Fence enable/disable
  // @Description: Allows you to enable (1) or disable (0) the fence functionality
  // @Values: 0:Disabled,1:Enabled
  def isFenceEnable = parametersById.get("FENCE_ENABLE").map(_.getBoolean.get).getOrElse(false)

  // @Param: TYPE
  // @DisplayName: Fence Type
  // @Description: Enabled fence types held as bitmask
  // @Values: 0:None,1:Altitude,2:Circle,3:Altitude and Circle
  // @User: Standard
  def fenceType = parametersById.get("FENCE_TYPE").map(_.getInt.get).getOrElse(0)
  def fenceIsCircle = {
    val t = fenceType
    t == 2 || t == 3
  }

  // @Param: ACTION
  // @DisplayName: Action to perform when the limit is breached
  // @Description: What to do on fence breach
  // @Values: 0:Report Only,1:RTL or Land
  // @User: Standard

  // @Param: ALT_MAX
  // @DisplayName: Fence Maximum Altitude
  // @Description: Maximum altitude allowed before geofence triggers
  // @Units: Meters
  // @Range: 10 1000
  // @Increment: 1
  // @User: Standard
  def fenceAltMax = parametersById.get("FENCE_ALT_MAX").map(_.getFloat.get)

  // @Param: RADIUS
  // @DisplayName: Circular Fence Radius
  // @Description: Circle fence radius which when breached will cause an RTL
  // @Units: Meters
  // @Range: 0 10000
  // @User: Standard
  def fenceRadius = parametersById.get("FENCE_RADIUS").map(_.getFloat.get)
}

object FenceModel {
  /**
   * It is callers responsibility to close the stream
   */
  def pointsFromStream(is: InputStream) = {
    val Comment = "#(.*)".r
    val LatLng = "(\\S+) (\\S+)".r

    Source.fromInputStream(is).getLines.flatMap { l =>
      l match {
        case LatLng(lat, lng) => Some(Location(lat.toDouble, lng.toDouble))
        case _ => None
      }
    }.toArray
  }
}