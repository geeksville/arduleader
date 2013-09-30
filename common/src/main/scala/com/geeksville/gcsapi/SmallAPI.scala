package com.geeksville.gcsapi

import com.ridemission.rest.JValue
import com.ridemission.rest.JArray
import com.ridemission.rest.JObject

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
  /**
   * Expected to return a JObject with three JString array members, "getters", "setters" and "methods"
   */
  def members: JObject = throw new Exception("FIXME we might want to remove this")

  def get(memberName: String): JValue
  def set(memberName: String, v: JValue) { throw new Exception("FIXME not implemented") }

  def call(methodName: String, arguments: JArray) = throw new Exception("FIXME not implemented")
}

