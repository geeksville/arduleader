package com.geeksville.util

import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Date

object DateTools {

  val utcTZ = TimeZone.getTimeZone("UTC");
  val df8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  df8601.setTimeZone(utcTZ)

  def toISO8601(d: Date) = df8601.format(d)
  def fromISO8601(d: String) = df8601.parse(d)
}