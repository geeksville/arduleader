package com.geeksville.akka

import akka.actor.ActorRef
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import akka.actor.Identify
import scala.concurrent.Future

object AkkaTools {

  // Wait until the specified actor is responding to msgs
  def waitAlive(actor: ActorRef): Future[Any] = {
    implicit val timeout = Timeout(30 second)
    actor ? Identify(0L)
  }
}