package com.geeksville.andropilot.gui

import android.os.Bundle
import android.view.View
import scala.collection.JavaConverters._
import android.widget.SimpleAdapter
import com.geeksville.util.ThreadTools._
import android.support.v4.app.ListFragment
import com.geeksville.andropilot.R
import com.geeksville.andropilot.service._

/**
 * A fragment that has a simple list of vehicle data
 */
abstract class SimpleListFragment extends ListFragment with AndroServicePage {

  /**
   * Make our list read-only
   */
  override def onViewCreated(view: View, savedInstanceState: Bundle) {
    super.onViewCreated(view, savedInstanceState)
    view.setFocusable(false)
  }

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    // Don't expand the view until we have _something_ to display
    if (isVisible) {
      debug("Setting rcchannel")
      updateList()
    }
  }

  protected def updateList() {
    makeAdapter.foreach(setListAdapter)
  }

  /**
   * FIXME - this is super inefficient
   */
  protected def seqToAdapter(seq: Seq[(String, Any)]) = {
    val asMap = seq.map {
      case (n, v) =>
        Map("n" -> n, "v" -> v.toString).asJava
    }.asJava

    val fromKeys = Array("n", "v")
    val toFields = Array(R.id.rcch_name, R.id.rcch_value)
    new SimpleAdapter(getActivity, asMap, R.layout.rcchannel_row, fromKeys, toFields)
  }

  protected def makeAdapter(): Option[SimpleAdapter]
}
