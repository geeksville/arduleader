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
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ridemission.scandroid.AndroidUtil._
import TypedResource._
import android.widget.ArrayAdapter
import com.geeksville.flight._
import java.util.LinkedList

class OverviewFragment extends Fragment with AndroServiceFragment {

  private lazy val statusItems = new ArrayAdapter(getActivity, android.R.layout.simple_list_item_1, new LinkedList[String]())

  private lazy val latView = getView.findView(TR.latitude)
  private lazy val lonView = getView.findView(TR.longitude)
  private lazy val altView = getView.findView(TR.altitude)
  private lazy val numSatView = getView.findView(TR.gps_numsats)
  private lazy val rssiLocalView = getView.findView(TR.rssi_local)
  private lazy val batteryView = getView.findView(TR.battery_volt)
  private lazy val listView = getView.findView(TR.status_list)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    // Inflate the layout for this fragment
    val v = inflater.inflate(R.layout.vehicle_overview, container, false)

    v.findView(TR.status_list).setAdapter(statusItems)

    v
  }

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    // Don't expand the view until we have _something_ to display
    if (isVisible) {
      debug("Setting rcchannel")
      // updateList()
    }
  }

  override def onVehicleReceive = {
    case l: Location =>
      //debug("Handling location: " + l)
      handler.post { () =>
        val degSymbol = "\u00B0"
        latView.setText(l.lat.toString + degSymbol)
        lonView.setText(l.lon.toString + degSymbol)
        altView.setText(l.alt + " m")
        myVehicle.foreach { v =>
          v.numSats.foreach { n => numSatView.setText(n.toString) }
        }
      }

    case MsgSysStatusChanged =>
      handler.post { () =>
        myVehicle.foreach { v =>
          v.radio.foreach { n =>
            rssiLocalView.setText(n.rssi.toString + "/" + n.remrssi.toString)
          }
          v.batteryVoltage.foreach { n =>
            val socStr = v.batteryPercent.map { pct => " (%d%%)".format((pct * 100).toInt) }.getOrElse("")
            batteryView.setText(n.toString + "V " + socStr)
          }
        }
      }

    case MsgStatusChanged(s) =>
      debug("Status changed: " + s)
      handler.post { () =>

        val maxNumStatus = 10

        statusItems.add(s)
        if (statusItems.getCount > maxNumStatus)
          statusItems.remove(statusItems.getItem(0))
        statusItems.notifyDataSetChanged()
        listView.smoothScrollToPosition(statusItems.getCount - 1)
      }
  }

}
