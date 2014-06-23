package com.geeksville.akka

import akka.actor.ActorRef
import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.Terminated
import akka.actor.ReceiveTimeout
import akka.actor.PoisonPill
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Promise
import akka.actor.ActorContext
import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.ActorRefFactory

class Stopper(target: ActorRef, result: Promise[Boolean]) extends Actor {
  // Terminated will be received when target has been stopped
  context watch target
  target ! PoisonPill
  // ReceiveTimeout will be received if nothing else is received within the timeout
  context.setReceiveTimeout(10 seconds)

  def receive = {
    case Terminated(a) ⇒
      result.complete(Success(true))
      self ! PoisonPill
    case ReceiveTimeout ⇒
      result.complete(Failure(
        new Exception("Failed to stop [%s] within [%s]".format(target.path, context.receiveTimeout))))
      self ! PoisonPill
  }
}

/**
 * Ask an actor to stop, and then wait for it.  If it fails to stop an exception will be thrown
 */
object Stopper {
  /**
   * Ask an actor to stop, and then wait for it.  If it fails to stop an exception will be thrown
   */
  def stop(target: ActorRef)(implicit context: ActorRefFactory) {
    import scala.concurrent.ExecutionContext.Implicits.global

    val f = Promise[Boolean]()
    val stopper = context.actorOf(Props(new Stopper(target, f)))
    f.future.onComplete {
      case Success(x) ⇒
        println("Successfully terminated: " + target)
      case Failure(e) ⇒
        throw e
    }
  }
}
