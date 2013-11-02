package com.geeksville.rest

import android.content.res._
import java.io._
import com.ridemission.rest._
import com.ridemission.scandroid.AndroidLogger

class AndroidFilesystem(val assets: AssetManager, val baseDir: String = "") extends HttpFileSystem with AndroidLogger {

  override def toString = s"AndroidFS($baseDir)"

  def exists(name: String) = {
    debug(s"$this: Claiming exists for $name")
    true
  }

  private def canOpen(name: String) =
    try {
      val fd = assets.openFd(baseDir + name)
      fd.close()
      debug(s"$this: Claiming canOpen for $name")
      true
    } catch {
      case ex: Exception =>
        error(s"$this: Claiming !canOpen for $name")
        false
    }

  def isDirectory(name: String): Boolean = {
    val r = assets.list(baseDir + name).length != 0
    debug(s"$this: $name isDir=$r")
    r
  }

  def open(nameIn: String) = {
    debug(s"$this: opening $nameIn")
    val name = if (nameIn.startsWith("/"))
      nameIn.substring(1)
    else
      nameIn

    try {
      val s = assets.open(baseDir + name)
      if (s == null) {
        error(s"$this: can't open $name")
        None
      } else
        Some(new BufferedInputStream(s))
    } catch {
      case ex: Exception =>
        error(s"$this: Can't find $name in '$baseDir' due to $ex")
        None
    }
  }
}