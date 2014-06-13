package com.geeksville.apiproxy

object APIConstants {
  /**
   * The default world wide drone broker
   */
  val DEFAULT_SERVER = "api.3dr.com"

  /**
   * If using a raw TCP link to the server, use this port number
   */
  val DEFAULT_TCP_PORT = 5555

  val URL_BASE = s"http://$DEFAULT_SERVER"

  val tlogMimeType = "application/vnd.mavlink.tlog"
  val flogMimeType = "application/vnd.mavlink.flog"
  val blogMimeType = "application/vnd.mavlink.blog"

  val flogExtension = ".log"
  val blogExtension = ".bin"

  def isValidMimeType(mimetype: String) = mimetype == tlogMimeType || mimetype == flogMimeType || mimetype == blogMimeType

  def mimeTypeToExtension(mimetype: String) = {
    if (mimetype == tlogMimeType)
      "" // On the server we have a nasty DB mistake, so we don't use suffixes on tlogs
    else if (mimetype == flogMimeType)
      flogExtension
    else if (mimetype == blogMimeType)
      blogExtension
    else
      throw new Exception("unknown mime type")
  }

  def extensionToMimeType(filename: String) = {
    if (filename.endsWith(".tlog"))
      Some(tlogMimeType)
    else if (filename.endsWith(flogExtension)) // temp hack to hide from users
      Some(flogMimeType)
    else if (filename.endsWith(".bix")) // temp hack 
      Some(blogMimeType)
    else
      None
  }
}