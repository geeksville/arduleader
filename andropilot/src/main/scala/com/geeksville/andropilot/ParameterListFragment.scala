package com.geeksville.andropilot

import android.app.ListFragment
import android.os.Bundle
import android.widget.ArrayAdapter
import com.ridemission.scandroid.AndroidLogger
import android.widget.ListView
import android.view.View
import com.geeksville.flight.VehicleMonitor

class ParameterListFragment extends ListFragment with AndroidLogger {

  override def onActivityCreated(saved: Bundle) {
    super.onActivityCreated(saved);
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    info("Item clicked: " + id)
  }

  def setVehicle(v: VehicleMonitor) {
    setListAdapter(new ArrayAdapter(getActivity, android.R.layout.simple_list_item_1, v.parameters))
  }
}
