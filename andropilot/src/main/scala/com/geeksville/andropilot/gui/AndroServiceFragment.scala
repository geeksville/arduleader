package com.geeksville.andropilot.gui

import android.os.Bundle
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import android.os.Handler
import com.geeksville.util.ThreadTools._
import android.support.v4.app.Fragment
import com.geeksville.andropilot.service.AndroServiceClient

/**
 * Mixin for common behavior for all our fragments that depend on data from the andropilot service.
 */
trait AndroServiceFragment extends Fragment with AndroidLogger with AndroServiceClient {

  implicit def context = getActivity

  /**
   * Does work in the GUIs thread
   */
  protected final var handler: Handler = null

  override def onCreate(saved: Bundle) {
    super.onCreate(saved)

    debug("androFragment onCreate")
    handler = new Handler
  }

  override def onResume() {
    debug("androFragment onResume")
    super.onResume()

    serviceOnResume()
  }

  override def onPause() {
    debug("androFragment onPause")
    serviceOnPause()

    super.onPause()
  }

  // protected def isVisible = (getActivity != null) && (getView != null)
}
