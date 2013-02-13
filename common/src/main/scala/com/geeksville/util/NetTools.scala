package com.geeksville.util

import java.net.NetworkInterface
import scala.collection.JavaConverters._
import java.net.Inet4Address

object NetTools {

  lazy val localIPAddresses = {
    val interfaces = NetworkInterface.getNetworkInterfaces().asScala
    val iface = interfaces.filter { i => i.isUp && !i.isLoopback && !i.isVirtual }
    iface.flatMap { i =>
      val addrs = i.getInetAddresses.asScala
      addrs.flatMap { a =>
        a match {
          case x: Inet4Address => Some(x.getHostAddress)
          case _ => None
        }
      }
    }.toSeq
  }
}