package com.geeksville.flight

import scala.xml._
import scala.collection.immutable.SortedMap

/**
 * Documentation (for human and machine) of a particular parameter name
 */
case class ParamDoc(val humanName: String, val name: String, val documentation: String,
  val valueMap: Option[Map[Int, String]] = None, val fieldMap: Option[Map[String, String]] = None) {
  import ParamDoc._

  /// A name with any namespace prefix removed
  val shortName = name match {
    case NamePattern(namespace, shortname) => shortname
    case x @ _ => x
  }

  /**
   * If a min max range was specified for this parameter, return it
   */
  lazy val range: Option[(Float, Float)] = {
    try {
      fieldMap.flatMap { f =>
        f.get("Range").map { rangestr =>
          rangestr match {
            case RangeValPattern(minr, maxr) =>
              minr.toFloat -> maxr.toFloat
          }
        }
      }
    } catch {
      case ex: Exception =>
        println("Ignoring invalid range: " + ex.getMessage)
        None
    }
  }

  lazy val uiHint: Option[String] =
    fieldMap.flatMap { f =>
      f.get("UIHint")
    }

  /**
   * Type of sharing, if specified (vehicle normally)
   */
  lazy val share: Option[String] =
    fieldMap.flatMap { f =>
      f.get("share")
    }

  /**
   * Try to decode a raw value into the best symbolic representation we can find
   */
  def decodeValue(f: Float) = {
    for {
      vals <- valueMap;
      strname <- vals.get(f.toInt)
    } yield {
      strname
    }
  }
}

object ParamDoc {
  private val NamePattern = "(.*):(.*)".r
  private val RangeValPattern = "\\s*(\\S+) (\\S+)\\s*".r
}

/**
 * Reads parameter documentation for a particular vehicle type
 */
class ParameterDocFile {
  val xml = XML.load(getClass.getResourceAsStream("apm.pdef.xml"))

  private def findParameters(root: NodeSeq, vehicle: String) =
    (root \ "parameters") filter { node => (node \ "@name").text == vehicle }

  private def toParamDoc(n: Node) = {
    val valMap = {
      val children = (n \ "values" \ "value") flatMap { p =>
        try {
          val code = (p \ "@code").text.trim.toInt
          val value = p.child.mkString

          Some(code -> value)
        } catch {
          case ex: NumberFormatException =>
            println("Skipping due to parse error: " + p)
            None
        }
      }
      if (children.isEmpty) None else Some(SortedMap(children: _*))
    }

    val fieldMap = {
      val children = (n \ "field") map { p =>
        val code = (p \ "@name").text
        val value = p.child.mkString

        code -> value
      }
      if (children.isEmpty) None else Some(Map(children: _*))
    }

    ParamDoc((n \ "@humanName").text, (n \ "@name").text, (n \ "@documentation").text, valMap, fieldMap)
  }

  /**
   * Return the set of parameter docs that are appropriate for this vehicle type.
   * i.e. all libraries and one particular vehicle
   *
   * @return a map from param name to ParamDoc
   */
  def forVehicle(vehicle: String) = {
    val vparams = findParameters(xml \ "vehicles", vehicle) \ "param"
    val lparams = xml \ "libraries" \ "parameters" \ "param"

    val alldocs = (vparams ++ lparams) map toParamDoc
    Map(alldocs.map { d => d.shortName -> d }: _*)
  }
}

object ParameterDocFile {
  def main(args: Array[String]) {
    val p = new ParameterDocFile
    p.forVehicle("ArduPlane").foreach(println)
  }
}
