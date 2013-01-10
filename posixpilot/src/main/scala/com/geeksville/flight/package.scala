package com.geeksville

// Standard akka imports
import _root_.akka.actor._
import scala.concurrent._

package object flight {
  /**
   * Our global akka system (use a name convention similar to playframework)
   */
  val Akka = ActorSystem("flight")
}