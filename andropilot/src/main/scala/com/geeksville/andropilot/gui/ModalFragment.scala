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
import android.graphics.Color
import com.geeksville.andropilot.service.AndropilotService

class ModalFragment extends LayoutFragment(R.layout.modal_bar) with AndroServiceFragment {
  private def uploadWpButton = getView.findView(TR.upload_waypoint_button)
  private def modeTextView = getView.findView(TR.mode_text)

  val errColor = Color.RED
  val warnColor = Color.YELLOW
  val okayColor = Color.GREEN

  private def postModeChange() {
    handler.post { () =>
      setModeFromVehicle()
    }
  }

  override def onVehicleReceive = {

    case MsgWaypointDirty(dirty) =>
      handler.post { () =>
        setWPUploadVisibility(dirty)
      }

    case MsgModeChanged(_) =>
      postModeChange()

    case MsgFSMChanged(_) =>
      postModeChange()
  }

  private def setWPUploadVisibility(dirty: Boolean) {
    if (getView != null) {
      uploadWpButton.setVisibility(if (dirty) View.VISIBLE else View.GONE)
    }
  }

  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    myVehicle.foreach { v =>
      setWPUploadVisibility(v.isDirty)
    }

    uploadWpButton.onClick { b =>
      myVehicle.foreach { v =>
        v ! SendWaypoints
      }
    }
  }

  override protected def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)
    setModeFromVehicle()
  }

  def setModeText(str: String, color: Int) {
    modeTextView.setTextColor(color)
    modeTextView.setText(str)
    modeTextView.setVisibility(View.VISIBLE)
  }

  private def setModeFromVehicle() {
    val (msg, color) = myVehicle match {
      case Some(v) =>
        v.fsm.getState.getName match {
          case "VehicleFSM.WantInterface" =>
            "Looking for interface" -> errColor
          case "VehicleFSM.WantVehicle" =>
            "Looking for interface" -> errColor
          case "VehicleFSM.DownloadingWaypoints" =>
            "Downloading waypoints..." -> warnColor
          case "VehicleFSM.DownloadingParameters" =>
            "Downloading parameters..." -> warnColor
          case "VehicleFSM.DownloadedParameters" =>
            "Downloaded params (BUG!)" -> errColor
          case "VehicleFSM.Disarmed" =>
            "Not armed" -> errColor
          case "VehicleFSM.Armed" =>
            "Armed" -> warnColor
          case "VehicleFSM.Flying" =>
            v.currentMode -> okayColor
        }
      case None =>
        "No vehicle (BUG!)" -> errColor
    }

    setModeText(msg, color)
  }
}
