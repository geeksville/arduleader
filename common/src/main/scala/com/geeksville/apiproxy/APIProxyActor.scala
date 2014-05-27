package com.geeksville.apiproxy

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega.msg_heartbeat
import org.mavlink.messages.MAV_TYPE
import scala.collection.mutable.HashMap
import java.util.UUID
import com.geeksville.mavlink.CanSendMavlink
import com.geeksville.mavlink.MavlinkUtils
import java.net.ConnectException
import scala.concurrent.duration._
import java.net.SocketException

/**
 * Base class for (client side) actors that connect to the central API hub.
 * If we receive any mavlink messages we will send them to the server
 */
abstract class APIProxyActor(host: String = APIConstants.DEFAULT_SERVER, port: Int = APIConstants.DEFAULT_TCP_PORT) extends InstrumentedActor with CanSendMavlink {
  import APIProxyActor._

  private var link: Option[GCSHooks] = None

  /// FIXME - we don't yet understand multiple interfaces
  private val interfaceNum = 0

  private val sysIdToVehicleId = HashMap[Int, String]()

  private var loginInfo: Option[LoginMsg] = None

  /// How long should we wait before calling again?
  private var callbackDelayMsec = 5000 // 30 * 60 * 1000

  /// We might need to start missions later - after we have a connection
  private var desiredMission: Option[StartMissionMsg] = None

  var errorMsg: Option[String] = None

  private val callbacks = new GCSCallback {
    def sendMavlink(b: Array[Byte]) {
      val msg = MavlinkUtils.bytesToPacket(b)
      log.debug(s"Forwarding server mavlink to vehicle: $msg")
      handlePacket(msg)
    }
  }

  /// Normally we claim to be a live/controllable vehicle - but subclasses can change this
  protected def isLive = true

  override def postStop() {
    disconnect()
    super.postStop()
  }

  override def onReceive = {
    case x: LoginMsg =>
      disconnect()
      loginInfo = Some(x)
      connect()
    //sender ! errorMsg

    case msg: MAVLinkMessage =>
      // FIXME - use ByteStrings instead!
      handleMessage(msg)

    case x: StartMissionMsg =>
      desiredMission = Some(x)
      perhapsStartMission()

    case StopMissionMsg(keep) =>
      link.foreach { l =>
        l.stopMission(keep)
      }

    case AttemptConnectMsg =>
      if (!link.isDefined)
        connect()
      else
        log.warning("Ignoring stale attempt connect message (we are already connected)")
  }

  /**
   * Call a function, but if it throws a network exception, mark the socket as down and try again later
   */
  private def watchForFailure(block: => Unit) {
    try {
      block
    } catch {
      case ex: ConnectException => // can't reach server
        log.error(s"Can't reach $host. Will try again in $callbackDelayMsec ms")
        scheduleReconnect()

      case ex: SocketException =>
        log.error(s"Lost connection to $host. Will try again in $callbackDelayMsec ms")
        scheduleReconnect()

      case ex: Exception =>
        setError("DroneAPI failure: " + ex.getMessage)
      // We do not try again
    }
  }

  private def perhapsStartMission() {
    // This may fail if we are not already connected
    for {
      l <- link
      m <- desiredMission
    } yield {
      // Resend any old vehicle defs
      sysIdToVehicleId.foreach {
        case (sysId, id) =>
          l.setVehicleId(id, interfaceNum, sysId, isLive)
      }

      l.startMission(m.keep, m.uuid)

      // Success
      desiredMission = None
    }
  }

  private def disconnect() {
    link.foreach { s =>
      log.debug("Closing link to server")
      s.close()
    }
    link = None
  }

  private def connect() = watchForFailure {
    // if we fail to connect we should periodically retry
    try {
      loginInfo.foreach { u =>
        // Now reconnect
        val l = new GCSHooksImpl(host, port)

        // Create user if necessary/possible
        if (l.isUsernameAvailable(u.loginName))
          l.createUser(u.loginName, u.password, u.email)
        else
          l.loginUser(u.loginName, u.password)

        link = Some(l) // Our link is now usable

        // We don't start reading async until we are logged in
        l.setCallback(callbacks)

        perhapsStartMission()
      }
    } catch {
      case ex: CallbackLaterException => // server wants us to callback
        callbackDelayMsec = ex.delayMsec
        setError(s"Server told us to call back later.  Will try again in $callbackDelayMsec ms")

        scheduleReconnect()
    }
  }

  private def setError(msg: String) {
    log.error(msg)
    errorMsg = Some(msg)
  }

  private def scheduleReconnect() {
    val system = context.system
    import system.dispatcher

    disconnect() // We are currently down
    system.scheduler.scheduleOnce(callbackDelayMsec milliseconds, self, AttemptConnectMsg)
  }

  private def handleMessage(msg: MAVLinkMessage) = watchForFailure {
    handleSysId(msg)

    link.foreach { l =>
      //log.debug(s"Sending to server: $msg")
      l.filterMavlink(interfaceNum, msg.encode)
    }
  }

  /**
   * The first time we see a vehicle heartbeat we will assign a vehicle ID
   */
  private def handleSysId(msg: MAVLinkMessage) {
    // val typ = msg.`type`
    val sysId = msg.sysId
    if (!sysIdToVehicleId.contains(sysId)) {
      val isGCS = sysId > 200 // FIXME - super skanky
      val id = if (isGCS)
        "GCS"
      else {
        val u = UUID.randomUUID() // FIXME - instead we should pick a machine specific UUID, a different one for each possible sysID
        u.toString
      }

      sysIdToVehicleId += (sysId -> id)
      link.foreach(_.setVehicleId(id, interfaceNum, sysId, isLive))
    }
  }
}

object APIProxyActor {
  /// Opens connection and logs into server
  case class LoginMsg(loginName: String, password: String, email: Option[String])

  case class StartMissionMsg(keep: Boolean, uuid: UUID = UUID.randomUUID)

  /// you must send this if you want the mission to be properly closed
  case class StopMissionMsg(keep: Boolean)

  /// Normally this message is sent only privately - but if we are not connected it will try to connect
  case object AttemptConnectMsg

  /// Use only for testing
  def testAccount = {
    val username = "test-bob"
    val email = "test-bob@3drobotics.com"
    val psw = "sekrit"
    APIProxyActor.LoginMsg(username, psw, Some(email))
  }
}