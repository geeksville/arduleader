package com.geeksville.andropilot

import android.app.ListFragment
import android.os.Bundle
import android.widget.ArrayAdapter
import com.ridemission.scandroid.AndroidLogger
import android.widget.ListView
import android.view.View
import com.geeksville.flight.VehicleMonitor
import android.app.FragmentManager

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

  def setVehicle(v: VehicleMonitor) {
    vehicle = Some(v)
    debug("Setting parameter list to " + v.parameters.size + " items")
    // Don't expand the view until we have _something_ to display
    if (v.parameters.size != 0 && getActivity != null)
      setListAdapter(new ArrayAdapter(getActivity, android.R.layout.simple_list_item_1, v.parameters))
  }
}
