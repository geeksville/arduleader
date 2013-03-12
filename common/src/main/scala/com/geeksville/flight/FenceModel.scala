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
case object MsgFenceChanged

/**
 * Client side mixin for modelling waypoints on a vehicle
 */
trait FenceModel extends ParametersModel {

  private var fencePoints = ListBuffer[msg_fence_point]()

  /**
   * Where the vehicle will go if the fence is broken
   */
  def fenceReturnPoint = fencePoints.headOption.map { p => Location(p.lat, p.lng) }

  def fenceBoundary = if (fencePoints.isEmpty) Seq() else fencePoints.tail.map { p => Location(p.lat, p.lng) }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {

    case msg: msg_fence_point =>
      // If it is the point we are waiting for, add it
      // We check for msg.target_system == 0 as a workaround for an ardupilot bug
      if ((msg.target_system == systemId || msg.target_system == 0) && msg.idx == fencePoints.size) {
        checkRetryReply(msg) // Cancel any retries that were waiting for this message

        log.debug("FenceRx: " + msg)
        fencePoints += msg
        if (msg.idx == msg.count - 1)
          onFenceChanged() // Done!
        else
          requestPoint(msg.idx + 1)
      }
  }

  override protected def onParametersDownloaded() {
    super.onParametersDownloaded()
    startFenceDownload()
  }

  private def onFenceChanged() { eventStream.publish(MsgFenceChanged) }

  private def startFenceDownload() {
    if (fenceTotal < 1)
      log.info("No fence on device - skipping download")
    else {
      log.info("Downloading fence")
      requestPoint(0)
    }
  }

  private def requestPoint(n: Int) = sendWithRetry(fenceFetchPoint(n), classOf[msg_fence_point])

  private def fenceTotal = parametersById.get("FENCE_TOTAL").map(_.getInt.get).getOrElse(0)
  private def fenceAction = parametersById.get("FENCE_ACTION").map(_.getInt.get).getOrElse(0)
}

