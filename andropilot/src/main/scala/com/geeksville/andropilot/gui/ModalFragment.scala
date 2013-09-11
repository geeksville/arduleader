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
import android.widget.Button
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.view.Gravity

class ModalFragment extends LayoutFragment(R.layout.modal_bar) with AndroServiceFragment {
  private def uploadWpButton = getView.findView(TR.upload_waypoint_button)
  private def modeTextView = getView.findView(TR.mode_text)
  private def modeButtonGroup = getView.findView(TR.mode_buttons)

  val errColor = Color.RED
  val warnColor = Color.YELLOW
  val okayColor = Color.GREEN

  private def postModeChange() {
    handler.post { () =>
      setModeFromVehicle()
      setButtons()
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

    case StatusText(s, severity) =>
      val isImportant = severity >= MsgStatusChanged.SEVERITY_HIGH
      handler.post { () =>
        handleStatus(s)
      }
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
    // Fade in the new text
    if (modeTextView.getText.toString != str) {
      fadeIn(modeTextView)
    }

    modeTextView.setTextColor(color)
    modeTextView.setText(str)

    modeTextView.setVisibility(View.VISIBLE)
  }

  private def handleStatus(msg: String) {
    val color = if (msg.contains("Failure")) errColor else okayColor
    setModeText(msg, errColor)

    // Go back to the regular status text after a few secs
    handler.postDelayed({ () => setModeFromVehicle() }, 5 * 1000)
  }

  private def fadeIn(v: View) {
    v.setAlpha(0f)
    v.animate().alpha(1f).setDuration(600)
  }

  private def setButtons() {
    myVehicle.foreach { v =>
      modeButtonGroup.removeAllViews()
      v.selectableModeNames(true).foreach { name =>
        val button = new Button(getActivity)
        button.setText(name)
        fadeIn(button)

        val lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0f)
        lp.gravity = Gravity.CENTER
        modeButtonGroup.addView(button, lp)

        button.onClick { b =>
          v ! DoSetMode(name)
        }
      }
    }
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
