package com.geeksville.shell

import java.io.PrintWriter

class ScalaShell {

  val name = "shell"

  val initCmds = Seq[String]()
  val bindings = Seq[(String, String, String)]()

  val in = System.in
  val out = System.out

  def run() {

    val pw = new PrintWriter(out)
    val il = new scala.tools.nsc.interpreter.ILoop(None, pw)
    il.setPrompt(name + "> ")
    il.settings = new scala.tools.nsc.Settings()
    il.settings.embeddedDefaults(getClass.getClassLoader)
    il.settings.usejavacp.value = true
    il.createInterpreter()
    il.in = new JLineIOReader(
      in,
      out,
      new scala.tools.nsc.interpreter.JLineCompletion(il.intp))

    if (il.intp.reporter.hasErrors) {
      println("Got errors, abandoning connection")
      return
    }

    il.printWelcome()
    try {
      il.intp.initialize()
      il.intp.beQuietDuring {
        il.intp.bind("stdout", pw)
        for ((bname, btype, bval) <- bindings)
          il.bind(bname, btype, bval)
      }
      il.intp.quietRun(
        """def println(a: Any) = {
                  stdout.write(a.toString)
                stdout.write('\n')
                }""")
      il.intp.quietRun(
        """def exit = println("Use ctrl-D to exit shell.")""")

      initCmds.foreach(il.intp.quietRun)

      il.loop()
    } finally
      il.closeInterpreter()
  }

}