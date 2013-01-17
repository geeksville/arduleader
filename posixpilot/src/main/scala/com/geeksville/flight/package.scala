package com.geeksville

// Standard akka imports
import scala.concurrent._
import com.geeksville.akka.MockAkka

package object flight {
  /**
   * Our global akka system (use a name convention similar to playframework)
   */
  def Akka = MockAkka // ActorSystem("flight")
}