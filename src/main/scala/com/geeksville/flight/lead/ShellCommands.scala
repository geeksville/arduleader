package com.geeksville.flight.lead

import com.geeksville.logback.MuteAllFilter

/**
 * Everything inside this object will end up inside our scala control shell
 */
object ShellCommands {
  /**
   * toggles log messages
   */
  def log() {
    MuteAllFilter.mute = !MuteAllFilter.mute
    println("Logs %s".format(if (MuteAllFilter.mute) "muted" else "unmuted"))
  }
}