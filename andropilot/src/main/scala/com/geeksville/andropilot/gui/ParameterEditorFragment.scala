package com.geeksville.andropilot.gui

import com.ridemission.scandroid._
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
import com.geeksville.andropilot.FlurryClient
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.view.inputmethod.InputMethodManager
import android.content.Context

class ParameterEditorFragment(val param: VehicleModel#ParamValue) 
extends DialogFragment with AndroidLogger with FlurryClient with UsesResources {
  setCancelable(true)

    implicit def context: Context = getActivity

  override def onCreateDialog(savedInstanceState: Bundle) = {
    val builder = new AlertDialog.Builder(getActivity)
    // Get the layout inflater
    val inflater = getActivity.getLayoutInflater

    val v = inflater.inflate(R.layout.paramedit_fragment, null)

    val valueView = v.findView(TR.param_oldvalue)

    param.getId.foreach { id => v.findView(TR.param_label).setText(id) }
    param.getValue.foreach { pv => valueView.setText(pv.toString) }

    // Make sure the keyboard shows
    val imm = getActivity.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.showSoftInput(valueView, InputMethodManager.SHOW_IMPLICIT)

    // Does this parameter have specified value choices?  If so, then use a spinner instead of edittext
    param.docs.flatMap(_.valueMap).foreach { valueMap =>

      val optionNames = valueMap.values.toArray
      val optionValues = valueMap.keys.toArray

      // select correct item (if current value doesn't get found just leave the edit text field)
      valueMap.get(param.getInt.get).foreach { curValStr =>
        val spinner = v.findView(TR.param_spinner)

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = new ArrayAdapter(getActivity, android.R.layout.simple_spinner_item, optionNames)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter)

        def listener(parent: Spinner, selected: View, pos: Int, id: Long) {
          val newval = optionValues(pos)
          valueView.setText(newval.toString)
        }
        spinner.onItemSelected(listener)

        val curIndex = optionNames.indexOf(curValStr)
        spinner.setSelection(curIndex)
        // show the spinner 
        spinner.setVisibility(View.VISIBLE)
        // valueView.setVisibility(View.GONE)
      }
    }

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
            usageEvent("param_edited", "name" -> param.getId.get, "value" -> str)
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