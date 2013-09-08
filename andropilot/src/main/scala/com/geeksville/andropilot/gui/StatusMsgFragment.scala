package com.geeksville.andropilot.gui

import android.os.Bundle
import android.widget.ArrayAdapter
import scala.collection.JavaConverters._
import com.geeksville.util.ThreadTools._
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ridemission.scandroid.AndroidUtil._
import com.geeksville.andropilot.TypedResource._
import com.geeksville.andropilot.TR
import com.geeksville.andropilot.service.AndropilotService
import android.widget.ArrayAdapter
import com.geeksville.flight._
import java.util.LinkedList
import com.geeksville.andropilot.R
import android.view.View
import com.ridemission.scandroid.ObservableAdapter
import com.geeksville.flight.StatusText
import android.widget.BaseAdapter

case object NeverSent

class StatusMsgFragment extends LayoutFragment(R.layout.statusmsg_fragment) with AndroServiceFragment {

  private lazy val listView = getView.findView(TR.status_list)

  override def onViewCreated(v: View) = {
    val list = v.findView(TR.status_list)
    list.setItemsCanFocus(false)
    updateListAdapter()
  }

  override protected def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)
    updateListAdapter()
  }

  private def updateListAdapter() {
    for {
      myView <- Option(getView)
      v <- myVehicle
    } yield {
      if (listView.getAdapter == null) {
        val adapt = new ObservableAdapter(getActivity, R.layout.simple_list_item_1_small, v.statusMessages)
        listView.setAdapter(adapt)
        listView.smoothScrollToPosition(v.statusMessages.size - 1)
      }
    }
  }
  override def onVehicleReceive = {
    case NeverSent =>
  }
}
