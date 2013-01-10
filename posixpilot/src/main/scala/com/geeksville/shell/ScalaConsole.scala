package com.geeksville.shell

/**
 * A shell bound to the default console
 */
object ScalaConsole {
  def main(args: Array[String]) {
    val shell = new ScalaShell()
    shell.run()
  }
}