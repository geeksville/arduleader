package com.geeksville.apiproxy

import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink.MavlinkEventBus
import org.mavlink.messages.MAVLinkMessage
import akka.actor.Props
import akka.actor.ActorRefFactory

/**
 * Listens on the mavlink event bus for packets, and sends them to the server
 */
class LiveUploader extends APIProxyActor {
  MavlinkEventBus.subscribe(self, -1)

  // Messages will now be getting delivered to the proxy actor
}

object LiveUploader {
  def create(context: ActorRefFactory) = {
    val r = context.actorOf(Props(new LiveUploader))

    // FIXME use real username
    r ! APIProxyActor.LoginMsg("test-bob@3drobotics.com", "sekrit")
    r
  }
}