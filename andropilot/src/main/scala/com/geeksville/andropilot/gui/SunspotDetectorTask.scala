package com.geeksville.andropilot.gui

import com.ridemission.scandroid.AsyncVoidTask
import com.ridemission.scandroid.AndroidLogger
import com.geeksville.sunspot.SunspotClient
import android.content.Context
import android.app.Activity
import com.ridemission.scandroid.UsesResources
import android.support.v4.app.FragmentActivity
import com.ridemission.scandroid.SimpleOkayDialog

class SunspotDetectorTask(val context: FragmentActivity) extends AsyncVoidTask with AndroidLogger with UsesResources {

  private var curLevel: Option[Int] = None

  override protected def inBackground() {
    warn("Asking for sunspot levels")
    curLevel = SunspotClient.getCurrentLevel()
  }

  private def showDialog(icon: Int, msg: String) {
    SimpleOkayDialog.show(context, msg, android.R.drawable.ic_dialog_alert)
  }

  protected def didSunspot(curLevel: Option[Int]) {
  }

  protected override def onPostExecute(unused: Void) {
    didSunspot(curLevel)

    curLevel match {
      case None =>
        toast("Solar radiation data not found (via internet) - no warnings available")
      case Some(lvl) =>
        if (lvl >= SunspotClient.criticalThreshold)
          showDialog(0, "Solar activity: Severe. Check for low GPS accuracy.")
        else if (lvl >= SunspotClient.warnThreshold)
          showDialog(0, "Solar activity: Extreme. Check for low GPS accuracy.")
        else
          toast("Solar radiation levels acceptable")
    }
  }
}