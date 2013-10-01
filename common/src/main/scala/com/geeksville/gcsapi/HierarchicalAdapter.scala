package com.geeksville.gcsapi

import com.ridemission.rest.JArray
import com.ridemission.rest.JValue
import com.ridemission.rest.JsonConverters._
import com.ridemission.rest.JObject

/**
 * Exposes child SmallAPI instances, with dots to separate the names of children
 *
 * No GCS specific code here - someday I'd like to reuse this somewhere else
 */
trait HierarchicalAdapter extends SmallAPI {
  def children: Map[String, SmallAPI]

  /**
   * If the user wants something in a child, delegate it to the child - otherwise handle it here
   */
  def getChild(memberName: String): Option[(String, SmallAPI)] = {
    val dotLoc = memberName.indexOf('.')
    if (dotLoc < 0)
      None
    else if (dotLoc == 0)
      throw new Exception(s"Invalid member name: $memberName")
    else {
      val m = memberName.substring(0, dotLoc)
      val rest = memberName.substring(dotLoc + 1, memberName.length())

      val child = children(m)
      Some(rest -> child)
    }
  }

  abstract override def get(memberName: String): JValue = {
    val p = getChild(memberName)
    p match {
      case Some((rest, child)) => child.get(rest)
      case None => super.get(memberName)
    }
  }
}