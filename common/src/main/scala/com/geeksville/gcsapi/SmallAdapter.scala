package com.geeksville.gcsapi

import com.ridemission.rest.JArray
import com.ridemission.rest.JValue
import com.ridemission.rest.JsonConverters._
import com.ridemission.rest.JObject

/**
 * Provides a high level mechanism to expose any object as SmallAPI instance.
 *
 * This class does not use reflection (for security and performance), you must register various handlers.  Someday someone could make a reflection
 * or annotation based mechanism if this becomes too painful.
 *
 * No GCS specific code here - someday I'd like to reuse this somewhere else
 */
abstract class SmallAdapter extends SmallAPI {

  override def members: JObject = {
    // val names = (getters.keys.toSeq ++ setters.keys.toSeq).distinct

    val elems = Seq("getters" -> getters.keys.toSeq.asJson,
      "setters" -> setters.keys.toSeq.asJson)

    // names.asJson
    elems.asJson
  }

  override def get(memberName: String): JValue = getters(memberName)()
  override def set(memberName: String, v: JValue) { setters(memberName)(v) }

  override def call(methodName: String, arguments: JArray) = throw new Exception("FIXME not implemented")

  type Getter = () => JValue
  type Setter = (JValue) => Unit

  def getters: Map[String, Getter] = Map()
  def setters: Map[String, Setter] = Map()

}