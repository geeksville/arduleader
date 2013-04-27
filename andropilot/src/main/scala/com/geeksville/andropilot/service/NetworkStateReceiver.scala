package com.geeksville.andropilot.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.content.IntentFilter
import com.ridemission.scandroid.AndroidLogger
import com.bugsense.trace.BugSenseHandler

/**
 * When we see a network connection arrive, try to restart the upload
 */
object NetworkStateReceiver extends BroadcastReceiver with AndroidLogger {

  private var registered = false

  def onReceive(context: Context, intent: Intent) {
    val conn = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val networkInfo = conn.getActiveNetworkInfo

    if (networkInfo != null) {
      debug("Received network info: " + networkInfo)

      context.startService(AndroidDirUpload.createIntent(context))
    }
  }

  def register(context: Context) {
    if (!registered) {
      val filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

      registered = true
      context.registerReceiver(this, filter)
    }
  }

  def unregister(context: Context) {
    if (registered) {
      try {
        context.unregisterReceiver(this)
      } catch {
        case ex: Exception =>
          // Should not happen... let's see
          BugSenseHandler.sendExceptionMessage("unregister", "exception", ex)
      }
      registered = false
    }
  }
}