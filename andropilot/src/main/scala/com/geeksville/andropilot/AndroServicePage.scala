package com.geeksville.andropilot

import android.os.Bundle
import android.widget.ArrayAdapter
import com.ridemission.scandroid.AndroidLogger
import android.widget.ListView
import android.view.View
import com.geeksville.flight.VehicleMonitor
import android.app.FragmentManager
import scala.collection.JavaConverters._
import android.widget.SimpleAdapter
import android.os.Handler
import com.geeksville.flight.MsgParametersDownloaded
import com.geeksville.util.ThreadTools._
import android.support.v4.app.Fragment
import com.ridemission.scandroid.PagerPage

/**
 * Mixin for common behavior for all our fragments that depend on data from the andropilot service.
 * This variant is careful to only start using the service when our page is shown (being careful to only start it once
 * and to only stop it when once
 */
trait AndroServicePage extends Fragment with AndroidLogger with AndroServiceClient with PagerPage {

  implicit def context = getActivity

  /**
   * Does work in the GUIs thread
   */
  protected final var handler: Handler = null

  private var serviceBound = false

  override def onCreate(saved: Bundle) {
    super.onCreate(saved)

    debug("androPage onCreate")
    handler = new Handler
  }

  override def onResume() {
    debug("androPage onResume")
    super.onResume()

    if (isShown) // Only do this we we were already the selected page
      bind()
  }

  override def onPause() {
    debug("androPage onPause")

    unbind()

    super.onPause()
  }

  private def unbind() {
    if (serviceBound) {
      serviceOnPause()
      serviceBound = false
    }
  }

  private def bind() {
    serviceOnResume()
    serviceBound = true
  }

  override def onPageShown() {
    super.onPageShown()
    bind()
  }

  override def onPageHidden() {
    unbind()

    super.onPageHidden()
  }
}
