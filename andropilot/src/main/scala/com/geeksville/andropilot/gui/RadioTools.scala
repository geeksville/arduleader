package com.geeksville.andropilot.gui

import org.mavlink.messages.ardupilotmega.msg_radio
import com.ridemission.scandroid.AndroidLogger

object RadioTools extends AndroidLogger {

  /**
   * Given a current distance in meters and a fadeMargin, estimate the distance where we will lose comms
   */
  def estimateRange(fadeMargin: Float, curDist: Float) = {
    // Per Tridge
    val range = curDist * math.pow(2.0, fadeMargin / 6)
    debug(s"margin $fadeMargin, curDist $curDist => range=$range")
  }

  def estimateRangePair(n: msg_radio, curDist: Float) = {
    val localFadeDb = (n.rssi - n.noise) / 2.0f
    val remFadeDb = (n.remrssi - n.remnoise) / 2.0f

    estimateRange(localFadeDb, curDist) -> estimateRange(remFadeDb, curDist)
  }
}