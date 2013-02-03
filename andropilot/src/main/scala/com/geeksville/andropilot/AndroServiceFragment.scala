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
}
