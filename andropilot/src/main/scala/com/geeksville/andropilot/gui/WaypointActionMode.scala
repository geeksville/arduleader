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
import com.google.android.gms.common.GooglePlayServicesUtil
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

abstract class WaypointActionMode(val context: Context) extends ActionMode.Callback with AndroidLogger {

  var selectedMarker: Option[WaypointMenuItem] = None

  private def setTypeOptions(s: Spinner) {
    val spinnerAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, Waypoint.commandNames)

    s.setAdapter(spinnerAdapter) // set the adapter
  }

  private def setTypeSpinner(s: Spinner, typStr: String) {
    // Crufty way of finding which element of spinner needs selecting
    def findIndex(str: String) = {
      val adapter = s.getAdapter

      (0 until adapter.getCount).find { i =>
        val is = adapter.getItem(i).toString
        is == str
      }
    }
    findIndex(typStr).foreach { n => s.setSelection(n) }

    // FIXME - if showing a more advanced type, also show the edit box to set # of turns or what have you...
  }

  // Called when the action mode is created; startActionMode() was called
  override def onCreateActionMode(mode: ActionMode, menu: Menu) = {
    // Inflate a menu resource providing context menu items
    val inflater = mode.getMenuInflater()
    inflater.inflate(R.menu.context_menu, menu)

    // Setup our type spinner
    val s = menu.findItem(R.id.menu_changetype).getActionView.asInstanceOf[Spinner] // find the spinner
    setTypeOptions(s)

    val editAlt = menu.findItem(R.id.menu_setalt).getActionView.asInstanceOf[TextView]
    editAlt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)

    debug("Creating actionMode")

    // Apparently IME_ACTION_DONE fires when the user leaves the edit text
    editAlt.setOnEditorActionListener(new TextView.OnEditorActionListener {
      override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent) = {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          val str = v.getText.toString
          debug("Editing completed: " + str)
          try {
            selectedMarker.foreach(_.altitude = str.toDouble)
          } catch {
            case ex: Exception =>
              error("Error parsing user alt: " + ex)
          }
          selectedMarker.foreach { m => v.setText(m.altitude.toString) }

          // Force the keyboard to go away
          val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
          imm.hideSoftInputFromWindow(v.getWindowToken, 0)

          // We handled the event
          true
        } else
          false
      }
    })

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
    val changetype = menu.findItem(R.id.menu_changetype)
    val autocontinue = menu.findItem(R.id.menu_autocontinue)

    // Default to nothing
    Seq(goto, add, delete, setalt, changetype, autocontinue).foreach(_.setVisible(false))

    debug("Prepare " + selectedMarker)

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
              val editAlt = menu.findItem(R.id.menu_setalt).getActionView.asInstanceOf[TextView]
              editAlt.setText(marker.altitude.toString)
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
              }
              s.onItemSelected(spinnerListener)

              changetype.setVisible(true)
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

