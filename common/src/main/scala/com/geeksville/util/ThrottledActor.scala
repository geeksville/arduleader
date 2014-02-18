package com.geeksville.util

import java.util.concurrent._

/* Not yet updated for akka
 
import scala.actors._

/// A mixin to throttle the actor send queue, once the queue reaches a certain size
/// we will stall anyone who tries to send to us.
trait ThrottledActor extends Actor {

  // By default we only allow one message at a time
  private val semaphore = new Semaphore(1)

  /// Set the max # of messages which can be enqueued (defaults to 1)
  /// You should only call this method before starting the actor
  protected def setThrottle(maxMessages: Int) {
    semaphore.drainPermits()
    (0 until maxMessages).foreach(semaphore.release)
  }

  override def react(handler: PartialFunction[Any, Unit]) = {
    def extraOps: PartialFunction[Unit, Unit] = {
      case _ =>
        handledMessage()
    }

    super.react(handler andThen extraOps)
  }

  override def send(msg: Any, replyTo: OutputChannel[Any]) {
    receivedMessage()
    super.send(msg, replyTo)
  }

  private def receivedMessage() {
    if (!semaphore.tryAcquire) {
      // We need to block
      println("Will block: " + this)
      // Thread.dumpStack()
      semaphore.acquire()
      println("Done block: " + this)
    }
  }

  private def handledMessage() { semaphore.release() }

}
*/ 