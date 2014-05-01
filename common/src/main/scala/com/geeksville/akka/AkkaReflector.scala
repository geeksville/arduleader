package com.geeksville.akka

import akka.actor.Actor
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.ActorRef
import scala.xml._
import akka.actor.ActorSelection
import scala.collection.immutable.SortedMap

object AkkaReflector {
  case object PollMsg
  case object GetHtmlMsg
}

class AkkaReflector extends InstrumentedActor {
  import AkkaReflector._

  private var debugInfo: SortedMap[ActorRef, Any] = SortedMap.empty

  private def sendPing(a: ActorSelection) {
    a ! Identify(0)
    a ! GetDebugInfo // Debuggable actors will understand this...
  }

  override def onReceive = {
    case ActorIdentity(_, ref) =>
      ref.foreach { a =>
        //log.debug(s"Found actor $a")
        debugInfo += (a -> "Identified")

        // Recurse
        sendPing(context.system.actorSelection(a.path + "/*"))
      }

    case DebugInfoResponse(info) =>
      debugInfo += (sender -> info)

    case PollMsg =>
      startPoll()

    case GetHtmlMsg =>
      sender ! asHtml
  }

  def startPoll() {
    debugInfo = SortedMap.empty
    sendPing(context.system.actorSelection("*"))
  }

  /**
   * Generate a hierchical list of all actors (for debugging)
   */
  def asHtml: Elem = {
    <div class="akka-debug-info">
      <p>{ debugInfo.size } Actors:</p>
      <table>
        {
          debugInfo.map {
            case (k, v) =>
              <tr><td>{ k }</td><td>{ v }</td></tr>
          }
        }
      </table>
    </div>
  }
}