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
import android.widget.ArrayAdapter
import com.geeksville.flight._
import java.util.LinkedList
import com.geeksville.andropilot.R
import android.view.View
import com.ridemission.scandroid.ObservableAdapter
import com.geeksville.flight.StatusText
import android.widget.BaseAdapter
import com.geeksville.andropilot.AndropilotPrefs

/**
 * Common behavior for both the overview and floating instruments
 */
class VehicleInfoFragment(layoutId: Int) extends LayoutFragment(layoutId) with AndroServiceFragment with AndropilotPrefs {
  protected def altView = getView.findView(TR.altitude)
  protected def airspeedView = getView.findView(TR.airspeed)
  protected def batteryView = getView.findView(TR.battery_volt)
  protected def numSatView = getView.findView(TR.gps_numsats)
  protected def rssiLocalView = getView.findView(TR.rssi_local)

  override def onVehicleReceive = {
    case l: Location =>
      //debug("Handling location: " + l)
      handler.post { () =>
        if (getView != null) {
          myVehicle.foreach { v =>
            onLocationUpdate(v, l)
          }
        }
      }

    case MsgSysStatusChanged =>
      handler.post { () =>
        if (getView != null) {
          myVehicle.foreach { v =>
            onStatusUpdate(v)
          }
        }
      }
  }

  /**
   * called in gui thread
   */
  protected def onStatusUpdate(v: VehicleModel) {

  }

  protected def onLocationUpdate(v: VehicleModel, l: Location) {

  }

}

class OverviewFragment extends VehicleInfoFragment(R.layout.vehicle_overview) {

  private def latView = getView.findView(TR.latitude)
  private def lonView = getView.findView(TR.longitude)
  private def groundspeedView = getView.findView(TR.groundspeed)
  private def devRowView = getView.findView(TR.dev_row)
  private def devInfoView = getView.findView(TR.dev_info)

  override def onResume() = {
    super.onResume()

    devRowView.setVisibility(if (developerMode) View.VISIBLE else View.GONE)
  }

  private def showDevInfo() {
    // Show current state
    myVehicle.foreach { v =>
      val stateName = v.fsm.getState.getName.split('.')(1)
      val status = v.systemStatus.getOrElse(-1)
      Option(devInfoView).foreach(_.setText(s"$stateName/$status"))
    }
  }

  /**
   * called in gui thread
   */
  override def onStatusUpdate(v: VehicleModel) {
    v.radio.foreach { n =>
      val local = n.rssi - n.noise
      val rem = n.remrssi - n.remnoise

      rssiLocalView.setText(local.toString + "/" + rem.toString)
    }
    v.batteryVoltage.foreach { n =>
      val socStr = v.batteryPercent.map { pct => " (%d%%)".format((pct * 100).toInt) }.getOrElse("")
      batteryView.setText(n.toString + "V " + socStr)
    }

    // Show current state
    showDevInfo()
  }

  override def onLocationUpdate(v: VehicleModel, l: Location) {
    val degSymbol = "\u00B0"
    latView.setText("%.4f".format(l.lat) + degSymbol)
    lonView.setText("%.4f".format(l.lon) + degSymbol)
    altView.setText("%.1f".format(v.bestAltitude) + " m")
    v.vfrHud.foreach { hud =>
      airspeedView.setText("%.1f".format(hud.airspeed) + " m/s")
      groundspeedView.setText("%.1f".format(hud.groundspeed) + " m/s")
    }
    val numSats = v.numSats.getOrElse("?")
    val hdop = v.hdop.getOrElse("?")
    numSatView.setText("%s / %s".format(numSats, hdop))
  }

  override def onVehicleReceive = ({
    case MsgFSMChanged(_) =>
      handler.post(showDevInfo _)
  }: PartialFunction[Any, Unit]).orElse(super.onVehicleReceive)

}
