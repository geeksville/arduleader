package com.geeksville.util

import java.io.File
import java.io.IOException
import java.lang.reflect.Field

object SystemTools {
  /**
   * Skanky way to update java.library.path
   * http://stackoverflow.com/questions/5419039
   * /is-djava-library-path-equivalent-to-system-setpropertyjava-library-path
   *
   * @param s
   * @throws IOException
   */
  def addDir(s: String) {
    // This enables the java.library.path to be modified at runtime
    // From a Sun engineer at
    // http://forums.sun.com/thread.jspa?threadID=707176
    //
    val field = classOf[ClassLoader].getDeclaredField("usr_paths")
    field.setAccessible(true)
    val paths = field.get(null).asInstanceOf[Array[String]]
    if (paths.contains(s))
      return

    val tmp = new Array[String](paths.length + 1)
    System.arraycopy(paths, 0, tmp, 0, paths.length)
    tmp(paths.length) = s
    field.set(null, tmp)
    System.setProperty("java.library.path", s + File.pathSeparator
      + System.getProperty("java.library.path"))
  }
}
