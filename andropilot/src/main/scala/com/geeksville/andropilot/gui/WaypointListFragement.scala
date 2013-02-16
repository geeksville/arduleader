package com.geeksville.andropilot.gui
import android.widget.ListView
import android.view.View
import scala.collection.JavaConverters._
import android.widget.SimpleAdapter
import com.geeksville.flight._
import com.geeksville.util.ThreadTools._
import android.support.v4.app.ListFragment
import com.geeksville.andropilot.R
import com.geeksville.andropilot.service._
import android.os.Bundle
import android.widget.AbsListView
import android.view.ActionMode
import android.graphics.Color
import android.view.ViewGroup

class WaypointListFragment extends ListFragment with AndroServiceFragment {

  private var selected: Option[Waypoint] = None

  private var actionMode: Option[ActionMode] = None

  private val menuAdapter = new WaypointMenuItem {
    /**
     * Can the user see/change auto continue
     */
    override def isAllowAutocontinue = isEditable
    override def isAutocontinue = selected.map(_.msg.autocontinue != 0).getOrElse(false)
    override def isAutocontinue_=(b: Boolean) {
      selected.foreach { s =>
        s.msg.autocontinue = if (b) 1 else 0
        changed()
      }
    }

    override def isAltitudeEditable = isEditable
    override def altitude = selected.map(_.altitude).getOrElse(0.0f).toDouble
    override def altitude_=(n: Double) {
      selected.foreach { s =>
        s.msg.z = n.toFloat
        changed()
      }
    }

    /**
     * The menu has just changed our item
     */
    private def changed() {
      myVehicle.foreach(_ ! SendWaypoints)
    }

    override def isAllowGoto = true
    override def isAllowAdd = true
    override def isAllowChangeType = isEditable
    override def isAllowDelete = isEditable

    private def isEditable = selected.map(!_.isHome).getOrElse(false)

    /**
     * Have vehicle go to this waypoint
     */
    override def doGoto() {
      for { v <- myVehicle; s <- selected } yield {
        v ! DoSetCurrent(s.msg.seq)
        v ! DoSetMode("AUTO")
      }
    }

    override def doAdd() { throw new Exception("Not yet implemented") }

    override def doDelete() {
      for { v <- myVehicle; s <- selected } yield {
        // FIXME - we shouldn't be touching this
        v ! DoDeleteWaypoint(s.msg.seq)

        changed()
      }
    }

    override def typStr = selected.map(_.commandStr).getOrElse("unknown")
    override def typStr_=(s: String) {
      selected.foreach(_.commandStr = s)
      changed()
    }
  }

  private lazy val contextMenuCallback = new WaypointActionMode(getActivity) {

    override def shouldShowMenu = myVehicle.map(_.hasHeartbeat).getOrElse(false)

    // Called when the user exits the action mode
    override def onDestroyActionMode(mode: ActionMode) {
      super.onDestroyActionMode(mode)

      actionMode = None
    }
  }

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    // Don't expand the view until we have _something_ to display
    debug("parameter list service connected")
    handleWaypoints()
  }

  override def onViewCreated(view: View, bundle: Bundle) {
    super.onViewCreated(view, bundle)

    getListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
  }

  override def onVehicleReceive = {

    case MsgWaypointsChanged =>
      handler.post(handleWaypoints _)
  }

  /// menu choices might have changed)
  def invalidateContextMenu() {
    actionMode.foreach(_.invalidate())
  }

  private def handleWaypoints() {
    // Don't expand the view until we have _something_ to display
    if (getView != null) {
      debug("updating parameters")
      makeAdapter.foreach(setListAdapter)
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    info("Item clicked: " + id)

    myVehicle.foreach { v =>
      if (position < v.waypoints.size) {
        l.setSelection(position)
        // FIXME - set selection on map also?
        selected = Some(v.waypoints(position))

        // Start up action menu if necessary
        actionMode match {
          case Some(am) =>
            invalidateContextMenu() // menu choices might have changed
          case None =>
            actionMode = Some(getActivity.startActionMode(contextMenuCallback))
        }
      }
    }
  }

  private def makeAdapter() =
    for (v <- myVehicle if !v.waypoints.isEmpty) yield {
      debug("Setting waypoint list to " + v.waypoints.size + " items")

      val asMap = v.waypoints.zipWithIndex.map {
        case (p, i) =>
          Map("n" -> i, "name" -> p.longString).asJava
      }.asJava
      val fromKeys = Array("n", "name")
      val toFields = Array(R.id.waypoint_number, R.id.waypoint_name)
      new SimpleAdapter(getActivity, asMap, R.layout.waypoint_row, fromKeys, toFields) {

        // Show selected item in a color
        override def getView(position: Int, convertView: View, parent: ViewGroup) = {
          val itemView = super.getView(position, convertView, parent)
          if (selected.map(_.msg.seq).getOrElse(-1) == position)
            itemView.setBackgroundColor(0xA0FF8000) // orange
          else
            itemView.setBackgroundColor(Color.TRANSPARENT)
          itemView
        }
      }
    }
}
