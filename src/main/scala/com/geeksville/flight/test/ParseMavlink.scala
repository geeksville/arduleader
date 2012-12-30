package com.geeksville.flight.test

import java.io._
import org.mavlink._
import com.geeksville.mavlink.UDPMavlinkReceiver

object ParseMavlink extends App {
  println("ParseMavlink running...")

  (new UDPMavlinkReceiver).thread.join
}