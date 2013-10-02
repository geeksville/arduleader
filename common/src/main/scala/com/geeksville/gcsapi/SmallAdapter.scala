package com.geeksville.gcsapi

import com.ridemission.rest.JArray
import com.ridemission.rest.JValue
import com.ridemission.rest.JsonConverters._
import com.ridemission.rest.JObject
import com.ridemission.rest.JNull

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

  override def call(objName: String, methodName: String, arguments: Seq[Any]) = {
    methodName match {
      case GetMethod =>
        val r = getters(objName)()
        println(s"getter for $objName returned $r")
        r
      case SetMethod =>
        setters(objName)(arguments(0))
        JNull
      case _ =>
        println(s"In $objName looking for $methodName, args=$arguments")
        methods(methodName)(arguments)
    }
  }

  type Getter = () => JValue
  type Setter = (Any) => Unit
  type Method = (Seq[Any]) => JValue

  def getters: Map[String, Getter] = Map()
  def setters: Map[String, Setter] = Map()
  def methods: Map[String, Method] = Map()
}