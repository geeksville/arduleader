package com.geeksville.gcsapi

import com.ridemission.rest.JArray
import com.ridemission.rest.JValue

/**
 * Provides a high level mechanism to expose any object as SmallAPI instance.
 *
 * This class does not use reflection (for security and performance), you must register various handlers.  Someday someone could make a reflection
 * or annotation based mechanism if this becomes too painful
 */
abstract class SmallAdapter extends SmallAPI {
  def members: JArray = throw new Exception("FIXME not implemented")

  def get(memberName: String): JValue = getters(memberName)()
  def set(memberName: String, v: JValue) = throw new Exception("FIXME not implemented")

  def call(methodName: String, arguments: JArray) = throw new Exception("FIXME not implemented")

  def getters: Map[String, () => JValue]
}