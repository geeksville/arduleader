package com.geeksville.apiproxy

import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.MavlinkEventBus
import org.mavlink.messages.MAVLinkMessage
import akka.actor.Props
import akka.actor.ActorRefFactory
import com.geeksville.mavlink.MavlinkReceiver
import akka.actor.PoisonPill

case object StopMissionAndExitMsg

/**
 * Listens on the mavlink event bus for packets, and sends them to the server
 */
class LiveUploader(override val isLive: Boolean, host: String, port: Int = APIConstants.DEFAULT_TCP_PORT) extends APIProxyActor(host, port) with MavlinkReceiver {
  import APIProxyActor._

  self ! StartMissionMsg(false) // If we fail to close don't keep the mission

  MavlinkEventBus.subscribe(self, -1)
  // Messages will now be getting delivered to the proxy actor

  override def onReceive = ({
    case StopMissionAndExitMsg =>
      log.warning("Someone wants us to exit - close our mission")
      // If somone asks us to exit, then _before_ exiting we will terminate any mission we were working on
      self ! StopMissionMsg(true) // We completed the session normally - go ahead and keep the whole file
      self ! PoisonPill // This request to stop will be handled normally

  }: Receive).orElse(super.onReceive)
}

object LiveUploader {
  def create(context: ActorRefFactory, login: APIProxyActor.LoginMsg, host: String, isLive: Boolean) = {
    val r = context.actorOf(Props(new LiveUploader(isLive, host)))

    // Creates account as needed

    r ! login
    r
  }
}