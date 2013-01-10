package com.geeksville.akka

import akka.actor._
import akka.util._
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Await

private case class GetOrCreate(id: String, prop: Props)
private case class Get(id: String)

/**
 * An actor which keeps a set of uniquely named children - and monitors their health
 */
class NamedActorSupervisor extends InstrumentedActor {

  implicit val timeout = Timeout(10 second)
  var as = Map.empty[String, ActorRef]

  def getActor(id: String, props: Props) = as get id getOrElse {
    val c = context.actorOf(props, id)
    as += id -> c
    context watch c
    log.debug("created actor " + id)
    c
  }

  def receive = {

    case GetOrCreate(id, props) => {
      sender ! getActor(id, props)
    }

    case Get(id) =>
      sender ! as.get(id)

    case Terminated(ref) => {
      log.debug("received termination for " + ref)
      as = as - ref.path.name
    }
  }
}

/**
 * Client glue for an actor that contains numerous well known children
 */
class NamedActorClient(val name: String) {
  private var supervisorRef: Option[ActorRef] = None

  implicit val timeout = Timeout(30 second)

  private def supervisor(implicit context: ActorContext) = {
    // We do this strange test for termination, so we will work correctly in the test harness, which apparently
    // can kill actors without killing every object
    if (!supervisorRef.isDefined || supervisorRef.get.isTerminated) {
      supervisorRef = Some(context.system.actorOf(Props[NamedActorSupervisor], name))
    }

    supervisorRef.get
  }

  /**
   * Get or create an actor with a particular ID
   */
  def getOrCreate(id: String, props: Props)(implicit context: ActorContext): ActorRef = {
    val f = (supervisor ? GetOrCreate(id, props))
    Await.result(f, timeout.duration).asInstanceOf[ActorRef]
  }

  /**
   * Get or create an actor with a particular ID
   */
  def get(id: String)(implicit context: ActorContext): Option[ActorRef] = {
    val f = (supervisor ? Get(id))
    Await.result(f, timeout.duration).asInstanceOf[Option[ActorRef]]
  }
}

