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

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
trait ParametersModel extends VehicleClient {

  case object FinishParameters

  var parameters = new Array[ParamValue](0)
  private var retryingParameters = false

  lazy val paramDocs = (new ParameterDocFile).forVehicle(vehicleTypeName)

  /**
   * Wrap the raw message with clean accessors, when a value is set, apply the change to the target
   */
  class ParamValue {
    private[ParametersModel] var raw: Option[msg_param_value] = None

    /// The docs for this parameter (if we can find them)
    def docs = for { id <- getId; d <- paramDocs.get(id) } yield { d }

    def getId = raw.map(_.getParam_id)

    def getValue = raw.map { v =>
      val asfloat = v.param_value

      raw.get.param_type match {
        case MAVLINK_TYPE_FLOAT => asfloat: AnyVal
        case _ => asfloat.toInt: AnyVal
      }
    }

    /**
     * @return a nice human readable version of this value (decoding based on documentation if possible)
     */
    def asString = {
      (for {
        v <- raw
        doc <- docs
        asstr <- doc.decodeValue(v.param_value)
      } yield {
        Some(asstr)
      }).getOrElse(getValue.map(_.toString))
    }

    def setValue(v: Float) {
      val p = raw.getOrElse(throw new Exception("Can not set uninited param"))

      p.param_value = v
      log.debug("Telling device to set value: " + this)
      sendMavlink(paramSet(p.getParam_id, p.param_type, v))
    }

    override def toString = (for { id <- getId; v <- getValue } yield { id + " = " + v }).getOrElse("undefined")
  }

  /// Either ArduCopter or ArduPlane etc... used to find appropriate parameter docs
  def vehicleTypeName: String

  private def onParametersDownloaded() { eventStream.publish(MsgParametersDownloaded) }

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

    case FinishParameters =>
      readNextParameter()
  }

  protected def startParameterDownload() {
    retryingParameters = false
    log.info("Requesting vehicle parameters")
    sendWithRetry(paramRequestList(), classOf[msg_param_value])
    MockAkka.scheduler.scheduleOnce(20 seconds, ParametersModel.this, FinishParameters)
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

}

