package com.geeksville.shell

import java.io._
import scala.tools.nsc.GenericRunnerSettings
import scala.tools.jline.Terminal
import scala.tools.jline.TerminalFactory

class ScalaShell(val in: InputStream = System.in, val out: OutputStream = System.out) {

  def name = "shell"

  def initCmds = Seq[String]()
  def bindings = Seq[(String, (Class[_], Any))]()

  private val pw = new PrintWriter(out)

  private class MyLoop extends scala.tools.nsc.interpreter.ILoop(None, pw) {
    override def loop() {
      if (isAsync) awaitInitialized()
      bindSettings()
      super.loop()
    }

    /** Bind the settings so that evaluated code can modify them */
    def bindSettings() {
      intp beQuietDuring {
        for ((name, (clazz, value)) <- bindings) {
          intp.bind(name, clazz.getCanonicalName, value)
        }
        initCmds.foreach(intp.interpret)
      }
    }
  }

  def run() {
    val il = new MyLoop
    il.setPrompt(name + "> ")
    // val settings = new scala.tools.nsc.Settings()
    val settings = new GenericRunnerSettings(pw.println)
    //settings.embeddedDefaults(getClass.getClassLoader)
    settings.usejavacp.value = true

    il process settings

    // Jline apparently leaves things messed up
    TerminalFactory.get.setEchoEnabled(true)
  }

}