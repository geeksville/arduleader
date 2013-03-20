package com.geeksville.andropilot.gui

import com.ridemission.scandroid.AndroidLogger
import android.view._
import android.os.Bundle
import com.geeksville.flight.VehicleModel
import com.ridemission.scandroid.AndroidUtil._
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Toast
import android.support.v4.app.DialogFragment
import com.geeksville.andropilot.R
import com.geeksville.andropilot.TypedResource._
import com.geeksville.andropilot.TR
import android.support.v4.app.Fragment
import android.text.method.ScrollingMovementMethod

class ParameterInfoFragment extends Fragment with AndroidLogger {

  private var param: Option[VehicleModel#ParamValue] = None

  // private def nameView = getView.findView(TR.param_name)
  private def humanNameView = getView.findView(TR.param_humanname)
  private def docsView = getView.findView(TR.param_doc_view)
  // private def valueView = getView.findView(TR.param_value)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    // Inflate the layout for this fragment
    val v = inflater.inflate(R.layout.param_edit, container, false)

    updateView(v)
    v
  }

  private def updateView(view: View = getView) {
    Option(view).foreach { v =>
      param.map { p =>
        v.setVisibility(View.VISIBLE)
        // nameView.setText(p.getId.getOrElse(""))
        p.docs.map { d =>
          humanNameView.setText(d.humanName)
          docsView.setText(d.documentation)
          docsView.setMovementMethod(new ScrollingMovementMethod)
        }.getOrElse {
          humanNameView.setText("")
          docsView.setText("")
        }
      }.getOrElse {
        // v.setVisibility(View.GONE)
      }
    }
  }

  def setParam(p: Option[VehicleModel#ParamValue]) {
    param = p
    updateView()
  }
}
