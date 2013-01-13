package com.geeksville.util

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.WeakHashMap
import scala.collection.JavaConverters._

case class DebugContext(info: Seq[Any]) {
  override def toString = info.mkString(", ")
}

/**
 * The information we keep on a per thread basis.  Younger contexts are at the head.
 * This does not need to be synchronized because only one thread will write at a time
 */
class ThreadContext {
  val contexts = ListBuffer[DebugContext]()

  def add(c: DebugContext) = contexts.prepend(c)
  def remove() = contexts.remove(0)

  /**
   * A detailed string representation
   */
  def toLongString = toString + contexts.zipWithIndex.map {
    case (c, i) =>
      "  %d: %s".format(i, c)
  }.mkString(":\n", "\n  ", "\n")

  override def toString = "%d debug contexts".format(contexts.size)
}

/**
 * A really useful debugging tool: Lets you keep debugging information around for any active thread.
 * This class is designed to solve the problem of getting a crash report with a bunch of stack traces, but without
 * useful information about context/arguments.  If you have important debugging context, just wrap your code with
 * debugContext (args, params) { yourcode }
 */
object DebugContext {

  private val contexts = new WeakHashMap[Thread, ThreadContext] with SynchronizedMap[Thread, ThreadContext]

  /**
   * The main API for registering debug contexts.
   *
   * Wrap your code like so:
   * debugContext("doing some stuff", this, "username" -> "kevin") { someCode }
   */
  def debugContext[T](info: Any*)(f: => T) = {
    val t = Thread.currentThread()
    val context = DebugContext(info)

    // Add our context
    val list = contexts.getOrElseUpdate(t, new ThreadContext)
    list.add(context)

    val r = try {
      f
    } finally {
      // Remove our context
      val popped = list.remove()
      assert(popped == context)
    }
    r
  }

  def getThreadContext(t: Thread) = contexts.getOrElse(t, new ThreadContext)

  /**
   * Return a nicely formatted report on a particular thread.  Including stack trace and extra debug information...
   */
  def getDebugReport(t: Thread) = {
    val name = "Thread: " + t.getName + "\n"

    val stack = if (!t.isAlive)
      "Stack: thread is dead"
    else
      t.getStackTrace.mkString("Stack:\n", "\n  ", "\n")

    val debugs = getThreadContext(t).toLongString

    name + stack + debugs
  }

  /**
   * A complete dump - suitable for including in crash reports.
   */
  def getAllDebugReports =
    Thread.getAllStackTraces.asScala.map {
      case (thread, stack) =>
        getDebugReport(thread)
    }.mkString("\n")
}
