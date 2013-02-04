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
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw
import com.geeksville.flight.MsgRcChannelsChanged
import com.geeksville.util.ThreadTools._
import android.support.v4.app.ListFragment

class RcChannelsFragment extends ListFragment with AndroServicePage {

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

  override def onVehicleReceive = {
    case MsgRcChannelsChanged(_) =>
      if (isVisible) {
        //debug("Received Rc channels")
        handler.post(updateList _)
      }
  }

  private def updateList() {
    makeAdapter.foreach(setListAdapter)
  }

  private def rcToSeq(m: msg_rc_channels_raw) =
    Seq("Channel 1" -> m.chan1_raw, "Channel 2" -> m.chan2_raw, "Channel 3" -> m.chan3_raw, "Channel 4" -> m.chan4_raw,
      "Channel 5" -> m.chan5_raw, "Channel 6" -> m.chan6_raw, "Channel 7" -> m.chan7_raw, "Channel 8" -> m.chan8_raw,
      "Rssi" -> m.rssi)

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
