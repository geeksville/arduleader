package com.geeksville.andropilot

import android.app.DialogFragment
import com.ridemission.scandroid.AndroidLogger
import android.view._
import android.os.Bundle
import com.geeksville.flight.VehicleMonitor
import com.ridemission.scandroid.AndroidUtil._
import TypedResource._
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Toast

class ParameterEditorFragment(val param: VehicleMonitor#ParamValue) extends DialogFragment with AndroidLogger {
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
/*

    v.findView(TR.button_ok).onClick { view =>
      
    }
    v
  }
08
 
09
    public override View OnCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
10
    {
11
        var view = inflater.Inflate(Resource.Layout.dialog_fragment_layout, container, false);
12
        var textView = view.FindViewById<TextView>(Resource.Id.dialog_text_view);
13
             
14
        view.FindViewById<Button>(Resource.Id.dialog_button).Click += delegate
15
                                                                              {
16
                                                                                   
17
                                                                                  textView.Text = "You clicked the button " + _clickCount++ + " times.";
18
                                                                              };
19
        // Set up a handler to dismiss this DialogFragment when this button is clicked.
20
            view.FindViewById<Button>(Resource.Id.dismiss_dialog_button).Click += (sender, args) => Dismiss();
21
            return view;
22
        }
23
    }
*/ 