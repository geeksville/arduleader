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

/**
 * Our customized map fragment
 */
class MyMapFragment extends SupportMapFragment with UsesPreferences with AndroServiceFragment {

  var scene: Option[Scene] = None

  private var waypointMarkers = Seq[DraggableWaypointMarker]()

  def mapOpt = Option(getMap)

  /**
   * If we are going to a GUIDED location
   */
  private var guidedMarker: Option[WaypointMarker] = None
  private var provisionalMarker: Option[ProvisionalMarker] = None

  var planeMarker: Option[VehicleMarker] = None

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
            val imm = getActivity.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
            imm.hideSoftInputFromWindow(v.getWindowToken, 0)

            // We handled the event
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

              if (marker.isAllowGoto)
                goto.setVisible(true)

              marker match {
                case x: GuidedWaypointMarker =>
                  // No context menu yet for guided waypoints
                  mode.finish()

                case x: ProvisionalMarker =>
                  Seq(add, setalt).foreach(_.setVisible(true))
                case x: WaypointMarker =>
                  if (x.draggable) {
                    // Don't allow delete of the current waypoint (for now)
                    if (!x.isCurrent)
                      Seq(delete).foreach(_.setVisible(true))
                    Seq(changetype, setalt).foreach(_.setVisible(true))
                  }
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

          /* Handled all in the edittext box now... 
            case R.id.menu_setalt =>
            marker.doSetAlt()
            mode.finish() // Action picked, so close the CAB
            true
           */

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

    def isAllowGoto = false

    /**
     * Have vehicle go to this waypoint
     */
    def doGoto() { toast("FIXME Goto waypoint not yet implemented") }
    def doAdd() { toast("FIXME Add WP not yet implemented") }
    def doDelete() { toast("FIXME Delete WP not yet implemented") }
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

    override def isAllowGoto = true

    override def icon: Option[BitmapDescriptor] = Some(BitmapDescriptorFactory.fromResource(R.drawable.waypoint))
    override def title = Some("Provisional waypoint")
    override def snippet = Some("Altitude %sm".format(altitude))

    /**
     * Go to our previously placed guide marker
     */
    override def doGoto() {
      for { map <- mapOpt; v <- myVehicle } yield {

        val wp = myVehicle.get.makeGuided(loc)
        v ! DoGotoGuided(wp)

        toast("Guided flight selected")
      }
    }

    override def doAdd() {
      for { map <- mapOpt; v <- myVehicle } yield {
        val wp = v.missionItem(v.waypoints.size, loc)

        // FIXME - we shouldn't be touching this
        v.waypoints = v.waypoints :+ Waypoint(wp)

        v ! SendWaypoints
        handleWaypoints() // Update GUI
        toast("Waypoint added")
      }
    }
  }

  class WaypointMarker(val wp: Waypoint) extends MyMarker with AndroidLogger {

    def lat = wp.msg.x
    def lon = wp.msg.y

    override def isAllowGoto = !isHome // Don't let people 'goto' home because that would probably smack them into the ground.  Really they want RTL

    override def isAltitudeEditable = !isHome
    override def altitude = wp.msg.z
    override def altitude_=(n: Double) {
      if (n != altitude) {
        wp.msg.z = n.toFloat
        setSnippet()
        sendWaypointsAndUpdate()
      }
    }

    override def title = {
      val r = if (isHome)
        "Home"
      else
        "Waypoint #" + wp.msg.seq + " (" + wp.commandStr + ")"

      Some(r)
    }

    override def snippet = {
      import wp.msg._
      val params = Seq(param1, param2, param3, param4)
      val hasParams = params.find(_ != 0.0f).isDefined
      val r = if (hasParams)
        "Alt=%sm %s params=%s".format(z, wp.frameStr, params.mkString(","))
      else
        "Altitude %sm %s".format(z, wp.frameStr)
      Some(r)
    }

    /**
     * Can the user see/change auto continue
     */
    override def isAllowAutocontinue = true
    override def isAutocontinue = wp.msg.autocontinue != 0
    override def isAutocontinue_=(b: Boolean) {
      if (b != isAutocontinue) {
        wp.msg.autocontinue = if (b) 1 else 0
        sendWaypointsAndUpdate()
      }
    }

    override def icon: Option[BitmapDescriptor] = {
      if (isHome) { // Show a house for home
        if (isCurrent)
          Some(BitmapDescriptorFactory.fromResource(R.drawable.lz_red))
        else
          Some(BitmapDescriptorFactory.fromResource(R.drawable.lz_blue))
      } else wp.msg.current match {
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
    def isHome = wp.isHome

    /// If the airplane is heading here
    def isCurrent = wp.isCurrent

    override def toString = title.get

    protected def sendWaypointsAndUpdate() {
      myVehicle.foreach(_ ! SendWaypoints)
      handleWaypoints() // Update GUI
    }
  }

  class GuidedWaypointMarker(wp: Waypoint) extends WaypointMarker(wp) {
  }

  /**
   * A draggable marker that will send movement commands to the vehicle
   */
  class DraggableWaypointMarker(wp: Waypoint) extends WaypointMarker(wp) {

    override def lat_=(n: Double) { wp.msg.x = n.toFloat }
    override def lon_=(n: Double) { wp.msg.y = n.toFloat }

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
          val keepme = w.seq != wp.seq

          // For items after the msg we are deleting, we need to fixup their sequence numbers
          if (w.seq > wp.seq)
            w.msg.seq -= 1

          keepme
        }

        sendWaypointsAndUpdate()
        toast("Waypoint deleted")
      }
    }

    override def doGoto() {
      for { map <- mapOpt; v <- myVehicle } yield {
        v.setCurrent(wp.seq)
        v.setMode("AUTO")
        //toast("Goto " + title)
      }
    }
  }

  class VehicleMarker extends MyMarker with AndroidLogger {

    private var oldWarning = false

    def lat = (for { v <- myVehicle; loc <- v.location } yield { loc.lat }).getOrElse(floatPreference("cur_lat", 0.0f))
    def lon = (for { v <- myVehicle; loc <- v.location } yield { loc.lon }).getOrElse(floatPreference("cur_lon", 0.0f))

    override def icon: Option[BitmapDescriptor] = Some(BitmapDescriptorFactory.fromResource(iconRes))

    private def iconRes = (for { s <- service; v <- myVehicle } yield {
      if (!v.hasHeartbeat || !s.isSerialConnected)
        R.drawable.plane_red
      else if (isWarning)
        R.drawable.plane_yellow
      else
        R.drawable.plane_blue
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

        val batStr = if (isLowVolt) Some("LowVolt!") else None
        val pctStr = if (isLowBatPercent) Some("LowPct!") else None
        val radioStr = if (isLowRssi) Some("LowRssi!") else None
        val gpsStr = if (isLowNumSats) Some("LowSats!") else None

        val r = Seq(locStr, batStr, pctStr, radioStr, gpsStr).flatten.mkString(" ")
        //debug("snippet: " + r)
        r
      }.getOrElse("No service"))

    def isLowVolt = (for { v <- myVehicle; volt <- v.batteryVoltage } yield { volt < minVoltage }).getOrElse(false)

    /// Apparently ardupane treats -1 for pct charge as 'no idea'
    def isLowBatPercent = (for { v <- myVehicle; pct <- v.batteryPercent } yield { pct < minBatPercent }).getOrElse(false)
    def isLowRssi = (for { v <- myVehicle; r <- v.radio } yield { r.rssi < minRssi || r.remrssi < minRssi }).getOrElse(false)
    def isLowNumSats = (for { v <- myVehicle; n <- v.numSats } yield { n < minNumSats }).getOrElse(false)
    def isWarning = isLowVolt || isLowBatPercent || isLowRssi || isLowNumSats

    override def toString = title.get

    /**
     * Something (other than icon) changed about our marker - redraw it
     */
    def update() {
      // Do we need to change icons?
      if (isWarning != oldWarning) {
        oldWarning = isWarning
        redraw()
      }

      setPosition()
      setTitle()
      setSnippet()

      if (isInfoWindowShown)
        showInfoWindow() // Force redraw of existing info window
    }

    /**
     * The icon has changed
     */
    def redraw() {
      setIcon()
      update()
    }
  }

  def minVoltage = floatPreference("min_voltage", 9.5f)
  def minBatPercent = intPreference("min_batpct", 25) / 100.0f
  def minRssi = intPreference("min_rssi", 100)
  def minNumSats = intPreference("min_numsats", 5)
  def isKeepScreenOn = boolPreference("force_screenon", false)

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
    markerOpt.foreach(_.update())
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

  override def onVehicleReceive = {
    case l: Location =>
      // log.debug("Handling location: " + l)
      handler.post { () =>
        //log.debug("GUI set position")

        markerOpt.foreach { marker => marker.setPosition() }

        val mappos = new LatLng(l.lat, l.lon)
        if (!hasLocation) {
          // On first update zoom in to plane
          hasLocation = true
          mapOpt.foreach(_.animateCamera(CameraUpdateFactory.newLatLngZoom(mappos, 16.0f)))
        } else if (boolPreference("follow_plane", true))
          mapOpt.foreach(_.animateCamera(CameraUpdateFactory.newLatLng(mappos)))

        // Store last known position in prefs
        preferences.edit.putFloat("cur_lat", l.lat.toFloat).putFloat("cur_lon", l.lon.toFloat).commit()

        updateMarker()
      }

    case MsgSysStatusChanged =>
      //debug("SysStatus changed")
      handler.post { () =>
        updateMarker()
      }

    case MsgStatusChanged(s) =>
      debug("Status changed: " + s)
      handler.post { () =>
        updateMarker()
      }

    case MsgModeChanged(_) =>
      handler.post { () =>
        myVehicle.foreach { v =>

          // we may need to update the segment lines 
          handleWaypoints()
        }

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
    markerOpt.foreach(_.redraw())
  }

  /// menu choices might have changed)
  def invalidateContextMenu() {
    actionMode.foreach(_.invalidate())
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)

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
      map.getUiSettings.setRotateGesturesEnabled(false)
      map.getUiSettings.setCompassEnabled(true)
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

    // Force the screen on if the user wants that (FIXME this only works if the _map_ is shown) - possibly worth doing somewhere better
    Option(getView).foreach(_.setKeepScreenOn(isKeepScreenOn))
  }

  /**
   * Generate our scene
   */
  def handleWaypoints() {
    myVehicle.foreach { v =>
      val wpts = v.waypoints

      scene.foreach { scene =>

        def createWaypointSegments() {
          // Generate segments going between each pair of waypoints (FIXME, won't work with waypoints that don't have x,y position)
          val pairs = waypointMarkers.zip(waypointMarkers.tail)
          scene.segments ++= pairs.map { p =>
            val color = if (p._1.isAutocontinue)
              Color.GREEN
            else
              Color.GRAY

            Segment(p, color)
          }
        }

        val isAuto = v.currentMode == "AUTO"
        var destMarker: Option[MyMarker] = None

        waypointMarkers.foreach(_.remove())

        scene.clearSegments() // FIXME - shouldn't touch this

        if (!wpts.isEmpty) {
          waypointMarkers = wpts.map { w =>
            val r = new DraggableWaypointMarker(w)
            scene.addMarker(r)

            if (isAuto && r.isCurrent)
              destMarker = Some(r)

            r
          }

          createWaypointSegments()
        }

        // Update any guided marker we might have
        guidedMarker.foreach(_.remove())
        guidedMarker = None
        v.guidedDest.foreach { gwp =>
          val marker = new GuidedWaypointMarker(gwp)
          scene.addMarker(marker)
          guidedMarker = Some(marker)
        }

        // Set 'special' destinations
        v.currentMode match {
          case "AUTO" =>
          // Was set above
          case "GUIDED" =>
            destMarker = guidedMarker
          case "RTL" =>
            destMarker = if (!waypointMarkers.isEmpty) Some(waypointMarkers(0)) else None // Assume we are going to a lat/lon that matches home
          case _ =>
            destMarker = None
        }

        // Create a segment for the path we expect the plane to take
        for { dm <- destMarker; pm <- planeMarker } yield {
          scene.segments += Segment(pm -> dm, Color.RED)
        }

        scene.render()
      }
    }
  }
}