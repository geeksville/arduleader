package com.geeksville.util

import java.util._
import java.io._
import java.security._

object Gravatar {
  private def hex(array: Array[Byte]) = array.map { b => "%02x".format(b) }.mkString

  private def md5Hex(message: String) = {
    val md = MessageDigest.getInstance("MD5")
    val r = hex(md.digest(message.getBytes("CP1252")))
    r
  }

  private def emailHash(email: String) = md5Hex(email.trim.toLowerCase)

  def profileUrl(email: String) = s"http://www.gravatar.com/${emailHash(email)}"

  def avatarImageUrl(email: String) = s"http://www.gravatar.com/avatar/${emailHash(email)}.jpg"
}