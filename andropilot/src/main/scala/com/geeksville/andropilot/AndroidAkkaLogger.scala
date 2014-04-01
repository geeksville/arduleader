package com.geeksville.andropilot

import akka.actor.Actor
import akka.event.Logging._
import android.util.Log

class AndroidAkkaLogger extends Actor {
  def receive = {
    case Error(cause, logSource, logClass, message) ⇒
      Log.e("com.geeksville.akka", s"$message [$logSource]: $cause")

    case Warning(logSource, logClass, message) ⇒
      Log.w("com.geeksville.akka", s"$message [$logSource]")

    case Info(logSource, logClass, message) ⇒
      Log.i("com.geeksville.akka", s"$message [$logSource]")

    case Debug(logSource, logClass, message) ⇒
      Log.d("com.geeksville.akka", s"$message [$logSource]")

    case InitializeLogger(_) ⇒
      Log.d("com.geeksville.akka", "Logging started")
      sender ! LoggerInitialized
  }
}