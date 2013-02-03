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
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw
import com.geeksville.flight.MsgRcChannelsChanged

class RcChannelsFragment extends ListFragment with AndroidLogger with AndroServiceClient {

  implicit def context = getActivity

  override def onActivityCreated(saved: Bundle) {
    super.onActivityCreated(saved)
  }

  override def onResume() {
    super.onResume()

    serviceOnResume()
  }

  override def onPause() {
    serviceOnPause()

    super.onPause()
  }

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    // Don't expand the view until we have _something_ to display
    if (getActivity != null) {
      debug("Setting rcchannel")
      makeAdapter.foreach(setListAdapter)
    }
  }

  override def onVehicleReceive = {
    case MsgRcChannelsChanged(_) =>
      debug("Received Rc channels - fixme")
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    info("Item clicked: " + id)
  }

  private def rcToSeq(m: msg_rc_channels_raw) =
    Seq("ch1" -> m.chan1_raw, "ch2" -> m.chan2_raw, "ch3" -> m.chan3_raw, "ch4" -> m.chan4_raw,
      "ch5" -> m.chan5_raw, "ch6" -> m.chan6_raw, "ch7" -> m.chan7_raw, "ch8" -> m.chan8_raw,
      "rssi" -> m.rssi)

  private def makeAdapter() = {
    for { v <- myVehicle; rc <- v.rcChannels } yield {
      val seq = rcToSeq(rc)

      val asMap = seq.map {
        case (n, v) =>
          Map("n" -> n, "v" -> v.toString).asJava
      }.asJava

      val fromKeys = Array("n", "v")
      val toFields = Array(R.id.rcch_name, R.id.rcch_value)
      new SimpleAdapter(getActivity, asMap, R.layout.rcchannel_row, fromKeys, toFields)
    }
  }
}
