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
    range
  }

  def estimateRangePair(n: msg_radio, curDist: Float) = {
    val fadeMargin = 5 // Per Michael O
    val localFadeDb = math.max((n.rssi - n.noise) / 2.0f - fadeMargin, 0.0f)
    val remFadeDb = math.max((n.remrssi - n.remnoise) / 2.0f - fadeMargin, 0.0f)

    estimateRange(localFadeDb, curDist) -> estimateRange(remFadeDb, curDist)
  }
}