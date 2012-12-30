package com.geeksville.flight.test

import java.io._
import org.mavlink._
import com.geeksville.mavlink.UDPMavlinkReceiver
import com.geeksville.flight._
import akka.actor._
import com.geeksville.mavlink.LogIncomingMavlink

object ParseMavlink extends App {
  println("ParseMavlink running...")

  // FIXME create this somewhere else
  (new UDPMavlinkReceiver)

  Akka.actorOf(Props[LogIncomingMavlink], "mavlog")

  Thread.sleep(1000 * 60 * 10)
}