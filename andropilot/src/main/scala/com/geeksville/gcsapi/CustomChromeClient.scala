package com.geeksville.gcsapi

import android.app._
import android.webkit._
import android.os.Bundle
import android.widget._
import android.view._
import android.content._
import android.os._
import android.text.util._
import com.ridemission.scandroid.AndroidLogger

/// Bridges javascript debug messages into the Android log
class CustomChromeClient extends WebChromeClient with AndroidLogger {
  override def onJsAlert(view: WebView, url: String, message: String, result: JsResult) = {
    warn(message) // FIXME - toast instead?
    result.confirm()
    true
  }

  override def onConsoleMessage(m: ConsoleMessage) = {
    val formatted = "%s %s (%s:%d)".format(m.messageLevel, m.message, m.sourceId, m.lineNumber)
    m.messageLevel match {
      case ConsoleMessage.MessageLevel.ERROR => error(formatted)
      case ConsoleMessage.MessageLevel.LOG => info(formatted)
      case ConsoleMessage.MessageLevel.WARNING => warn(formatted)
      case _ => debug(formatted)
    }

    true
  }
}
