package com.geeksville.rest

import android.content.res._

import java.io._

import com.ridemission.rest._

class AndroidFilesystem(val assets: AssetManager, val baseDir: String = "") extends HttpFileSystem {

  def exists(name: String) = true

  private def canOpen(name: String) =
    try {
      val fd = assets.openFd(name)
      fd.close()
      true
    } catch {
      case ex: Exception =>
        false
    }

  def isDirectory(name: String): Boolean = {
    assets.list(name).length != 0
  }

  def open(nameIn: String) = {
    val name = if (nameIn.startsWith("/"))
      nameIn.substring(1)
    else
      nameIn

    try {
      Some(new BufferedInputStream(assets.open(baseDir + name)))
    } catch {
      case ex: Exception =>
        printf("Can't find %s due to %s\n", name, ex)
        None
    }
  }
}