package com.geeksville.andropilot

import com.geeksville.gmaps.Scene
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.GoogleMap
import com.geeksville.flight._
import com.geeksville.akka._
import com.geeksville.mavlink._
import com.ridemission.scandroid.AndroidLogger
import android.os.Handler
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
import org.mavlink.messages.ardupilotmega.msg_mission_item
import com.geeksville.gmaps.SmartMarker
import android.graphics.Color
import org.mavlink.messages.MAV_CMD
import android.widget.TextView
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo

/**
 * Our customized map fragment
 */
class MyMapFragment extends com.google.android.gms.maps.MapFragment with AndroidLogger
  with UsesPreferences with AndroServiceClient {

  implicit def context = getActivity

  var scene: Option[Scene] = None

  private var waypointMarkers = Seq[DraggableWaypointMarker]()

  def mapOpt = Option(getMap)

  /**
   * If we are going to a GUIDED location
   */
  private var guidedMarker: Option[WaypointMarker] = None
  private var provisionalMarker: Option[ProvisionalMarker] = None

  var planeMarker: Option[VehicleMarker] = None

  /**
   * Does work in the GUIs thread
   */
  var handler: Handler = null

  private var actionMode: Option[ActionMode] = None
  private var selectedMarker: Option[MyMarker] = None

  private val contextMenuCallback = new ActionMode.Callback {

    private var oldSelected: Option[MyMarker] = None

    // Called when the action mode is created; startActionMode() was called
    override def onCreateActionMode(mode: ActionMode, menu: Menu) = {
      // Inflate a menu resource providing context menu items
      val inflater = mode.getMenuInflater()
      inflater.inflate(R.menu.context_menu, menu)

      val editAlt = menu.findItem(R.id.menu_setalt).getActionView.asInstanceOf[TextView]
      editAlt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
      editAlt.setImeOptions(EditorInfo.IME_ACTION_DONE) // Make keyboard go away after editing

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
            true
          } else
            false
        }
      })

      true // Did create a menu
    }

    // Called each time the action mode is shown. Always called after onCreateActionMode, but
    // may be called multiple times if the mode is invalidated.
    override def onPrepareActionMode(mode: ActionMode, menu: Menu) = {
      val changed = oldSelected != selectedMarker
      if (changed) {
        val goto = menu.findItem(R.id.menu_goto)
        val add = menu.findItem(R.id.menu_add)
        val delete = menu.findItem(R.id.menu_delete)
        val setalt = menu.findItem(R.id.menu_setalt)
        val changetype = menu.findItem(R.id.menu_changetype)
        val autocontinue = menu.findItem(R.id.menu_autocontinue)

        // Default to nothing
        Seq(goto, add, delete, setalt, changetype, autocontinue).foreach(_.setVisible(false))

        // We only enable options if we are talking to a real vehicle
        if (myVehicle.map(_.hasHeartbeat).getOrElse(false)) {
          selectedMarker match {
            case None =>
              // Nothing selected - exit context mode 
              mode.finish()

            case Some(marker) =>
              if (marker.isAllowAutocontinue) {
                autocontinue.setVisible(true)
                autocontinue.setChecked(marker.isAutocontinue)
              }

              if (marker.isAltitudeEditable) {
                setalt.setVisible(true)
                val editAlt = menu.findItem(R.id.menu_setalt).getActionView.asInstanceOf[TextView]
                editAlt.setText(marker.altitude.toString)
              }

              marker match {
                case x: GuidedWaypointMarker =>
                  // No context menu yet for guided waypoints
                  mode.finish()

                case x: ProvisionalMarker =>
                  Seq(goto, add, setalt).foreach(_.setVisible(true))
                case x: WaypointMarker =>
                  if (x.draggable) {
                    // Don't allow delete of the current waypoint (for now)
                    if (!x.isCurrent)
                      Seq(delete).foreach(_.setVisible(true))
                    Seq(changetype, setalt).foreach(_.setVisible(true))
                  }
                  Seq(goto).foreach(_.setVisible(true))
                case _ =>
                  // For other marker types - exit context menu mode
                  mode.finish()
              }
          }
          oldSelected = selectedMarker
        }
      }
      changed // Return false if nothing is done
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

          case R.id.menu_setalt =>
            marker.doSetAlt()
            mode.finish() // Action picked, so close the CAB
            true

          case R.id.menu_changetype =>
            marker.doChangeType()
            mode.finish() // Action picked, so close the CAB
            true

          case _ =>
            false
        }
      }.getOrElse(false)

    // Called when the user exits the action mode
    override def onDestroyActionMode(mode: ActionMode) {
      provisionalMarker.foreach(_.remove()) // User didn't activate our provisional waypoint - remove it from the map
      actionMode = None
    }
  }

  abstract class MyMarker extends SmartMarker {
    override def onClick() = {
      selectMarker(this)
      super.onClick() // Default will show the info window
    }

    /**
     * Can the user see/change auto continue
     */
    def isAllowAutocontinue = false
    def isAutocontinue = false
    def isAutocontinue_=(b: Boolean) { throw new Exception("Not implemented") }

    def isAltitudeEditable = false
    def altitude = 0.0
    def altitude_=(n: Double) { throw new Exception("Not implemented") }

    /**
     * Have vehicle go to this waypoint
     */
    def doGoto() { toast("FIXME Goto waypoint not yet implemented") }
    def doAdd() { toast("FIXME Add WP not yet implemented") }
    def doDelete() { toast("FIXME Delete WP not yet implemented") }
    def doSetAlt() { toast("FIXME SetAlt not yet implemented") }
    def doChangeType() { toast("FIXME Add change type not yet implemented") }
  }

  /**
   * Shows as a question mark
   */
  class ProvisionalMarker(latlng: LatLng, private var alt: Double) extends MyMarker {
    def lat = latlng.latitude
    def lon = latlng.longitude

    def loc = Location(lat, lon, altitude)

    override def isAltitudeEditable = true
    override def altitude = alt
    override def altitude_=(n: Double) {
      if (n != altitude) {
        alt = n
        setSnippet()
      }
    }

    override def icon: Option[BitmapDescriptor] = Some(BitmapDescriptorFactory.fromResource(R.drawable.waypoint))
    override def title = Some("Provisional waypoint")
    override def snippet = Some("Altitude %sm".format(altitude))

    /**
     * Go to our previously placed guide marker
     */
    override def doGoto() {
      for { map <- mapOpt; v <- myVehicle } yield {
        guidedMarker.foreach(_.remove())

        val marker = new GuidedWaypointMarker(loc)
        scene.get.addMarker(marker)
        guidedMarker = Some(marker)

        v.gotoGuided(marker.msg)
        toast("Guided flight selected")
      }
    }

    override def doAdd() {
      for { map <- mapOpt; v <- myVehicle } yield {
        val wp = v.missionItem(v.waypoints.size, loc)

        // FIXME - we shouldn't be touching this
        v.waypoints = v.waypoints :+ wp

        v ! SendWaypoints
        handleWaypoints() // Update GUI
        toast("Waypoint added")
      }
    }
  }

  class WaypointMarker(val msg: msg_mission_item) extends MyMarker with AndroidLogger {
    private val commandCodes = Map(
      MAV_CMD.MAV_CMD_NAV_TAKEOFF -> "Takeoff",
      MAV_CMD.MAV_CMD_NAV_WAYPOINT -> "Waypoint", // Navigate to Waypoint
      MAV_CMD.MAV_CMD_NAV_LAND -> "Land", // LAND to Waypoint
      MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM -> "Loiter", // Loiter indefinitely
      MAV_CMD.MAV_CMD_NAV_LOITER_TURNS -> "LoiterN", // Loiter N Times
      MAV_CMD.MAV_CMD_NAV_LOITER_TIME -> "LoiterT",
      MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH -> "RTL")

    def commandStr = commandCodes.get(msg.command).getOrElse("cmd=unknown")

    def lat = msg.x
    def lon = msg.y

    override def isAltitudeEditable = true
    override def altitude = msg.z
    override def altitude_=(n: Double) {
      if (n != altitude) {
        msg.z = n.toFloat
        setSnippet()
        sendWaypointsAndUpdate()
      }
    }

    override def title = Some("Waypoint #" + msg.seq + " (" + commandStr + ")")

    override def snippet = {
      import msg._
      val r = "Alt=%sm params=%s,%s,%s,%s frame=%s".format(z, param1, param2, param3, param4, frame)
      Some(r)
    }

    /**
     * Can the user see/change auto continue
     */
    override def isAllowAutocontinue = true
    override def isAutocontinue = msg.autocontinue != 0
    override def isAutocontinue_=(b: Boolean) {
      if (b != isAutocontinue) {
        msg.autocontinue = if (b) 1 else 0
        sendWaypointsAndUpdate()
      }
    }

    override def icon: Option[BitmapDescriptor] = {
      if (isHome) { // Show a house for home
        if (isCurrent)
          Some(BitmapDescriptorFactory.fromResource(R.drawable.lz_red))
        else
          Some(BitmapDescriptorFactory.fromResource(R.drawable.lz_blue))
      } else msg.current match {
        case 0 =>
          Some(BitmapDescriptorFactory.fromResource(R.drawable.blue))
        case 1 =>
          Some(BitmapDescriptorFactory.fromResource(R.drawable.red))
        case 2 => // Guided
          Some(BitmapDescriptorFactory.fromResource(R.drawable.flag))
        case _ =>
          None // Default
      }
    }

    /// The magic home position
    def isHome = (msg.current != 2) && (msg.seq == 0)

    /// If the airplane is heading here
    def isCurrent = msg.current == 1

    override def toString = title.get

    protected def sendWaypointsAndUpdate() {
      myVehicle.foreach(_ ! SendWaypoints)
      handleWaypoints() // Update GUI
    }
  }

  class GuidedWaypointMarker(loc: Location) extends WaypointMarker(myVehicle.get.makeGuided(loc)) {
  }

  /**
   * A draggable marker that will send movement commands to the vehicle
   */
  class DraggableWaypointMarker(msg: msg_mission_item) extends WaypointMarker(msg) {

    override def lat_=(n: Double) { msg.x = n.toFloat }
    override def lon_=(n: Double) { msg.y = n.toFloat }

    override def draggable = !isHome

    override def onDragEnd() {
      super.onDragEnd()
      debug("Drag ended on " + this)

      myVehicle.foreach { v => v ! SendWaypoints }
    }

    override def doDelete() {
      for { map <- mapOpt; v <- myVehicle } yield {
        // FIXME - we shouldn't be touching this
        v.waypoints = v.waypoints.filter { w =>
          val keepme = w.seq != msg.seq

          // For items after the msg we are deleting, we need to fixup their sequence numbers
          if (w.seq > msg.seq)
            w.seq -= 1

          keepme
        }

        sendWaypointsAndUpdate()
        toast("Waypoint deleted")
      }
    }

    override def doGoto() {
      for { map <- mapOpt; v <- myVehicle } yield {
        v.setCurrent(msg.seq)
        //toast("Goto " + title)
      }
    }
  }

  class VehicleMarker extends MyMarker with AndroidLogger {
    def lat = (for { v <- myVehicle; loc <- v.location } yield { loc.lat }).getOrElse(0.0)
    def lon = (for { v <- myVehicle; loc <- v.location } yield { loc.lon }).getOrElse(0.0)

    override def icon: Option[BitmapDescriptor] = Some(BitmapDescriptorFactory.fromResource(iconRes))

    private def iconRes = (for { s <- service; v <- myVehicle } yield {
      if (v.hasHeartbeat && s.isSerialConnected) R.drawable.plane_blue else R.drawable.plane_red
    }).getOrElse(R.drawable.plane_red)

    override def title = Some((for { s <- service; v <- myVehicle } yield {
      val r = "Mode " + v.currentMode + (if (!s.isSerialConnected) " (No USB)" else (if (v.hasHeartbeat) "" else " (Lost Comms)"))
      //debug("title: " + r)
      r
    }).getOrElse("No service"))

    override def snippet = Some(
      myVehicle.map { v =>
        // Generate a few optional lines of text

        val locStr = v.location.map { l =>
          "Altitude %.1fm".format(l.alt)
        }

        val batStr = v.batteryPercent.map { p => "Battery %sV (%d%%)".format(v.batteryVoltage.get, p * 100 toInt) }

        val r = Seq(v.status, locStr, batStr).flatten.mkString("\n")
        //debug("snippet: " + r)
        r
      }.getOrElse("No service"))

    override def toString = title.get
  }

  override def onServiceConnected(s: AndropilotService) {
    // FIXME - we leave the vehicle marker dangling
    planeMarker.foreach(_.remove())
    planeMarker = None

    // The monitor might already have state - in which case we should just draw what he has
    updateMarker()
    handleWaypoints()
  }

  /// On first position update zoom in on plane
  private var hasLocation = false

  private def updateMarker() {
    markerOpt.foreach { marker =>
      marker.setPosition()
      marker.setTitle()
      marker.setSnippet()

      if (marker.isInfoWindowShown)
        marker.showInfoWindow() // Force redraw
    }
  }

  private def showInfoWindow() {
    updateMarker()
    markerOpt.foreach { marker =>
      marker.showInfoWindow()
    }
  }

  def markerOpt() = {
    if (!planeMarker.isDefined) {
      debug("Creating vehicle marker")
      for { map <- mapOpt; s <- scene } yield {
        val marker = new VehicleMarker
        s.addMarker(marker)
        planeMarker = Some(marker)
      }
    }

    planeMarker
  }

  private def toast(str: String) {
    Toast.makeText(getActivity, str, Toast.LENGTH_LONG).show()
  }

  /**
   * Return true if we were showing the info window
   */
  def removeMarker() = {
    val wasShown = planeMarker.isDefined && markerOpt.map(_.isInfoWindowShown).getOrElse(false)

    planeMarker.foreach(_.remove())
    planeMarker = None // Will be recreated when we need it

    wasShown
  }

  override def onVehicleReceive = {
    case l: Location =>
      // log.debug("Handling location: " + l)
      handler.post { () =>
        //log.debug("GUI set position")

        markerOpt.foreach { marker => marker.setPosition() }
        if (!hasLocation) {
          // On first update zoom in to plane
          hasLocation = true
          val pos = new LatLng(l.lat, l.lon)
          mapOpt.foreach(_.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16.0f)))
        }
        updateMarker()
      }

    case MsgStatusChanged(s) =>
      debug("Status changed: " + s)
      handler.post { () =>
        updateMarker()
      }

    case MsgModeChanged(_) =>
      handler.post { () =>
        updateMarker()
      }

    case MsgHeartbeatLost(_) =>
      //log.debug("heartbeat lost")
      handler.post { () =>
        redrawMarker()
        invalidateContextMenu()
      }

    case MsgHeartbeatFound(_) =>
      debug("heartbeat found")
      handler.post { () =>
        redrawMarker()
        invalidateContextMenu()
      }

    case MsgWaypointsDownloaded(wp) =>
      handler.post(handleWaypoints _)

    case MsgWaypointsChanged =>
      handler.post(handleWaypoints _)
  }

  private def redrawMarker() {
    val wasShown = removeMarker()
    markerOpt() // Recreate in red 
    if (wasShown)
      showInfoWindow()
  }

  /// menu choices might have changed)
  def invalidateContextMenu() {
    actionMode.foreach(_.invalidate())
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)

    debug("Maps onCreate")

    handler = new Handler

    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity) == ConnectionResult.SUCCESS) {
      initMap()
    }
  }

  def initMap() {
    mapOpt.foreach { map =>
      scene = Some(new Scene(map))
      map.setMyLocationEnabled(true)
      map.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
      map.getUiSettings.setTiltGesturesEnabled(false)
      map.setOnMapLongClickListener(new OnMapLongClickListener {

        // On click set guided to there
        def onMapLongClick(l: LatLng) {
          makeProvisionalMarker(l)
        }
      })
    }
  }

  private def selectMarker(m: MyMarker) {
    selectedMarker = Some(m)

    // Stare up action menu if necessary
    actionMode match {
      case Some(am) =>
        invalidateContextMenu() // menu choices might have changed
      case None =>
        // FIXME - temp hack to not raise menu for clicks on plane
        if (!m.isInstanceOf[VehicleMarker]) {
          actionMode = Some(getActivity.startActionMode(contextMenuCallback))
          getView.setSelected(true)
        }
    }
  }

  private def makeProvisionalMarker(l: LatLng) {
    mapOpt.foreach { map =>
      // FIXME Allow altitude choice (by adding altitude to provisional marker)
      myVehicle.foreach { v =>
        removeProvisionalMarker()
        val marker = new ProvisionalMarker(l, intPreference("guide_alt", 100))
        val m = scene.get.addMarker(marker)
        m.showInfoWindow()
        provisionalMarker = Some(marker)
        selectMarker(marker)
      }
    }
  }

  private def removeProvisionalMarker() {
    provisionalMarker.foreach(_.remove())
    provisionalMarker = None
  }

  override def onResume() {
    super.onResume()

    serviceOnResume()
  }

  override def onPause() {
    serviceOnPause()

    super.onPause()
  }

  /**
   * Generate our scene
   */
  def handleWaypoints() {
    myVehicle.foreach { v =>
      val wpts = v.waypoints

      scene.foreach { scene =>
        waypointMarkers.foreach(_.remove())

        if (!wpts.isEmpty) {
          waypointMarkers = wpts.map { w =>
            val r = new DraggableWaypointMarker(w)
            scene.addMarker(r)
            r
          }

          // Generate segments going between each pair of waypoints (FIXME, won't work with waypoints that don't have x,y position)
          val pairs = waypointMarkers.zip(waypointMarkers.tail)
          scene.clearSegments() // FIXME - shouldn't touch this

          scene.segments ++= pairs.map { p =>
            val color = if (p._1.isAutocontinue)
              Color.GREEN
            else
              Color.GRAY

            Segment(p, color)
          }
          scene.render()
        }
      }
    }
  }
}