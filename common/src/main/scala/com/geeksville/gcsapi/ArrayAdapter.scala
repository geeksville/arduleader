package com.geeksville.gcsapi

import com.ridemission.rest.JArray
import com.ridemission.rest.JValue
import com.ridemission.rest.JsonConverters._
import com.ridemission.rest.JObject

/**
 * Exposes N children indexed by int.
 * i.e. we match member names that look like "24"
 *
 * No GCS specific code here - someday I'd like to reuse this somewhere else
 */
class ArrayAdapter(val children: Seq[SmallAPI]) extends HierarchicalAdapter {

  /**
   * If the user wants something in a child, delegate it to the child - otherwise handle it here
   */
  def getChild(memberName: String): Option[(String, SmallAPI)] = {

    val (m, rest) = splitAtDot(memberName)
    val index = m.toInt
    val child = children(index)
    Some(rest -> child)
  }
}