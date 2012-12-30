package com.geeksville

// Standard akka imports
import akka.actor._
import akka.util.Duration
import akka.util.duration._

package object flight {
  /**
   * Our global akka system (use a name convention similar to playframework)
   */
  val Akka = ActorSystem("flight")
}