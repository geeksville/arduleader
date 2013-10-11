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
import Using._
import com.geeksville.logback.Logging

/**
 * Anything that can serve up 'files' to a file handler
 * For android we might want to use a different implementation.
 */
trait HttpFileSystem {
  def exists(name: String): Boolean
  def isDirectory(name: String): Boolean
  def open(name: String): Option[InputStream]
}

/**
 * Just reads files from a file system
 */
class JavaFileSystem(val rootDir: File) extends HttpFileSystem with Logging {
  println("Exposing java filesystem at " + rootDir.getAbsolutePath)

  private def makeFile(name: String) = new File(rootDir, name)

  def exists(name: String) = {
    val f = makeFile(name)

    logger.debug("Checking exists: " + f.getAbsolutePath)

    f.exists
  }

  def isDirectory(name: String) = makeFile(name).isDirectory
  def open(name: String) = {
    val f = makeFile(name)
    if (f.exists)
      Some(new BufferedInputStream(new FileInputStream(f), 8192))
    else
      None
  }
}

/**
 * Serves up files rooted at a particular URL
 */
class FileHandler(val urlPath: String, fs: HttpFileSystem)
  extends GETHandler((urlPath + "(.*)").r) {

  def this(urlPath: String, rootDir: File) =
    this(urlPath, new JavaFileSystem(rootDir))

  /// @return true if this handler will match against the provided path
  override def canHandle(method: Method.Value, matches: List[String]) = super.canHandle(method, matches) && fs.exists(matches(0))

  override protected def handleRequest(req: Request) = {
    var relpath = req.matches(0)

    if (fs.isDirectory(relpath)) // Implicitly look for index files
      relpath = relpath + "/index.html"

    val fopt = fs.open(relpath)
    if (!fopt.isDefined) {
      printf("Not found: %s\n", relpath)
      new ErrorResponse(404, "NOT FOUND", "File not found '%s'".format(relpath))
    } else {
      val ext = FileTools.getExtension(relpath)
      val mimeType = ext.flatMap(HttpConstants.extensionToMime.get _).
        getOrElse(HttpConstants.contentTypeBinary)

      printf("Serving: %s as %s\n", relpath, mimeType)

      StreamResponse(mimeType) { out =>
        using(fopt.get) { src =>
          FileTools.copy(src, out)
        }
      }
    }
  }
}

