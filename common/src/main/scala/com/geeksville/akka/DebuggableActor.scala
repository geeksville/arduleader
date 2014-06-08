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

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(s"RESTARTING $this due to $reason, message=$message")
    reason.printStackTrace()
    super.preRestart(reason, message)
  }

  override def postStop() {
    log.warning(s"postStop on $this")
    super.postStop()
  }

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