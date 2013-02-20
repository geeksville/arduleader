/**
 * Copyright 2010 Mission Motors, Inc.
 * Kindly released under the Apache Source License (http://www.apache.org/licenses/LICENSE-2.0.html) on Feb 19th, 2013
 */
package com.ridemission.rest

import scala.actors._
import scala.util.matching._
import scala.io._
import scala.collection.mutable.ListBuffer

import java.io._
import java.net._
import java.util.concurrent._

import com.geeksville.util._
import com.geeksville.util.ThreadTools._
import Using._

object HttpConstants {
  val utf = "; charset=utf-8"

  val contentTypeText = "text/plain" + utf
  val contentTypeHtml = "text/html" + utf
  val contentTypeJson = "text/json" + utf
  val contentTypeBinary = "application/octet-stream"

  val extensionToMime = Map(
    "html" -> contentTypeHtml,
    "txt" -> contentTypeText,
    "png" -> "image/png",
    "js" -> ("application/javascript" + utf),
    "ttf" -> "font/truetype",
    "otf" -> "font/opentype",
    "css" -> ("text/css" + utf),
    "xml" -> ("application/xml" + utf),
    "xpi" -> "application/x-xpinstall")
}

object Method extends Enumeration {
  type Method = Value

  val GET = Value("GET")
  val POST = Value("POST")
  val PUT = Value("PUT")
}