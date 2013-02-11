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

/**
 * An endpoint client that talks to a vehicle (adds message retries etc...)
 */
class VehicleClient extends HeartbeatMonitor with VehicleSimulator {

  case class RetryExpired(ctx: RetryContext)

  private val retries = HashSet[RetryContext]()

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

  override def systemId = 253 // We always claim to be a ground controller (FIXME, find a better way to pick a number)

  override def onReceive = mReceive.orElse(super.onReceive)

  private def mReceive: Receiver = {

    case RetryExpired(ctx) =>
      ctx.doRetry()
  }

  override def postStop() {
    // Close off any retry timers
    retries.toList.foreach(_.close())

    super.postStop()
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
        retryTimer = Some(MockAkka.scheduler.scheduleOnce(retryInterval milliseconds, VehicleClient.this, RetryExpired(this)))
      } else {
        log.error("No more retries, giving up: " + retryPacket)
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
   * Check to see if this satisfies our retry reply requirement, if it does and it isn't a dup return the message, else None
   */
  protected def checkRetryReply[T <: MAVLinkMessage](reply: T): Option[T] = {
    val numHandled = retries.count(_.handleRetryReply(reply))
    if (numHandled > 0) {
      Some(reply)
    } else
      None
  }
}

