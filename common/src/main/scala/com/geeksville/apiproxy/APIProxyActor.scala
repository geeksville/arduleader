package com.geeksville.apiproxy

import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import org.mavlink.messages.ardupilotmega.msg_heartbeat
import org.mavlink.messages.MAV_TYPE
import scala.collection.mutable.HashMap
import java.util.UUID
import com.geeksville.mavlink.CanSendMavlink
import com.geeksville.mavlink.MavlinkUtils

/**
 * Base class for (client side) actors that connect to the central API hub.
 * If we receive any mavlink messages we will send them to the server
 */
trait APIProxyActor extends InstrumentedActor with CanSendMavlink {
  import APIProxyActor._

  private var link: Option[GCSHooks] = None

  /// FIXME - we don't yet understand multiple interfaces
  private val interfaceNum = 0

  private val sysIdToVehicleId = HashMap[Int, String]()

  private var loginInfo: Option[LoginMsg] = None

  private val callbacks = new GCSCallback {
    def sendMavlink(b: Array[Byte]) {
      val msg = MavlinkUtils.bytesToPacket(b)
      log.debug(s"Client received mavlink from server: $msg")
      handlePacket(msg)
    }
  }

  override def postStop() {
    disconnect()
    super.postStop()
  }

  override def onReceive = {
    case x: LoginMsg =>
      disconnect()
      loginInfo = Some(x)
      connect()

    case msg: msg_heartbeat =>
      handleSysId(msg)
      handleMessage(msg)

    case msg: MAVLinkMessage =>
      // FIXME - use ByteStrings instead!
      handleMessage(msg)
  }

  private def disconnect() {
    link.foreach { s =>
      log.debug("Closing link to server")
      s.close()
    }
    link = None
  }

  private def connect() {
    // FIXME - if we fail to connect we should periodically retry
    loginInfo.foreach { u =>
      // Now reconnect
      val l = new GCSHooksImpl

      link = Some(l)

      // Create user if necessary/possible
      if (l.isUsernameAvailable(u.loginName))
        l.createUser(u.loginName, u.password, u.email)
      else
        l.loginUser(u.loginName, u.password)

      // We don't start reading async until we are logged in
      l.setCallback(callbacks)

      // Resend any old vehicle defs
      sysIdToVehicleId.foreach {
        case (sysId, id) =>
          l.setVehicleId(id, interfaceNum, sysId, true)
      }
    }
  }

  private def handleMessage(msg: MAVLinkMessage) {
    link.foreach { l =>
      l.filterMavlink(interfaceNum, msg.encode)
    }
  }

  /**
   * The first time we see a vehicle heartbeat we will assign a vehicle ID
   */
  private def handleSysId(msg: msg_heartbeat) {
    val typ = msg.`type`
    val sysId = msg.sysId
    if (!sysIdToVehicleId.contains(sysId)) {
      val isGCS = typ == MAV_TYPE.MAV_TYPE_GCS
      val id = if (isGCS)
        "GCS"
      else {
        val u = UUID.randomUUID() // FIXME - instead we should pick a machine specific UUID, a different one for each possible sysID
        u.toString
      }

      sysIdToVehicleId += (sysId -> id)
      link.foreach(_.setVehicleId(id, interfaceNum, sysId, true))
    }
  }
}

object APIProxyActor {
  case class LoginMsg(loginName: String, password: String, email: Option[String])
}