package com.geeksville.gcsapi

import com.ridemission.rest.JValue
import com.ridemission.rest.JArray

/**
 * This is a 'small surface area' API which intended to be easy to expose through various different mechanisms (REST server, Android WebView scripting, etc...)
 *
 * It uses three types of 'operations' (GET, SET, CALL) that can be applied against 'instances'.  There will be a small number of instances exposed via
 * this API (only 'vehicle' and 'gcs' initially).  The intent is that this SmallAPI could be easily accessed with minimal glue and reused in lots of different
 * (non drone) projects.
 *
 * All returned/accepted types are intended to be very simple - either a JSONish object, scalar or array.
 *
 * The intent is that you'd use SmallAdapter to generate this JSONish API from a higher level representation. Anyone _using_ SmallAPI would know only
 * about this simple interface.
 */
trait SmallAPI {
  def members: JArray

  def get(memberName: String): JValue
  def set(memberName: String, v: JValue)

  def call(methodName: String, arguments: JArray): JValue
}

