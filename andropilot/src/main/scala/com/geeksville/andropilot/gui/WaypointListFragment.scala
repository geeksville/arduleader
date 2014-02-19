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
import com.geeksville.flight.MsgWaypointCurrentChanged
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.model.LatLng
import com.geeksville.akka.InstrumentedActor
import android.content.Intent
import com.geeksville.util.Using._
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import com.geeksville.andropilot.UsesDirectories
import com.geeksville.util.FileTools
import com.ridemission.scandroid.SimpleDialogClient
import android.view.MenuInflater

class WaypointListFragment extends ListAdapterHelper[Waypoint]
  with AndroServiceFragment with UsesDirectories with SimpleDialogClient {

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
        val f = n.toFloat
        if (f != s.msg.z) {
          s.msg.z = f
          changed()
        }
      }
    }

    /**
     * The menu has just changed our item
     */
    private def changed() {
      for { v <- myVehicle } yield {
        v.self ! DoMarkDirty

        // We don't need this because the model will publish waypointschanged
        // a.notifyDataSetChanged()
        //l.invalidate()
        // setAdapter(v.waypoints)
      }
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
      if (n != getParam(i)) {
        s.setParam(i, n)
        changed()
      }
    }

    /**
     * Have vehicle go to this waypoint
     */
    override def doGoto() {
      for { v <- myVehicle; s <- selected } yield {
        if (v.isDirty)
          v.self ! SendWaypoints // Make sure the vehicle has latest waypoints
        v.self ! DoSetCurrent(s.msg.seq)
        v.self ! DoSetMode("AUTO")
      }
    }

    override def doDelete() {
      for { v <- myVehicle; s <- selected } yield {
        v.self ! DoDeleteWaypoint(s.msg.seq)
      }
    }

    override def typStr = {
      val r = selected.map(_.commandStr).getOrElse("unknown")
      debug("Returning typStr=" + r)
      r
    }

    override def typStr_=(s: String) {
      selected.foreach { w =>
        if (s != w.commandStr) {
          w.commandStr = s
          changed()
        }
      }
    }
  }

  /**
   * The action mode customize for list view
   */
  private lazy val contextMenuCallback = new WaypointActionMode(getActivity) with ActionModeCallback {

    selectedMarker = Some(menuAdapter)

    override def shouldShowMenu = myVehicle.map(_.hasHeartbeat).getOrElse(false)

    // Called when the user exits the action mode
    override def onDestroyActionMode(mode: ActionMode) {
      super.onDestroyActionMode(mode)

      setSelection(None)
    }

    override def onPrepareActionMode(mode: ActionMode, menu: Menu) = {
      val movedown = menu.findItem(R.id.menu_movedown)
      val moveup = menu.findItem(R.id.menu_moveup)
      val showmap = menu.findItem(R.id.menu_showonmap)

      // Allow some extra features when using list view
      val isNav = selected.map { i => i.isNavCommand && i.isValidLatLng }.getOrElse(false)
      showmap.setVisible(isNav)
      //Seq( /* movedown, moveup, */ showmap).foreach(_.setVisible(true))
      moveup.setVisible(!selectedIsFirst)
      movedown.setVisible(!selectedIsLast)

      super.onPrepareActionMode(mode, menu)
    }

    def selectedIsLast = (for {
      v <- myVehicle
      s <- selected
    } yield {
      s.seq >= v.waypoints.size - 1
    }).getOrElse(false)

    /// Are we the first non home waypoint?
    def selectedIsFirst = (for {
      s <- selected
    } yield {
      s.seq <= 1
    }).getOrElse(false)

    private def doMove(dir: Int) {
      val movingDown = dir > 0

      for {
        v <- myVehicle
        s <- selected if !s.isHome && (if (movingDown) !selectedIsLast else !selectedIsFirst)
      } yield {
        // Swap with an adjacent waypoint
        val myindex = s.seq
        val otherindex = if (movingDown) myindex + 1 else myindex - 1
        val wpts = v.waypoints
        val other = wpts(otherindex)

        v.waypoints = wpts.zipWithIndex.map {
          case (w, i) =>
            if (i == myindex) {
              val r = wpts(otherindex)
              r.msg.seq = myindex
              r
            } else if (i == otherindex) {
              val r = wpts(myindex)
              r.msg.seq = otherindex
              r
            } else
              w
        }

        v.self ! DoMarkDirty
      }
    }

    // Called when the user selects a contextual menu item
    override def onActionItemClicked(mode: ActionMode, item: MenuItem) = {
      debug("In list item clicked")
      selected.map { marker =>
        item.getItemId match {

          case R.id.menu_movedown =>
            doMove(1)
            true

          case R.id.menu_moveup =>
            doMove(-1)
            true

          case R.id.menu_showonmap =>
            val activity = context.asInstanceOf[MainActivity]
            val l = marker.location
            debug(s"zooming to $l")
            activity.zoomMapTo(new LatLng(l.lat, l.lon))
            true

          case _ =>
            super.onActionItemClicked(mode, item)
        }
      }.getOrElse(super.onActionItemClicked(mode, item))
    }
  }

  setHasOptionsMenu(true)

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

  override def onVehicleReceive: InstrumentedActor.Receiver = {

    case MsgWaypointsChanged =>
      handler.post(handleWaypoints _)

    case MsgWaypointCurrentChanged(n) =>
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

  def hasKitKat = android.os.Build.VERSION.SDK_INT >= 19

  /**
   * Fires an intent to spin up the "file chooser" UI and select an image.
   */
  def performFileSearch() {

    // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
    // browser.
    // Added in kitkat
    val intent = new Intent("android.intent.action.GET_CONTENT" /* OPEN_DOCUMENT Intent.ACTION_GET_CONTENT */ )

    // Filter to only show results that can be "opened", such as a
    // file (as opposed to a list of contacts or timezones)
    intent.addCategory(Intent.CATEGORY_OPENABLE)

    // Filter to show only images, using the image MIME data type.
    // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
    // To search for all documents available via installed storage providers,
    // it would be "*/*".
    intent.setType("*/*")

    getActivity.startActivityForResult(intent, MainActivity.openWaypointRequestCode)
  }

  private def save() {
    for { dir <- waypointDirectory; vm <- myVehicle } yield {
      val file = FileTools.getDatestampFilename(".wpt", dir)
      info("Saving waypoints to " + file.getAbsolutePath)
      val os = new BufferedOutputStream(new FileOutputStream(file, true), 8192)
      vm.writeToStream(os)
      toast(s"Waypoints saved to $file", true)
    }
  }

  private def canSave = waypointDirectory.isDefined && myVehicle.isDefined

  private def deleteAll() {
    myVehicle.foreach { v =>
      val seqNums = v.waypoints.filterNot(_.isHome).map(_.msg.seq).reverse.toArray
      seqNums.foreach { s =>
        v.self ! DoDeleteWaypoint(s)
      }
    }
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {

      case R.id.menu_load =>
        performFileSearch()
        true

      case R.id.menu_save =>
        save()
        true

      case R.id.menu_deleteall =>
        showYesNo("Delete all waypoints?", deleteAll)
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }

  override def onPrepareOptionsMenu(menu: Menu) = {
    val save = menu.findItem(R.id.menu_save)
    val load = menu.findItem(R.id.menu_load)

    load.setVisible(hasKitKat)
    save.setVisible(canSave)

    super.onPrepareOptionsMenu(menu)
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    debug("Creating option menu")
    inflater.inflate(R.menu.waypoint_menu, menu) // inflate the menu
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
