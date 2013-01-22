package com.geeksville.andropilot

import org.acra._
import org.acra.annotation._
import android.app.Application

@ReportsCrashes(formKey = "dDFiVzhuNkNjVWJjUDFMVzJWbXBxZkE6MQ") /*
  mode = ReportingInteractionMode.TOAST,
  forceCloseDialogAfterToast = false, // optional, default false
  resToastText = R.string.crash_toast_text)
  * 
  */
class MyApplication extends Application {
  override def onCreate() {
    // The following line triggers the initialization of ACRA
    ACRA.init(this)
    super.onCreate()
  }
}