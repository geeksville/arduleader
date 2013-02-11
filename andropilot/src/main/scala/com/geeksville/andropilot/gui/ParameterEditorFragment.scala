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

class ParameterEditorFragment(val param: VehicleModel#ParamValue) extends DialogFragment with AndroidLogger {
  setCancelable(true)

  private def toast(str: String) {
    Toast.makeText(getActivity, str, Toast.LENGTH_LONG).show()
  }

  override def onCreateDialog(savedInstanceState: Bundle) = {
    val builder = new AlertDialog.Builder(getActivity)
    // Get the layout inflater
    val inflater = getActivity.getLayoutInflater

    val v = inflater.inflate(R.layout.paramedit_fragment, null)

    val valueView = v.findView(TR.param_value)

    param.getId.foreach { id => v.findView(TR.param_label).setText(id) }
    param.getValue.foreach { pv => valueView.setText(pv.toString) }

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
      .setTitle("Set parameter")
      // Add action buttons
      .setPositiveButton("Ok", new DialogInterface.OnClickListener {
        override def onClick(dialog: DialogInterface, id: Int) {
          info("User clicked ok")
          val str = valueView.getText.toString
          try {
            param.setValue(str.toFloat)
          } catch {
            case ex: Exception =>
              toast("Can't set value: " + ex.getMessage)
          }
          getDialog.dismiss()
        }
      })
      .setNegativeButton("Cancel", new DialogInterface.OnClickListener {
        override def onClick(dialog: DialogInterface, id: Int) {
          info("User clicked cancel")
          getDialog.cancel()
        }
      });
    builder.create()
  }
}
