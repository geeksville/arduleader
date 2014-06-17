package com.geeksville.mavlink

import java.io._
import com.geeksville.akka.InstrumentedActor
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.util.ThreadTools
import com.geeksville.util.Using._
import org.mavlink._
import com.geeksville.util.DebugInputStream
import com.geeksville.util.ByteOnlyInputStream
import com.geeksville.util.Throttled
import com.geeksville.logback.Logging
import java.net.ConnectException
import scala.concurrent._
import scala.util.Random
import java.net.SocketTimeoutException
import akka.actor.PoisonPill

// with SerialPortEventListener

/**
 * Sends and receives mavlink out a serial port
 *
 * @param sysIdOverride if set, we will replace any received sysIds with this alternative (useful for remapping sysId based on interface)
 * @param tlogSpeedup if specified, we expect to see an 8 byte timestamp before each msg.  We will play the read data back at the rate we find in the file.
 */
class MavlinkStream(
  outgen: => OutputStream,
  ingen: => InputStream,
  sysIdOverride: Option[Int] = None,
  tlogSpeedup: Option[Double] = None) extends MavlinkStreamReceiver(ingen, sysIdOverride, tlogSpeedup) with MavlinkSender {

  /**
   * We use generators to init these variables, because android doesn't allow network access from the
   * 'main' thread
   */
  private lazy val out = outgen

  override def onReceive = {
    super[MavlinkStreamReceiver].onReceive.orElse(super[MavlinkSender].onReceive)
  }

  protected def doSendMavlink(bytes: Array[Byte]) {
    //log.debug("Sending ser (sysId=%d): %s".format(msg.sysId, msg))

    try {
      blocking {
        out.write(bytes)
        out.flush()
      }
    } catch {
      case ex: IOException =>
        log.error("Error sending packet: " + ex.getMessage)
    }
  }
}
