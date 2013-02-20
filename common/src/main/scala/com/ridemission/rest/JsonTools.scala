package com.ridemission.rest

import JsonTools._

trait JValue {
  def asJSON: String

  override def toString = asJSON
}

case class JObject(s: (String, Any)*) extends JValue {
  def asJSON = s.map(pairToJSON).mkString("{", ",", "}")
}

case class JArray(s: Any*) extends JValue {
  def asJSON = s.map(valueToJSON).mkString("[", ",", "]")
}

/**
 * It may make sense to use one of the (zillion) Java to JSON libraries.  However, I want something small for Android so let's just see if we can make do...
 */
object JsonTools {

  //// FIXME - need to properly backquote quotes inside str
  def quoted(str: String) = "\"" + str + "\""

  def valueToJSON(s: Any): String = s match {
    case None => "null"
    case Some(x) => valueToJSON(x)

    case x: JValue => x.asJSON

    case x: String => quoted(x)
    case x: Long => x.toString
    case x: Double => x.toString
    case x: Float => x.toString
    case x: Int => x.toString
    case x: Boolean => x.toString

    case x: Seq[_] => JArray(x: _*).toString // Let's give this a shot if we can

    case x @ _ => quoted(x.toString)
  }

  def pairToJSON(s: (String, Any)) = quoted(s._1) + ":" + valueToJSON(s._2)

  /// For testing
  def main(args: Array[String]) {
    val m = JObject("cat" -> "meow", "int" -> 5, "truth" -> true, "ape" -> 2.4, "dog" -> Some("dog"), "none" -> None)
    println(m)

    val a = JArray(1, 2, 3, "cat", "dog", true, None)
    println(a)
  }
}