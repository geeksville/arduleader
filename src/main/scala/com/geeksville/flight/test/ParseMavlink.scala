package com.geeksville.flight.test

import java.io._
import org.mavlink._
import com.geeksville.mavlink.MavlinkReceiver
import com.geeksville.flight._
import akka.actor._
import com.geeksville.mavlink.LogIncomingMavlink

object ParseMavlink extends App {
  println("ParseMavlink running...")

  // FIXME create this somewhere else
  Akka.actorOf(Props[MavlinkReceiver], "mavrx")

  Akka.actorOf(Props[LogIncomingMavlink], "mavlog")

  Thread.sleep(1000 * 60 * 10)
}