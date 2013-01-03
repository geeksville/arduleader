package com.geeksville.shell

/**
 * A shell bound to the default console
 */
class ScalaConsole extends ScalaShell(in = new RawConsoleInputStream) {

}