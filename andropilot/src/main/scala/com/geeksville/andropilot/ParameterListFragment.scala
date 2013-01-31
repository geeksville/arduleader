package com.geeksville.andropilot

import android.app.ListFragment
import android.os.Bundle
import android.widget.ArrayAdapter
import com.ridemission.scandroid.AndroidLogger
import android.widget.ListView
import android.view.View
import com.geeksville.flight.VehicleMonitor
import android.app.FragmentManager
import scala.collection.JavaConverters._
import android.widget.SimpleAdapter

class ParameterListFragment extends ListFragment with AndroidLogger {

  private var vehicle: Option[VehicleMonitor] = None

  override def onActivityCreated(saved: Bundle) {
    super.onActivityCreated(saved);
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    info("Item clicked: " + id)

    vehicle.foreach { v =>
      val param = v.parameters(position)
      val transaction = getFragmentManager.beginTransaction()

      var frag = new ParameterEditorFragment(param)
      frag.show(transaction, "paramedit_fragment")
    }
  }

  private def makeAdapter() = {
    // new ArrayAdapter(getActivity, android.R.layout.simple_list_item_1, vehicle.get.parameters)
    val asMap = vehicle.get.parameters.toSeq.map { p =>
      Map("n" -> p.getId.getOrElse("?"), "v" -> p.getValue.getOrElse("?").toString).asJava
    }.asJava
    val fromKeys = Array("n", "v")
    val toFields = Array(R.id.param_name, R.id.param_value)
    //new SimpleAdapter(getActivity, asMap, R.layout.parameter_row, fromKeys, toFields)
    new SimpleAdapter(getActivity, asMap, R.layout.parameter_row, fromKeys, toFields)
  }

  def setVehicle(v: VehicleMonitor) {
    vehicle = Some(v)
    debug("Setting parameter list to " + v.parameters.size + " items")
    // Don't expand the view until we have _something_ to display
    if (v.parameters.size != 0 && getActivity != null)
      setListAdapter(makeAdapter)
  }
}
