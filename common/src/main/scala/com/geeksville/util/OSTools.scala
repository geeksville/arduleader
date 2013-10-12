package com.geeksville.util

/// Utilities for doing OS specific operations
object OSTools {

  /// Check for Android but without using any android libs
  def isAndroid = System.getProperty("java.vm.name") == "Dalvik"

  def osName = System.getProperty("os.name").toLowerCase
  def isWindows = osName.contains("win")
  def isLinux = osName.contains("linux")
  def isMac = osName.contains("mac")

  def runtime = Runtime.getRuntime

  /// Create and start a child process
  def exec(cmdArgs: String*) = runtime.exec(cmdArgs.toArray)
}