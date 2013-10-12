package com.geeksville.flight

import scala.xml._
import scala.collection.immutable.SortedMap
import java.io.File
import java.net.URL
import com.geeksville.util.Using._
import com.geeksville.util.AnalyticsService
import com.geeksville.util.FileTools
import java.io.BufferedInputStream
import java.io.FileInputStream
import com.geeksville.util.OSTools

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
  // Force a read of param docs at the time we make this object
  val xml = ParameterDocFile.xml

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
  var cacheDir: Option[File] = {
    // Not really ideal - but good enough for now
    if (OSTools.isAndroid)
      None
    else
      Some(new File(System.getProperty("user.home")))
  }

  private val pdefFilename = "apm.pdef.xml"

  /**
   * Pull the latest copy of apm.pdef.xml from the server
   */
  private def updateCache(cacheFile: File) {
    pdefFilename synchronized {
      try {
        val url = "http://autotest.diydrones.com/Parameters/apm.pdef.xml"
        println(s"Looking for pdef at $url")
        Option((new URL(url)).openStream).foreach {
          using(_) { src =>
            FileTools.atomicOutputFile(cacheFile) { dest =>
              FileTools.copy(src, dest)
            }
            println("Downloaded new pdef from diydrones")
          }
        }
      } catch {
        case ex: Exception =>
          AnalyticsService.reportException("pdef_failed", ex)
      }
    }
  }

  private def openBuiltinPdefStream() = getClass.getResourceAsStream("apm.pdef.xml")

  private def needsUpdate = cacheDir match {
    case None => false // Can't update
    case Some(d) =>
      val cacheFile = new File(d, pdefFilename)
      // If the cached file is older than one day, we try to fetch a new one
      val tooOld = System.currentTimeMillis - 24 * 60 * 60 * 1000L
      val needsUpdate = !cacheFile.exists || cacheFile.lastModified < tooOld
      needsUpdate
  }

  /**
   * Check if we need to update param docs, if so update them now (useful for android where we can't just network whenever we want)
   */
  def updateParamDocs() {
    cacheDir.foreach { d =>
      val cacheFile = new File(d, "apm.pdef.xml")
      if (needsUpdate)
        updateCache(cacheFile)
      else
        println("No PDEF cache update needed")
    }
  }

  private def openPdefStream() = cacheDir match {
    case Some(d) =>
      val cacheFile = new File(d, "apm.pdef.xml")
      // Android doesn't allow doing networking in the 'main' thread - where we might currently be
      if (needsUpdate && !OSTools.isAndroid)
        updateCache(cacheFile)

      if (cacheFile.exists)
        new BufferedInputStream(new FileInputStream(cacheFile))
      else
        openBuiltinPdefStream()
    case None =>
      openBuiltinPdefStream() // No matter what - we must return a valid stream
  }

  private var _xml: Option[Elem] = None

  /// Return our XML pdef representation - contacting servers/updating caches as needed
  def xml = synchronized {
    _xml match {
      case Some(x) if !needsUpdate =>
        println("Reusing cached pdef xml")
        x
      case _ =>
        try {
          println("Fetching new XML")
          using(openPdefStream()) { stream =>
            _xml = Some(XML.load(stream))
            _xml.get
          }
        } catch {
          case ex: Exception =>
            // No matter what - we must return a valid stream - and apparently something about the cached file is corruped
            AnalyticsService.reportException("pdef_badparse", ex)
            XML.load(openBuiltinPdefStream())
        }
    }
  }

  def main(args: Array[String]) {
    val p = new ParameterDocFile
    p.forVehicle("ArduPlane").foreach(println)
  }
}
