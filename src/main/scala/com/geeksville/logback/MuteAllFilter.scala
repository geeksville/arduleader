package com.geeksville.logback

import ch.qos.logback.core.filter.Filter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.spi.FilterReply
import scala.reflect.BeanProperty
import ch.qos.logback.classic.Level

/**
 * If mute is set to true, then turn off all logging (useful to globally toggle logs at runtime)
 */
class MuteAllFilter extends Filter[ILoggingEvent] {

  override def decide(e: ILoggingEvent) = if (MuteAllFilter.mute) FilterReply.DENY else FilterReply.ACCEPT
}

object MuteAllFilter {
  var mute = false
}