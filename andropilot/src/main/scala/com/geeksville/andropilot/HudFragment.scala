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
import org.mavlink.messages.ardupilotmega.msg_attitude

class HudFragment extends Fragment with AndroServiceFragment {

  private def hud = getView.findView(TR.hud_view)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    // Inflate the layout for this fragment
    val v = inflater.inflate(R.layout.hud_fragment, container, false)

    v
  }

  override def onVehicleReceive = {
    case msg: msg_attitude =>
      //debug(msg.toString)
      hud.newFlightData(msg.roll, msg.pitch, msg.yaw);

    case l: Location =>
      //debug("Handling location: " + l)
      handler.post { () =>
        if (getView != null) {
          val degSymbol = "\u00B0"
          //latView.setText(l.lat.toString + degSymbol)
          //lonView.setText(l.lon.toString + degSymbol)
          //altView.setText(l.alt + "m")
          hud.setAltitude(l.alt + "m")
          // myVehicle.foreach { v =>v.numSats.foreach { n => numSatView.setText(n.toString) } }
        }
      }

    case MsgSysStatusChanged =>
      handler.post { () =>
        if (getView != null) {
          myVehicle.foreach { v =>
            v.radio.foreach { n =>
              //rssiLocalView.setText(n.rssi.toString + "/" + n.remrssi.toString)
            }
            v.batteryVoltage.foreach { n =>
              val socStr = v.batteryPercent.map { pct => "%d%%".format((pct * 100).toInt) }.getOrElse("")
              //batteryView.setText(n.toString + "V " + socStr)

              hud.setBatteryRemaining(socStr)
              hud.setBatteryMVolt(n.toString + "V")
            }
          }
        }
      }

    case MsgStatusChanged(s) =>
      debug("Status changed: " + s)
      handler.post { () =>
        if (getView != null) {

          //statusItems.add(s)
        }
      }
  }

}
