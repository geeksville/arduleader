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

/**
 * Our customized map fragment
 */
class MyMapFragment extends SupportMapFragment with AndropilotPrefs with AndroServiceFragment {

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

  private val contextMenuCallback = new WaypointActionMode {

    override def shouldShowMenu = myVehicle.map(_.hasHeartbeat).getOrElse(false)

    // Called when the user exits the action mode
    override def onDestroyActionMode(mode: ActionMode) {
      super.onDestroyActionMode(mode)

      provisionalMarker.foreach(_.remove()) // User didn't activate our provisional waypoint - remove it from the map
      actionMode = None
    }
  }

  abstract class MyMarker extends SmartMarker with WaypointMenuItem {
    override def onClick() = {
      selectMarker(this)
      super.onClick() // Default will show the info window
    }
  }

  /**
   * Shows as a question mark
   */
  class ProvisionalMarker(latlng: LatLng, private var alt: Double) extends MyMarker {
    def lat = latlng.latitude
    def lon = latlng.longitude

    def loc = Location(lat, lon, Some(altitude))

    override def isAltitudeEditable = true
    override def altitude = alt
    override def altitude_=(n: Double) {
      if (n != altitude) {
        alt = n
        setSnippet()
      }
    }

    override def isAllowAdd = true
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
        val wp = v.missionItem(v.waypointsForMap.size, loc)

        v ! DoAddWaypoint(Waypoint(wp))
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
    override def isAllowChangeType = !isHome
    override def isAllowDelete = !isHome && !isCurrent

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
        //if (isCurrent)
        // Some(BitmapDescriptorFactory.fromResource(R.drawable.lz_red))
        //else
        Some(BitmapDescriptorFactory.fromResource(R.drawable.lz_blue))
      } else wp.msg.current match {
        case 0 =>
          Some(WaypointMarker.toDrawable(wp.msg.command))
        case 1 =>
          Some(WaypointMarker.toDrawable(wp.msg.command))
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
    }
  }

  object WaypointMarker {
    private val wpToBitmap = Map(MAV_CMD.MAV_CMD_NAV_TAKEOFF -> R.drawable.waypoint_takeoff,
      MAV_CMD.MAV_CMD_NAV_WAYPOINT -> R.drawable.waypoint_dot,
      MAV_CMD.MAV_CMD_NAV_LAND -> R.drawable.waypoint_land,
      MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM -> R.drawable.waypoint_forever,
      MAV_CMD.MAV_CMD_NAV_LOITER_TURNS -> R.drawable.waypoint_number,
      MAV_CMD.MAV_CMD_NAV_LOITER_TIME -> R.drawable.waypoint_timed,
      MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH -> R.drawable.waypoint_rtl,
      MAV_CMD.MAV_CMD_NAV_LAND -> R.drawable.waypoint_land,
      MAV_CMD.MAV_CMD_DO_JUMP -> R.drawable.yellow).map {
        case (k, v) =>
          k -> BitmapDescriptorFactory.fromResource(v)
      }

    private val defaultWp = BitmapDescriptorFactory.fromResource(R.drawable.blue)

    def toDrawable(cmd: Int) = wpToBitmap.getOrElse(cmd, defaultWp)
  }

  class GuidedWaypointMarker(wp: Waypoint) extends WaypointMarker(wp) {
    override def isAllowContextMenu = false
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
        v ! DoDeleteWaypoint(wp.seq)

        sendWaypointsAndUpdate()
        toast("Waypoint deleted")
      }
    }

    override def doGoto() {
      for { map <- mapOpt; v <- myVehicle } yield {
        v ! DoSetCurrent(wp.seq)
        v ! DoSetMode("AUTO")
        //toast("Goto " + title)
      }
    }
  }

  class VehicleMarker extends MyMarker with AndroidLogger {

    private var oldWarning = false

    override def isAllowContextMenu = false

    def lat = (for { v <- myVehicle; loc <- v.location } yield { loc.lat }).getOrElse(curLatitude)
    def lon = (for { v <- myVehicle; loc <- v.location } yield { loc.lon }).getOrElse(curLongitude)

    override def icon: Option[BitmapDescriptor] = Some(BitmapDescriptorFactory.fromResource(iconRes))

    private def iconRes = (for { s <- service; v <- myVehicle } yield {
      if (!v.hasHeartbeat || !s.isSerialConnected)
        if (v.isCopter) R.drawable.quad_red else R.drawable.plane_red
      else if (isWarning)
        if (v.isCopter) R.drawable.quad_yellow else R.drawable.plane_yellow
      else if (v.isCopter) R.drawable.quad_blue else R.drawable.plane_blue
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
          "Altitude %.1fm".format(v.toAGL(l))
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
        } else if (followPlane)
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
      //map.getUiSettings.setScrollGesturesEnabled(false)
      map.setOnMapLongClickListener(new OnMapLongClickListener {

        // On click set guided to there
        def onMapLongClick(l: LatLng) {
          makeProvisionalMarker(l)
        }
      })
    }
  }

  private def selectMarker(m: MyMarker) {
    contextMenuCallback.selectedMarker = Some(m)

    // Start up action menu if necessary
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
        val marker = new ProvisionalMarker(l, guideAlt)
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
      val wpts = v.waypointsForMap

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