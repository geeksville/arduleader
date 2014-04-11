package com.geeksville.akka

import akka.actor.Actor
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.ActorRef
import scala.xml._

object AkkaReflector {
  case object PollMsg
  case object GetHtmlMsg
}

class AkkaReflector extends InstrumentedActor {
  import AkkaReflector._

  private var allActors: List[ActorRef] = Nil

  override def onReceive = {
    case ActorIdentity(_, ref) =>
      ref.foreach { a =>
        //log.debug(s"Found actor $a")
        allActors = a :: allActors

        // Recurse
        context.system.actorSelection(a.path + "/*") ! Identify(0)
      }

    case PollMsg =>
      startPoll()

    case GetHtmlMsg =>
      sender ! asHtml
  }

  def startPoll() {
    allActors = Nil
    context.system.actorSelection("*") ! Identify(0)
  }

  /**
   * Generate a hierchical list of all actors (for debugging)
   */
  def asHtml: Elem = {
    <html>
      <body>
        <p>Actors:</p>
        <ul>
          {
            allActors.sorted.map { a =>
              <li> { a } </li>
            }
          }
        </ul>
      </body>
    </html>
  }
}