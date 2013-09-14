package com.geeksville.andropilot.gui

import com.geeksville.gmaps.Scene
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.geeksville.flight._
import com.geeksville.akka._
import com.geeksville.mavlink._
import com.ridemission.scandroid.AndroidLogger
import com.google.android.gms.maps.CameraUpdateFactory
import com.geeksville.util.ThreadTools._
import android.os.Bundle
import com.ridemission.scandroid.UsesPreferences
import android.widget.Toast
import com.geeksville.gmaps.Segment
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.geeksville.flight.MsgWaypointsChanged
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.geeksville.gmaps.SmartMarker
import android.graphics.Color
import android.widget.TextView
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.geeksville.flight.Waypoint
import com.geeksville.andropilot.R
import scala.Option.option2Iterable
import com.geeksville.andropilot.service._
import com.geeksville.flight.DoGotoGuided
import com.geeksville.andropilot.AndropilotPrefs
import org.mavlink.messages.MAV_CMD
import com.geeksville.flight.DoAddWaypoint
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.geeksville.flight.Waypoint
import android.app.Activity
import android.view.View
import com.ridemission.scandroid.AndroidUtil._
import android.support.v4.app.FragmentActivity
import android.text.TextWatcher
import android.text.Editable

abstract class WaypointActionMode(val context: FragmentActivity) extends ActionMode.Callback with AndroidLogger {

  var selectedMarker: Option[WaypointMenuItem] = None

  private def setTypeOptions(s: Spinner) {
    val spinnerAdapter = new ArrayAdapter(MainActivity.getThemedContext(context),
      android.R.layout.simple_spinner_dropdown_item, Waypoint.commandNames)

    s.setAdapter(spinnerAdapter) // set the adapter
  }

  private def setTypeSpinner(s: Spinner, typStr: String) {
    // Crufty way of finding which element of spinner needs selecting
    def findIndex(str: String) = {
      val adapter = s.getAdapter

      val r = (0 until adapter.getCount).find { i =>
        val is = adapter.getItem(i).toString
        is == str
      }

      debug("Typespinner index=" + r)
      r
    }
    findIndex(typStr).foreach { n => s.setSelection(n) }

    // FIXME - if showing a more advanced type, also show the edit box to set # of turns or what have you...
  }

  private def initEditText(tv: TextView, doGet: WaypointMenuItem => Float, onSet: (WaypointMenuItem, Float) => Unit) {
    tv.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED)
    tv.setImeOptions(EditorInfo.IME_ACTION_DONE)

    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.showSoftInput(tv, InputMethodManager.SHOW_IMPLICIT)

    // Every time the user changes the value _immediately_ update the marker (in case they click "Goto" etc..)
    tv.addTextChangedListener(new TextWatcher {
      def afterTextChanged(e: Editable) {
        try {
          // FIXME - this will also get invoked every time _we_ change the value
          val f = tv.getText.toString.toFloat
          selectedMarker.foreach(onSet(_, f))
        } catch {
          case ex: Exception =>
            error("Error parsing user entry: " + ex)
        }
      }

      def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
      def onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    })

    // Apparently IME_ACTION_DONE fires when the user leaves the edit text
    tv.setOnEditorActionListener(new TextView.OnEditorActionListener {
      override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent) = {
        debug("actionId: " + actionId)
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          val str = v.getText.toString
          debug("Editing completed: " + str)

          // Read back the value from the marker, in case it could only approximate what the user wanted
          selectedMarker.foreach { m => v.setText(doGet(m).toString) }

          // Force the keyboard to go away
          imm.hideSoftInputFromWindow(v.getWindowToken, 0)

          // We handled the event
          true
        } else
          false
      }
    })
  }

  // Called when the action mode is created; startActionMode() was called
  override def onCreateActionMode(mode: ActionMode, menu: Menu) = {
    // Inflate a menu resource providing context menu items
    val inflater = mode.getMenuInflater()
    inflater.inflate(R.menu.context_menu, menu)

    // Setup our type spinner
    val s = menu.findItem(R.id.menu_changetype).getActionView.asInstanceOf[Spinner] // find the spinner
    setTypeOptions(s)

    debug("Creating actionMode")

    initEditText(menu.findItem(R.id.menu_setalt).getActionView.asInstanceOf[TextView], { m => m.altitude.toFloat }, { (m, v) => m.altitude = v })
    initEditText(menu.findItem(R.id.menu_param1).getActionView.asInstanceOf[TextView], { m => m.getParam(0) }, { (m, v) => m.setParam(0, v) })
    initEditText(menu.findItem(R.id.menu_param2).getActionView.asInstanceOf[TextView], { m => m.getParam(1) }, { (m, v) => m.setParam(1, v) })
    initEditText(menu.findItem(R.id.menu_param3).getActionView.asInstanceOf[TextView], { m => m.getParam(2) }, { (m, v) => m.setParam(2, v) })
    initEditText(menu.findItem(R.id.menu_param4).getActionView.asInstanceOf[TextView], { m => m.getParam(3) }, { (m, v) => m.setParam(3, v) })

    true // Did create a menu
  }

  def shouldShowMenu = true

  // Called each time the action mode is shown. Always called after onCreateActionMode, but
  // may be called multiple times if the mode is invalidated.
  override def onPrepareActionMode(mode: ActionMode, menu: Menu) = {
    val goto = menu.findItem(R.id.menu_goto)
    val add = menu.findItem(R.id.menu_add)
    val delete = menu.findItem(R.id.menu_delete)
    val setalt = menu.findItem(R.id.menu_setalt)
    val param1 = menu.findItem(R.id.menu_param1)
    val param2 = menu.findItem(R.id.menu_param2)
    val param3 = menu.findItem(R.id.menu_param3)
    val param4 = menu.findItem(R.id.menu_param4)
    val changetype = menu.findItem(R.id.menu_changetype)
    val autocontinue = menu.findItem(R.id.menu_autocontinue)

    // Default to nothing
    Seq(goto, add, delete, setalt, param1, param2, param3, param4, changetype, autocontinue).foreach(_.setVisible(false))

    debug("Prepare " + selectedMarker)

    if (mode != null) {
      // We only enable options if we are talking to a real vehicle
      if (shouldShowMenu) {
        selectedMarker match {
          case None =>
            // Nothing selected - exit context mode 
            mode.finish()

          case Some(marker) =>
            if (!marker.isAllowContextMenu)
              mode.finish()
            else {
              if (marker.isAllowAutocontinue) {
                autocontinue.setVisible(true)
                autocontinue.setChecked(marker.isAutocontinue)
              }

              if (marker.isAltitudeEditable) {
                setalt.setVisible(true)
                setalt.getActionView.asInstanceOf[TextView].setText(marker.altitude.toString)
              }

              val numParams = marker.numParams
              if (numParams >= 1) {
                param1.setVisible(true)
                param1.getActionView.asInstanceOf[TextView].setText(marker.getParam(0).toString)
              }
              if (numParams >= 2) {
                param2.setVisible(true)
                param2.getActionView.asInstanceOf[TextView].setText(marker.getParam(1).toString)
              }
              if (numParams >= 3) {
                param3.setVisible(true)
                param3.getActionView.asInstanceOf[TextView].setText(marker.getParam(2).toString)
              }
              if (numParams >= 4) {
                param4.setVisible(true)
                param4.getActionView.asInstanceOf[TextView].setText(marker.getParam(3).toString)
              }

              if (marker.isAllowGoto)
                goto.setVisible(true)

              if (marker.isAllowAdd)
                add.setVisible(true)

              if (marker.isAllowDelete)
                delete.setVisible(true)

              if (marker.isAllowChangeType) {
                val s = changetype.getActionView.asInstanceOf[Spinner]
                setTypeSpinner(s, marker.typStr)

                def spinnerListener(parent: Spinner, selected: View, pos: Int, id: Long) {
                  val newtyp = s.getAdapter.getItem(pos).toString
                  debug("Type selected: " + newtyp)
                  marker.typStr = newtyp
                  Option(mode).foreach(_.invalidate()) // Repopulate the options - but be careful, our action mode might already be gone
                }
                s.onItemSelected(spinnerListener)

                changetype.setVisible(true)
              }
            }
        }
      }
    }
    true // Return false if nothing is done
  }

  // Called when the user selects a contextual menu item
  override def onActionItemClicked(mode: ActionMode, item: MenuItem) =
    selectedMarker.map { marker =>
      item.getItemId match {
        case R.id.menu_autocontinue =>
          debug("Toggle continue, oldmode " + item.isChecked)
          item.setChecked(!item.isChecked)
          marker.isAutocontinue = item.isChecked
          true

        case R.id.menu_goto =>
          marker.doGoto()
          mode.finish() // Action picked, so close the CAB
          true

        case R.id.menu_add =>
          marker.doAdd()
          mode.finish() // Action picked, so close the CAB
          true

        case R.id.menu_delete =>
          marker.doDelete()
          mode.finish() // Action picked, so close the CAB
          true

        case _ =>
          false
      }
    }.getOrElse(false)
}

