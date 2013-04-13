package com.geeksville.andropilot.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.content.IntentFilter
import com.ridemission.scandroid.AndroidLogger

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

      context.registerReceiver(this, filter)
      registered = true
    }
  }

  def unregister(context: Context) {
    if (registered) {
      context.unregisterReceiver(this)
      registered = false
    }
  }
}