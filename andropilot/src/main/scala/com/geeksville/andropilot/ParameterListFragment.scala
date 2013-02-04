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
import android.support.v4.app.ListFragment

class ParameterListFragment extends ListFragment with AndroServiceFragment {

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    // Don't expand the view until we have _something_ to display
    debug("parameter list service connected")
    updateParameters()
  }

  override def onVehicleReceive = {
    case MsgParametersDownloaded =>
      handler.post(updateParameters _)
  }

  private def updateParameters() {
    // Don't expand the view until we have _something_ to display
    if (getActivity != null) {
      debug("updating parameters")
      makeAdapter.foreach(setListAdapter)
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    info("Item clicked: " + id)

    myVehicle.foreach { v =>
      if (position < v.parameters.size) {
        val param = v.parameters(position)
        val transaction = getFragmentManager.beginTransaction()

        var frag = new ParameterEditorFragment(param)
        frag.show(transaction, "paramedit_fragment")
      }
    }
  }

  private def makeAdapter() =
    for (v <- myVehicle if !v.parameters.isEmpty) yield {
      debug("Setting parameter list to " + v.parameters.size + " items")

      val asMap = v.parameters.toSeq.map { p =>
        Map("n" -> p.getId.getOrElse("?"), "v" -> p.getValue.getOrElse("?").toString).asJava
      }.asJava
      val fromKeys = Array("n", "v")
      val toFields = Array(R.id.param_name, R.id.param_value)
      //new SimpleAdapter(getActivity, asMap, R.layout.parameter_row, fromKeys, toFields)
      new SimpleAdapter(getActivity, asMap, R.layout.parameter_row, fromKeys, toFields)
    }
}
