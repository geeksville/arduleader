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
trait MapAdapter extends HierarchicalAdapter {
  def children: Map[String, SmallAPI] = Map()

  /**
   * If the user wants something in a child, delegate it to the child - otherwise handle it here
   */
  def getChild(memberName: String): Option[(String, SmallAPI)] = {
    val (m, rest) = splitAtDot(memberName)

    val child = children(m)
    Some(rest -> child)
  }
}