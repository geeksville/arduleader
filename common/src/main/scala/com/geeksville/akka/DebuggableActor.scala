package com.geeksville.akka

import akka.actor.Actor
import akka.actor.ActorLogging

case object GetDebugInfo

/**
 * Returns debugging information about this actor.  Typically either as a string or XML.
 * You can assume that the recipient will probably toString the response (but not necessarily)
 */
case class DebugInfoResponse(info: Any)

trait DebuggableActor extends Actor with ActorLogging with UncaughtExceptionActor {

  protected def debuggingInfo: Any = toString

  override def unhandled(message: Any): Unit = {
    message match {
      case GetDebugInfo ⇒
        println(s"Replying to $sender with $debuggingInfo")
        sender ! DebugInfoResponse(debuggingInfo)
      case _ ⇒
        super.unhandled(message)
    }
  }

}