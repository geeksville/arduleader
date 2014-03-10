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
import com.geeksville.mavlink.MavlinkConstants
import com.geeksville.akka.InstrumentedActor
import akka.actor.Cancellable

/**
 * An endpoint client that talks to a vehicle (adds message retries etc...)
 *
 * @param targetOverride if specified then we will only talk with the specified sysId
 */
abstract class VehicleClient(val targetOverride: Option[Int] = None) extends HeartbeatMonitor with VehicleSimulator with HeartbeatSender with MavlinkConstants {
  import context._

  case class RetryExpired(ctx: RetryContext)

  private var defaultStreamFreq = 1
  private var positionStreamFreq = 3
  private var ahrsStreamFreq = 1

  private val retries = HashSet[RetryContext]()

  /**
   * If an override has been set, use that otherwise try to talk to whatever vehicle we've received heartbeats from
   */
  override def targetSystem = targetOverride.getOrElse {
    heartbeatSysId.getOrElse(1)
  }

  override def systemId = 253 // We always claim to be a ground controller (FIXME, find a better way to pick a number)

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: InstrumentedActor.Receiver = {

    case RetryExpired(ctx) =>
      ctx.doRetry()
  }

  override def postStop() {
    // Close off any retry timers
    retries.toList.foreach(_.close())

    super.postStop()
  }

  case class RetryContext(val retryPacket: MAVLinkMessage, val expectedResponse: Class[_]) {
    val numRetries = 10
    var retriesLeft = numRetries
    val retryInterval = 1000
    var retryTimer: Option[Cancellable] = None

    sendPacket()

    def close() {
      //log.debug("Closing " + this)
      retryTimer.foreach(_.cancel())
      retries.remove(this)
    }

    /**
     * Return true if we handled it
     */
    def handleRetryReply[T <: MAVLinkMessage](reply: T) = {
      if (reply.getClass == expectedResponse) {
        // Success!
        log.debug("Success for " + this)
        close()
        true
      } else
        false
    }

    /**
     * Subclasses can do something more elaborate if they want
     */
    protected def handleFailure() {}

    private def sendPacket() {
      retriesLeft -= 1
      sendMavlink(retryPacket)
      retryTimer = Some(system.scheduler.scheduleOnce(retryInterval milliseconds, self, RetryExpired(this)))
    }

    def doRetry() {
      if (retriesLeft > 0) {
        log.debug(System.currentTimeMillis + " Retry expired on " + this + " trying again...")
        sendPacket()
      } else {
        log.error("No more retries, giving up: " + retryPacket)
        handleFailure()
        close()
      }
    }
  }

  /**
   * Send a packet that expects a certain packet type in response, if the response doesn't arrive, then retry
   */
  protected def sendWithRetry(msg: MAVLinkMessage, expected: Class[_]) {
    retries.add(RetryContext(msg, expected))
  }

  /**
   * Send a packet that expects a certain packet type in response, if the response doesn't arrive, then retry
   */
  protected def sendWithRetry(msg: MAVLinkMessage, expected: Class[_], onFailure: () => Unit) {
    val c = new RetryContext(msg, expected) {
      override def handleFailure() { onFailure() }
    }
    retries.add(c)
  }

  /**
   * Check to see if this satisfies our retry reply requirement, if it does and it isn't a dup return the message, else None
   */
  protected def checkRetryReply[T <: MAVLinkMessage](reply: T): Option[T] = {
    val numHandled = retries.count(_.handleRetryReply(reply))
    if (numHandled > 0) {
      Some(reply)
    } else
      None
  }

  private def setFreq(dest: Int, fIn: Int, enabled: Boolean) {
    val f = if (VehicleClient.isUsbBusted) 1 else fIn
    sendMavlink(requestDataStream(dest, f, enabled))
    sendMavlink(requestDataStream(dest, f, enabled))
  }

  protected def setAhrsFreq(fIn: Int) {
    ahrsStreamFreq = fIn
    setFreq(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1, fIn, true)
  }

  protected def setPositionFreq(fIn: Int) {
    positionStreamFreq = fIn
    setFreq(MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, fIn, true)
  }

  /**
   * Turn streaming on or off (and if USB is crummy on this machine, turn it on real slow)
   */
  protected[flight] def setStreamEnable(enabled: Boolean) {

    log.info("Setting stream enable: " + enabled)

    val interestingStreams = Seq(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS -> defaultStreamFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS -> defaultStreamFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS -> 2,
      MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION -> positionStreamFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1 -> ahrsStreamFreq, // faster AHRS display use a bigger #
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2 -> defaultStreamFreq,
      MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3 -> defaultStreamFreq)

    interestingStreams.foreach {
      case (id, freqHz) =>
        setFreq(id, freqHz, enabled)
    }
  }
}

object VehicleClient {
  /**
   * Some android clients don't have working USB and therefore have very limited bandwidth.  This nasty global allows the android builds to change 'common' behavior.
   */
  var isUsbBusted = false
}
