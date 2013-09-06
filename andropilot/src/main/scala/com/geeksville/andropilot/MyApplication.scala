package com.geeksville.andropilot

import android.app.Application
import com.flurry.android.FlurryAgent
import com.bugsense.trace.BugSenseHandler

// @ReportsCrashes(formKey = "dDFiVzhuNkNjVWJjUDFMVzJWbXBxZkE6MQ") /*
/* mode = ReportingInteractionMode.TOAST,
  forceCloseDialogAfterToast = false, // optional, default false
  resToastText = R.string.crash_toast_text)
  * 
  */
class MyApplication extends Application with AndropilotPrefs {
  override def context = this

  override def onCreate() {
    // The following line triggers the initialization of ACRA
    // ACRA.init(this)
    BugSenseHandler.initAndStartSession(this, "2a5e5e70")
    val username = dshareUsername
    if (!username.isEmpty)
      BugSenseHandler.setUserIdentifier(dshareUsername)

    // BugSenseHandler.setLogging(true)

    FlurryAgent.setCaptureUncaughtExceptions(false) // So we get the reports through google instead
    super.onCreate()
  }
}