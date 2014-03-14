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

    // Create account as needed (FIXME) - pull out to expose separately in GUI
    val username = "test-bob"
    val email = "test-bob@3drobotics.com"
    val psw = "sekrit"

    r ! APIProxyActor.LoginMsg(username, psw, Some(email))
    r
  }
}