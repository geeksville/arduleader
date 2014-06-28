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
import org.mavlink.messages.MAV_DATA_STREAM
import org.mavlink.messages.MAV_MISSION_RESULT
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashSet
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.mavlink.MavlinkStream
import com.geeksville.akka.InstrumentedActor
import akka.actor.Cancellable

//
// Messages we publish on our event bus when something happens
//
case object MsgParametersDownloaded

// We just received a new parameter (after the initial download)
case class MsgParameterReceived(index: Int)

/**
 * A download progress update
 * primary is a number between 0 and 100 showing the amount of 'primary' progress (each big download pass)
 * secondary is a number between 0 and 100 showing per param progress
 */
case class MsgParameterDownloadProgress(primary: Int, secondary: Int)

/**
 * Listens to a particular vehicle, capturing interesting state like heartbeat, cur lat, lng, alt, mode, status and next waypoint
 */
trait ParametersModel extends VehicleClient with LiveOrPlaybackModel with ParametersReadOnlyModel {

  import context._

  case object FinishParameters

  val paramLogThrottle = new Throttled(5000)

  var parameters = new Array[ParamValue](0)
  var parametersById = Map[String, ParamValue]()

  var autoParameterDownload = true

  /**
   * If we just connected to a vehicle it might be still sending stale param messages from some previous session.  Ignore them.
   */
  private var hasRequestedParameters = false

  private var retryingParameters = false
  var unsortedParameters = new Array[ParamValue](0)

  var finisher: Option[Cancellable] = None

  /**
   * This feature is not supported on older arducopter builds
   */
  var useRequestById = true

  private val maxNumAttempts = 10
  private var numAttemptsRemaining = maxNumAttempts

  final def hasParameters = !parametersById.isEmpty

  /**
   * Given a modeNum from 1 to 6, return the mode that is assigned to that position (if known)
   */
  final def getFlightMode(modeNum: Int): Option[Int] = parametersById.get(flightModePrefix + modeNum).flatMap(_.getInt)

  /**
   * What RC channel # is used to provide the mode switch pwm data? (if known)
   */
  final def rcModeChannel: Option[Int] = isCopterOpt.flatMap { isCopter =>
    if (isCopter)
      Some(5)
    else
      parametersById.get(flightModePrefix + "_CH").flatMap(_.getInt)
  }

  /**
   * How many params do we want to find
   */
  private def numParametersDesired = unsortedParameters.size

  /**
   * Wrap the raw message with clean accessors, when a value is set, apply the change to the target
   */
  class ParamValue extends ROParamValue {
    def setValueNoAck(v: Float) = {
      val p = raw.getOrElse(throw new Exception("Can not set uninited param"))

      p.param_value = v
      val msg = paramSet(p.getParam_id, p.param_type, v)
      log.debug("Setting param: " + msg + " from=" + p)
      sendMavlink(msg)
      p
    }

    def setValue(v: Float) {
      val p = setValueNoAck(v)

      // Readback to confirm the change happened
      reread()
    }

    def reread() {
      val p = raw.getOrElse(throw new Exception("Can not reread uninited param"))
      requestParameterById(p.getParam_id)
    }
  }

  private def sendProgress(secondary: Int) {
    val primary = 100 * (maxNumAttempts - numAttemptsRemaining - 1) / maxNumAttempts

    publishEvent(MsgParameterDownloadProgress(primary, secondary))
  }

  override protected def onHeartbeatFound() {
    resetParameters()
    super.onHeartbeatFound()
  }

  /**
   * Reinit state (FIXME - yucky)
   */
  private def resetParameters() {
    hasRequestedParameters = false // Ignore any stale params
    parameters = Array.empty
    parametersById = Map.empty
    retryingParameters = false
    unsortedParameters = Array.empty
    numAttemptsRemaining = maxNumAttempts

    finisher.foreach(_.cancel())
    finisher = None
  }

  protected def onParametersDownloaded() {
    setStreamEnable(true) // Turn streaming back on

    log.info("Downloaded " + unsortedParameters.size + " parameters!")
    parameters = unsortedParameters.sortWith { case (a, b) => a.getId.getOrElse("ZZZ") < b.getId.getOrElse("ZZZ") }

    // We only index params with valid ids
    val known = parameters.flatMap { p =>
      p.getId.map { id => id -> p }
    }
    parametersById = Map(known: _*)

    publishEvent(MsgParametersDownloaded)
  }

  /**
   * We only want to stop downloading _once_ if we get multiple completions
   */
  private def perhapsParametersDownloaded() {
    if (!hasParameters)
      onParametersDownloaded()
    else
      log.warning("Ignoring stale parameter update")
  }

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: InstrumentedActor.Receiver = {

    //
    // Messages for downloading parameters from vehicle

    case msg: msg_param_value =>
      if (hasRequestedParameters)
        handleParameterMessage(msg)
      else
        paramLogThrottle.withIgnoreCount { numIgnored: Int =>
          log.warning(s"Ignoring stale parameter: $msg (and $numIgnored others)")
        }

    case FinishParameters =>
      log.info("Handling finish parameters")
      if (useRequestById)
        readNextParameter()
      else
        perhapsDownloadParameters()
  }

  private def handleParameterMessage(msg: msg_param_value) {
    // log.debug("Receive: " + msg)
    restartFinisher()
    checkRetryReply(msg)
    if (msg.param_count != unsortedParameters.size) {
      // Resize for new parameter count
      unsortedParameters = ArrayBuffer.fill(msg.param_count)(new ParamValue).toArray
    }

    log.debug("Received param: " + msg)

    var index = msg.param_index
    if (index == 65535) { // Apparently means unknown index, so look up by id
      val idstr = msg.getParam_id

      index = unsortedParameters.find { p =>
        p.getId.getOrElse("") == idstr
      }.flatMap(_.raw).map(_.param_index).getOrElse(-1)

      // We now know where this param belongs
      msg.param_index = index
    }

    if (index == -1) {
      // We failed to find this parameter by name - just ignore the message.
      log.error(s"No param found by name for $msg")
    } else {
      if (unsortedParameters(index).raw == None) {
        unsortedParameters(index).raw = Some(msg)
        sendProgress(100 * index / (numParametersDesired - 1))
      }

      // Are we done with our initial download early?  If so, we can publish done right now
      if (finisher.isDefined && msg.param_index == msg.param_count - 1) {
        log.info("Sending early finish")
        finisher.foreach(_.cancel())
        finisher = None
        self ! FinishParameters
      } else if (retryingParameters) {
        // If during our initial download we can use the param index as the index, but later we are sorted and have to do something smarter
        readNextParameter()
      } else {
        // After we have a sorted param list, we will start publishing updates for individual parameters
        val paramNum = parameters.indexWhere(_.raw.map(_.param_index).getOrElse(-1) == index)

        if (paramNum != -1) {
          log.debug("publishing param " + paramNum)
          publishEvent(MsgParameterReceived(paramNum))
        }
      }
    }
  }

  private def startParameterDownload() {
    // We could be called multiple times, because we are called after every waypoint download.  If we already have parameters,
    // don't start over
    if (parametersById.isEmpty) {
      retryingParameters = false

      // Turn off streaming because those crummy XBee adapters seem to drop critical parameter responses
      setStreamEnable(false)

      restartParameterDownload()
    }
  }

  protected def perhapsParameterDownload() {
    if (autoParameterDownload)
      startParameterDownload()
  }

  /**
   * Ask for another batch of parameters
   */
  private def restartParameterDownload() {
    log.info("Requesting vehicle parameters")
    hasRequestedParameters = true
    sendWithRetry(paramRequestList(), classOf[msg_param_value])
    restartFinisher()
  }

  /**
   * We setup a deadman timer after each parameter
   */
  private def restartFinisher() {
    finisher.foreach(_.cancel())
    finisher = Some(system.scheduler.scheduleOnce(4 seconds, self, FinishParameters))
  }

  private def requestParameterByIndex(i: Int) {
    sendWithRetry(paramRequestReadByIndex(i), classOf[msg_param_value], { () =>
      // We failed, just tell everyone we are done
      log.warning("failed read by index")
      perhapsParametersDownloaded()
    })
  }

  private def requestParameterById(id: String) {
    sendWithRetry(paramRequestReadById(id), classOf[msg_param_value])
  }

  private def numMissingParameters() =
    unsortedParameters.count(!_.raw.isDefined)

  /**
   * Check to see if we are missing any params, if we are, try to download again
   */
  private def perhapsDownloadParameters() {
    val numMissing = numMissingParameters
    log.info("Number of missing parameters: " + numMissing)
    if (numMissing > 0 && numAttemptsRemaining > 0) {
      numAttemptsRemaining -= 1
      log.warning("Asking for params, attempts remaining: " + numAttemptsRemaining)
      restartParameterDownload()
    } else
      perhapsParametersDownloaded() // Yay!  Success (or we gave up)
  }

  /**
   * If we are still missing parameters, try to read again (only used if we are reading params by id)
   */
  private def readNextParameter() {
    val firstMissing = unsortedParameters.zipWithIndex.find {
      case (v, i) =>
        val hasData = v.raw.isDefined
        !hasData // Stop here?
    }

    log.info(s"In readNextParameter firstMissing=$firstMissing")

    firstMissing match {
      case Some((_, i)) =>
        retryingParameters = true
        requestParameterByIndex(i)
      case None =>
        retryingParameters = false
        perhapsParametersDownloaded() // Yay - we have everything!
    }
  }
}

