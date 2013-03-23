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
import android.view.LayoutInflater
import com.geeksville.andropilot.TypedResource._
import com.geeksville.andropilot.TR

class WaypointListFragment extends ListAdapterHelper[Waypoint] with AndroServiceFragment {

  private var selected: Option[Waypoint] = None

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
    override def isAllowAdd = false // No good way to select location on this view.  For now just make them use map
    override def isAllowChangeType = {
      debug("allow change type=" + isEditable)
      isEditable
    }

    override def isAllowDelete = isEditable

    private def isEditable = selected.map(!_.isHome).getOrElse(false)

    override def numParams = selected.map(_.numParamsUsed).getOrElse(0)
    override def getParam(i: Int) = selected.map(_.getParam(i)).getOrElse(0)
    override def setParam(i: Int, n: Float) = selected.foreach { s =>
      s.setParam(i, n)
      changed()
    }

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

    override def typStr = {
      val r = selected.map(_.commandStr).getOrElse("unknown")
      debug("Returning typStr=" + r)
      r
    }

    override def typStr_=(s: String) {
      selected.foreach(_.commandStr = s)
      changed()
    }
  }

  private lazy val contextMenuCallback = new WaypointActionMode(getActivity) with ActionModeCallback {

    selectedMarker = Some(menuAdapter)

    override def shouldShowMenu = myVehicle.map(_.hasHeartbeat).getOrElse(false)

    // Called when the user exits the action mode
    override def onDestroyActionMode(mode: ActionMode) {
      super.onDestroyActionMode(mode)

      setSelection(None)
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

  private def handleWaypoints() {
    // Don't expand the view until we have _something_ to display
    //if (getView != null) {
    debug("updating waypoints")
    setAdapter()
    /*
     * I don't think this is necessary anymore
    if (getView != null) {
      Option(getListView).foreach(_.invalidate())
    }
    * 
    */
  }

  private def listView = Option(getListView)

  /**
   * Select a particular waypoint (or None) and update our action bar
   */
  private def setSelection(s: Option[Waypoint]) {
    for { v <- myVehicle; l <- listView } yield {
      // FIXME - set selection on map also?
      selected = s

      selected.map { sel =>
        // Handle the new selection
        l.setSelection(sel.msg.seq)

        // Start up action menu if necessary
        startActionMode(contextMenuCallback)
      }.getOrElse {
        // Nothing selected, end the action mode
        stopActionMode()
      }

      l.invalidateViews()
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    info("Item clicked: " + id + "/" + position)

    myVehicle.foreach { v =>
      if (position < v.waypoints.size) {
        setSelection(Some(v.waypoints(position)))
      }
    }
  }

  protected def makeRow(i: Int, p: Waypoint) = Map("num" -> i, "type" -> p.typeString, "args" -> p.argumentsString, "icon" -> WaypointUtil.toDrawable(p.msg.command))
  protected def rowId = R.layout.waypoint_row

  protected override def fromKeys = Array("num", "type", "args", "icon")
  protected override val toFields = Array(R.id.waypoint_number, R.id.waypoint_type, R.id.waypoint_args, R.id.waypoint_iconcol)

  override def isSelected(p: Int) = selected.map(_.msg.seq).getOrElse(-1) == p

  private def setAdapter() {
    for (v <- myVehicle) yield {
      debug("Setting waypoint list to " + v.waypoints.size + " items")
      setAdapter(v.waypoints)
    }
  }
}
