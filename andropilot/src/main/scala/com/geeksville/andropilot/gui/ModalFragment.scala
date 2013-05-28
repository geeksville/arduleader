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

class ModalFragment extends LayoutFragment(R.layout.modal_bar) with AndroServiceFragment {
  private lazy val uploadWpButton = getView.findView(TR.upload_waypoint_button)

  override def onVehicleReceive = {

    case MsgWaypointDirty(dirty) =>
      handler.post { () =>
        setVisibility(dirty)
      }
  }

  private def setVisibility(dirty: Boolean) {
    if (getView != null) {
      uploadWpButton.setVisibility(if (dirty) View.VISIBLE else View.GONE)
    }
  }

  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    myVehicle.foreach { v =>
      setVisibility(v.isDirty)
    }

    uploadWpButton.onClick { b =>
      myVehicle.foreach { v =>
        v ! SendWaypoints
      }
    }
  }
}
