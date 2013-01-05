/*
 * Copyright 2010-2011 Weigle Wilczek GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geeksville.logback

import org.slf4j.{ Logger => Slf4jLogger, LoggerFactory }
import org.slf4j.spi.{ LocationAwareLogger => Slf4jLocationAwareLogger }

/**
 * Factory for Loggers.
 */
object Logger {

  /**
   * Creates a Logger named corresponding to the given class.
   * @param clazz Class used for the Logger's name. Must not be null!
   */
  def apply(clazz: Class[_]): Logger = {
    require(clazz != null, "clazz must not be null!")
    logger(LoggerFactory getLogger clazz)
  }

  /**
   * Creates a Logger with the given name.
   * @param name The Logger's name. Must not be null!
   */
  def apply(name: String): Logger = {
    require(name != null, "loggerName must not be null!")
    logger(LoggerFactory getLogger name)
  }

  private def logger(slf4jLogger: Slf4jLogger): Logger = slf4jLogger match {
    case locationAwareLogger: Slf4jLocationAwareLogger =>
      new DefaultLocationAwareLogger(locationAwareLogger)
    case _ =>
      new DefaultLogger(slf4jLogger)
  }
}

/**
 * Thin wrapper for SLF4J making use of by-name parameters to improve performance.
 */
trait Logger {

  /**
   * The name of this Logger.
   */
  lazy val name = slf4jLogger.getName

  /**
   * The wrapped SLF4J Logger.
   */
  protected val slf4jLogger: Slf4jLogger

  /**
   * Log a message with ERROR level.
   * @param msg The message to be logged
   */
  def error(msg: => String) {
    if (slf4jLogger.isErrorEnabled) slf4jLogger.error(msg)
  }

  /**
   * Log a message with ERROR level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def error(msg: => String, t: Throwable) {
    if (slf4jLogger.isErrorEnabled) slf4jLogger.error(msg, t)
  }

  /**
   * Log a message with WARN level.
   * @param msg The message to be logged
   */
  def warn(msg: => String) {
    if (slf4jLogger.isWarnEnabled) slf4jLogger.warn(msg)
  }

  /**
   * Log a message with WARN level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def warn(msg: => String, t: Throwable) {
    if (slf4jLogger.isWarnEnabled) slf4jLogger.warn(msg, t)
  }

  /**
   * Log a message with INFO level.
   * @param msg The message to be logged
   */
  def info(msg: => String) {
    if (slf4jLogger.isInfoEnabled) slf4jLogger.info(msg)
  }

  /**
   * Log a message with INFO level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def info(msg: => String, t: Throwable) {
    if (slf4jLogger.isInfoEnabled) slf4jLogger.info(msg, t)
  }

  /**
   * Log a message with DEBUG level.
   * @param msg The message to be logged
   */
  def debug(msg: => String) {
    if (slf4jLogger.isDebugEnabled) slf4jLogger.debug(msg)
  }

  /**
   * Log a message with DEBUG level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def debug(msg: => String, t: Throwable) {
    if (slf4jLogger.isDebugEnabled) slf4jLogger.debug(msg, t)
  }

  /**
   * Log a message with TRACE level.
   * @param msg The message to be logged
   */
  def trace(msg: => String) {
    if (slf4jLogger.isTraceEnabled) slf4jLogger.trace(msg)
  }

  /**
   * Log a message with TRACE level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def trace(msg: => String, t: Throwable) {
    if (slf4jLogger.isTraceEnabled) slf4jLogger.trace(msg, t)
  }
}

private final class DefaultLogger(override protected val slf4jLogger: Slf4jLogger) extends Logger

/**
 * Thin wrapper for a location aware SLF4J logger making use of by-name parameters to improve performance.
 *
 * This implementation delegates to a location aware logger. For those SLF4J adapters that implement this
 * interface, such as log4j and java.util.logging adapters, the code location reported will be that
 * of the caller instead of the wrapper.
 *
 * Hint: Use the Logger object to choose the correct implementation automatically.
 */
trait LocationAwareLogger extends Logger {
  import Slf4jLocationAwareLogger.{ ERROR_INT, WARN_INT, INFO_INT, DEBUG_INT, TRACE_INT }

  override protected val slf4jLogger: Slf4jLocationAwareLogger

  /**
   * Get the wrapper class name for detection of the stackframe of the user code calling into the log framework.
   * @return The fully qualified class name of the outermost logger wrapper class.
   */
  protected val wrapperClassName: String

  override def error(msg: => String) {
    if (slf4jLogger.isErrorEnabled) log(ERROR_INT, msg)
  }

  override def error(msg: => String, t: Throwable) {
    if (slf4jLogger.isErrorEnabled) log(ERROR_INT, msg, t)
  }

  override def warn(msg: => String) {
    if (slf4jLogger.isWarnEnabled) log(WARN_INT, msg)
  }

  override def warn(msg: => String, t: Throwable) {
    if (slf4jLogger.isWarnEnabled) log(WARN_INT, msg, t)
  }

  override def info(msg: => String) {
    if (slf4jLogger.isInfoEnabled) log(INFO_INT, msg)
  }

  override def info(msg: => String, t: Throwable) {
    if (slf4jLogger.isInfoEnabled) log(INFO_INT, msg, t)
  }

  override def debug(msg: => String) {
    if (slf4jLogger.isDebugEnabled) log(DEBUG_INT, msg)
  }

  override def debug(msg: => String, t: Throwable) {
    if (slf4jLogger.isDebugEnabled) log(DEBUG_INT, msg, t)
  }

  override def trace(msg: => String) {
    if (slf4jLogger.isTraceEnabled) log(TRACE_INT, msg)
  }

  override def trace(msg: => String, t: Throwable) {
    if (slf4jLogger.isTraceEnabled) log(TRACE_INT, msg, t)
  }

  private final def log(level: Int, msg: String, throwable: Throwable = null) {
    slf4jLogger.log(null, wrapperClassName, level, msg, null, throwable)
  }
}

private object DefaultLocationAwareLogger {
  private val WrapperClassName = classOf[DefaultLocationAwareLogger].getName
}

private final class DefaultLocationAwareLogger(override protected val slf4jLogger: Slf4jLocationAwareLogger)
  extends LocationAwareLogger {
  override protected val wrapperClassName = DefaultLocationAwareLogger.WrapperClassName
}