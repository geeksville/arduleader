package com.geeksville.apiproxy

import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.MavlinkEventBus
import org.mavlink.messages.MAVLinkMessage
import akka.actor.Props
import akka.actor.ActorRefFactory
import com.geeksville.mavlink.MavlinkReceiver

/**
 * Listens on the mavlink event bus for packets, and sends them to the server
 */
class LiveUploader(override val isLive: Boolean) extends APIProxyActor with MavlinkReceiver {
  import APIProxyActor._

  self ! StartMissionMsg(false) // If we fail to close don't keep the mission

  MavlinkEventBus.subscribe(self, -1)
  // Messages will now be getting delivered to the proxy actor

  override def postStop() {
    self ! StopMissionMsg(true) // We completed the session normally - go ahead and keep the whole file

    super.postStop()
  }
}

object LiveUploader {
  def create(context: ActorRefFactory, login: APIProxyActor.LoginMsg, isLive: Boolean) = {
    val r = context.actorOf(Props(new LiveUploader(isLive)))

    // Creates account as needed

    r ! login
    r
  }
}