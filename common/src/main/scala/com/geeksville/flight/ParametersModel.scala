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
case object MsgParametersDownloaded

// We just received a new parameter (after the initial download)
case class MsgParameterReceived(index: Int)

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
trait ParametersModel extends VehicleClient with ParametersReadOnlyModel {

  case object FinishParameters

  var parameters = new Array[ParamValue](0)
  var parametersById = Map[String, ParamValue]()

  private var retryingParameters = false

  /**
   * Wrap the raw message with clean accessors, when a value is set, apply the change to the target
   */
  class ParamValue extends ROParamValue {
    def setValue(v: Float) {
      val p = raw.getOrElse(throw new Exception("Can not set uninited param"))

      p.param_value = v
      log.debug("Telling device to set value: " + this)
      sendMavlink(paramSet(p.getParam_id, p.param_type, v))

      // Readback to confirm the change happened
      requestParameter(p.param_index)
    }
  }

  protected def onParametersDownloaded() {
    setStreamEnable(true) // Turn streaming back on

    log.info("Downloaded " + parameters.size + " parameters!")
    parameters = parameters.sortWith { case (a, b) => a.getId.getOrElse("ZZZ") < b.getId.getOrElse("ZZZ") }

    // We only index params with valid ids
    val known = parameters.flatMap { p =>
      p.getId.map { id => id -> p }
    }
    parametersById = Map(known: _*)

    eventStream.publish(MsgParametersDownloaded)
  }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {

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
      else
        eventStream.publish(MsgParameterReceived(index))

    case FinishParameters =>
      readNextParameter()
  }

  protected def startParameterDownload() {
    retryingParameters = false

    // Turn off streaming because those crummy XBee adapters seem to drop critical parameter responses
    setStreamEnable(false)

    log.info("Requesting vehicle parameters")
    sendWithRetry(paramRequestList(), classOf[msg_param_value])
    MockAkka.scheduler.scheduleOnce(20 seconds, ParametersModel.this, FinishParameters)
  }

  private def requestParameter(i: Int) {
    sendWithRetry(paramRequestRead(i), classOf[msg_param_value], { () =>
      // We failed, just tell everyone we are done
      onParametersDownloaded()
    })
  }

  /**
   * If we are still missing parameters, try to read again
   */
  private def readNextParameter() {
    val wasMissing = parameters.zipWithIndex.find {
      case (v, i) =>
        val hasData = v.raw.isDefined
        if (!hasData)
          requestParameter(i)

        !hasData // Stop here?
    }.isDefined

    retryingParameters = wasMissing
    if (!wasMissing) {
      onParametersDownloaded() // Yay - we have everything!
    }
  }
}

