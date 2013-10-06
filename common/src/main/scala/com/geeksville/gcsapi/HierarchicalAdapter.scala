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
trait HierarchicalAdapter extends SmallAdapter {

  /**
   * If the user wants something in a child, delegate it to the child - otherwise handle it here
   */
  protected def getChild(memberName: String): Option[(String, SmallAPI)]

  /**
   * @return leftofdot, rightofdot (or empty string if no dot)
   */
  protected final def splitAtDot(memberName: String) = {
    val dotLoc = memberName.indexOf('.')
    if (dotLoc < 0) {
      // No dot, try to find the child with this name and use "" for the rest
      memberName -> ""
    } else if (dotLoc == 0)
      throw new Exception(s"Invalid member name: $memberName")
    else {
      val m = memberName.substring(0, dotLoc)
      val rest = memberName.substring(dotLoc + 1, memberName.length())

      m -> rest
    }
  }

  private def childInvoker[T](memberName: String)(ifFound: (SmallAPI, String) => T)(ifNotFound: (String) => T): T = {
    val p = getChild(memberName)
    p match {
      case Some((rest, child)) => ifFound(child, rest)
      case None => ifNotFound(memberName)
    }
  }

  abstract override def call(objName: String, methodName: String, arguments: Seq[Any]) = childInvoker(objName) {
    case (child, rest) =>
      child.call(rest, methodName, arguments)
  } { n =>
    super.call("_this", methodName, arguments)
  }
}