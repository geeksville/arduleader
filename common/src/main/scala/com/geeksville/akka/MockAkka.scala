package com.geeksville.akka

import com.geeksville.logback.Logger
import com.geeksville.logback.Logging
import scala.collection.mutable.HashSet
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

object MockAkka extends Logging {

  // The system event stream
  val eventStream = new EventStream

  /**
   * If you'd like to use a non standard config (i.e. android) you can set configOverride early in application start
   */
  var configOverride: Option[Config] = None

  // For some platforms (android) it is useful to pass in a non default class loader
  var classLoader = getClass.getClassLoader

  private lazy val config = configOverride.getOrElse(ConfigFactory.load(classLoader))

  private var currentSystem: Option[ActorSystem] = None

  /// Create a new akka system as needed
  def system = currentSystem.getOrElse {
    currentSystem = Some(ActorSystem("mockakka", config, classLoader))
    currentSystem.get
  }

  def shutdown() {
    system.shutdown()
    system.awaitTermination()
    currentSystem = None // If we use akka again we'll need to start from scratch
  }

  /*
  var scheduler = new Scheduler

  private val actors = HashSet[InstrumentedActor]()

  def actorOf[T <: InstrumentedActor](r: T, name: String = "unspecified") = {
    actors.add(r)
    r.start()
    r
  }


  */
}
