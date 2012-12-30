package com.geeksville.logback

import ch.qos.logback.core.filter.Filter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.spi.FilterReply
import scala.reflect.BeanProperty
import ch.qos.logback.classic.Level

/**
 * If a particular package/classname matches, the decide logging based on the level of the message
 */
class NameLevelFilter extends Filter[ILoggingEvent] {

  @BeanProperty
  var prefix = ""

  @BeanProperty
  var minLevel = Level.ALL

  override def decide(e: ILoggingEvent) = {
    if (!e.getLoggerName.startsWith(prefix))
      FilterReply.NEUTRAL
    else if (e.getLevel.isGreaterOrEqual(minLevel))
      FilterReply.ACCEPT
    else
      FilterReply.DENY
  }
}