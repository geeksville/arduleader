package com.geeksville

// Standard akka imports
import _root_.akka.actor._
import _root_.akka.util.Duration
import _root_.akka.util.duration._

package object flight {
  /**
   * Our global akka system (use a name convention similar to playframework)
   */
  val Akka = ActorSystem("flight")
}