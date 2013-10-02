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
  val GetMethod = "_get"
  val SetMethod = "_set"

  /// Get by index
  //val GetMethodN = "_getn"

  /**
   * Expected to return a JObject with three JString array members, "getters", "setters" and "methods"
   */
  def members: JObject = throw new Exception("FIXME we might want to remove this")

  // Syntactic sugar to provide access to get/set methods
  def get(objName: String) = call(objName, GetMethod, Seq.empty)
  //def get(objName: String, n: Int) = call(objName, GetMethodN, Seq(n))
  def set(objName: String, v: Any): Unit = call(objName, SetMethod, Seq(v))

  /**
   * Some method names are reserved:
   * _get gets the value of that object
   * _set sets the value of that object args(0) is expected to be an appropriate type (a map, array or simple type)
   */
  def call(objName: String, methodName: String, arguments: Seq[Any]): JValue = throw new Exception("FIXME not implemented")
}

