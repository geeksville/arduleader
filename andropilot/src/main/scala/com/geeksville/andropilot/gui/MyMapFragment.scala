package com.geeksville.andropilot.gui

import com.geeksville.gmaps.Scene
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.geeksville.flight._
import com.geeksville.akka._
import com.geeksville.mavlink._
import com.ridemission.scandroid._
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
import com.geeksville.gmaps.PolylineFactory
import org.mavlink.messages.FENCE_ACTION
import com.geeksville.flight.MsgWaypointCurrentChanged
import com.geeksville.gmaps.CircleFactory
import android.location.LocationListener
import android.location.LocationManager
import org.mavlink.messages.ardupilotmega.msg_radio
import com.geeksville.util.Throttled
import com.geeksville.flight.StatusText

/**
 * Our customized map fragment
 */
class MyMapFragment extends SupportMapFragment
  with AndropilotPrefs with AndroServiceFragment with UsesResources {

  var scene: Option[Scene] = None

  private var waypointMarkers = Seq[DraggableWaypointMarker]()

  def mapOpt = Option(getMap)

  private val statusThrottle = new Throttled(15000)

  /**
   * If we are going to a GUIDED location
   */
  private var guidedMarker: Option[WaypointMarker] = None
  private var provisionalMarker: Option[ProvisionalMarker] = None
  private var fenceMarker: Option[MyMarker] = None

  var planeMarker: Option[VehicleMarker] = None

  private var actionMode: Option[ActionMode] = None

  private lazy val contextMenuCallback = new WaypointActionMode(getActivity) with ActionModeCallback {

    override def shouldShowMenu = myVehicle.map(_.hasHeartbeat).getOrElse(false)

    // Called when the user exits the action mode
    override def onDestroyActionMode(mode: ActionMode) {
      super.onDestroyActionMode(mode)

      provisionalMarker.foreach(_.remove()) // User didn't activate our provisional waypoint - remove it from the map
    }
  }

  abstract class MyMarker extends SmartMarker with WaypointMenuItem {
    override def onClick() = {
      selectMarker(this)
      super.onClick() // Default will show the info window
    }
  }

  /**
   * FIXME, support dragging to change fence return position
   */
  class FenceReturnMarker(l: Location) extends MyMarker {
    def lat = l.lat
    def lon = l.lon

    override def icon: Option[BitmapDescriptor] = Some(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
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
    override def title = Some(S(R.string.provisional_waypoint))
    override def snippet = Some(S(R.string.altitude_m).format(altitude))

    /**
     * Go to our previously placed guide marker
     */
    override def doGoto() {
      for { map <- mapOpt; v <- myVehicle } yield {

        val wp = myVehicle.get.makeGuided(loc)
        v ! DoGotoGuided(wp)

        toast(S(R.string.guided_meters).format(loc.alt.get))
      }
    }

    override def doAdd() {
      for { map <- mapOpt; v <- myVehicle } yield {
        val wp = v.missionItem(v.waypointsForMap.size, loc)

        v ! DoAddWaypoint(Waypoint(wp))
        // toast("Waypoint added")
      }
    }
  }

  class WaypointMarker(val wp: Waypoint) extends MyMarker with AndroidLogger {

    def lat = wp.msg.x
    def lon = wp.msg.y

    override def isAllowChangeType = !isHome

    override def isAltitudeEditable = !isHome
    override def altitude = wp.msg.z
    override def altitude_=(n: Double) {
      if (n != altitude) {
        val f = n.toFloat
        if (f != wp.msg.z) {
          wp.msg.z = f
          setSnippet()
          sendWaypointsAndUpdate()
        }
      }
    }
    override def numParams = wp.numParamsUsed
    override def getParam(i: Int) = wp.getParam(i)
    override def setParam(i: Int, n: Float) = {
      if (getParam(i) != n) {
        wp.setParam(i, n)
        setSnippet()
        sendWaypointsAndUpdate()
      }
    }

    override def title = {
      val r = if (isHome)
        S(R.string.home)
      else
        S(R.string.wp_num_label).format(wp.msg.seq, wp.commandStr)

      Some(r)
    }

    override def snippet = {
      import wp.msg._
      val params = Seq(param1, param2, param3, param4)
      val hasParams = params.find(_ != 0.0f).isDefined
      val r = if (hasParams)
        S(R.string.wp_parms).format(z, wp.frameStr, params.mkString(","))
      else
        S(R.string.wp_alt).format(z, wp.frameStr)
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
          Some(WaypointMarker.toBitmap(wp.msg.command))
        case 1 =>
          Some(WaypointMarker.toBitmap(wp.msg.command))
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
      myVehicle.foreach(_ ! DoMarkDirty) // Will implicitly cause an update
    }
  }

  object WaypointMarker {

    private val wpToBitmap = WaypointUtil.wpToDrawable.map {
      case (k, v) =>
        k -> BitmapDescriptorFactory.fromResource(v)
    }

    private val defaultBitmap = BitmapDescriptorFactory.fromResource(WaypointUtil.defaultDrawable)

    /// Get a bitmap suitable for use by gmaps
    def toBitmap(cmd: Int) = wpToBitmap.getOrElse(cmd, defaultBitmap)
  }

  class GuidedWaypointMarker(wp: Waypoint) extends WaypointMarker(wp) {
    override def isAllowContextMenu = false
  }

  /**
   * A draggable marker that will send movement commands to the vehicle
   */
  class DraggableWaypointMarker(wp: Waypoint) extends WaypointMarker(wp) {

    override def isAllowGoto = !isHome // Don't let people 'goto' home because that would probably smack them into the ground.  Really they want RTL

    override def lat_=(n: Double) { wp.msg.x = n.toFloat }
    override def lon_=(n: Double) { wp.msg.y = n.toFloat }

    override def draggable = !isHome
    override def isAllowDelete = !isHome && !isCurrent

    override def onDragEnd() {
      super.onDragEnd()
      debug("Drag ended on " + this)

      sendWaypointsAndUpdate()
    }

    override def doDelete() {
      for { v <- myVehicle } yield {
        v ! DoDeleteWaypoint(wp.seq)

        sendWaypointsAndUpdate()
        // toast(R.string.waypoint_deleted, true)
      }
    }

    override def doGoto() {
      for { map <- mapOpt; v <- myVehicle } yield {
        v ! DoSetCurrent(wp.seq)
        v ! DoSetMode("AUTO")
        //toast("Goto " + title)
      }
    }

    override def typStr = wp.commandStr
    override def typStr_=(s: String) {
      if (s != wp.commandStr) {
        wp.commandStr = s

        sendWaypointsAndUpdate()
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
      //debug(s"in iconres ${v.hasHeartbeat}, ${s.isConnected}")
      if (!v.hasHeartbeat || !s.isConnected)
        if (v.isCopter) R.drawable.quad_red else R.drawable.plane_red
      else if (isWarning)
        if (v.isCopter) R.drawable.quad_yellow else R.drawable.plane_yellow
      else if (v.isCopter) R.drawable.quad_blue else R.drawable.plane_blue
    }).getOrElse(R.drawable.plane_red)

    override def title = Some((for { s <- service; v <- myVehicle } yield {
      val r = S(R.string.mode) + " " + v.currentMode + (if (!s.isConnected)
        " (" + S(R.string.no_link) + ")"
      else (if (v.hasHeartbeat)
        ""
      else
        " (" + S(R.string.lost_comms) + ")"))
      //debug("title: " + r)
      r
    }).getOrElse("No service"))

    override def snippet = Some(
      myVehicle.map { v =>
        // Generate a few optional lines of text

        val locStr = v.location.map { l =>
          "Altitude %.1fm".format(v.toAGL(l))
        }

        val batStr = if (isLowVolt) Some(S(R.string.low_volt)) else None
        val pctStr = if (isLowBatPercent) Some(S(R.string.low_charge)) else None
        val radioStr = if (isLowRssi) Some(S(R.string.low_rssi)) else None
        val gpsStr = if (isLowNumSats) Some(S(R.string.low_sats)) else None

        val r = Seq(locStr, batStr, pctStr, radioStr, gpsStr).flatten.mkString(" ")
        //debug("snippet: " + r)
        r
      }.getOrElse(S(R.string.no_service)))

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

      statusThrottle { () =>
        handler.post { () =>
          // radio range might have changed
          handleWaypoints()

          updateMarker()
        }
      }

    case StatusText(s, _) =>
      debug("Status changed: " + s)
      handler.post { () =>
        updateMarker()
      }

    case MsgModeChanged(_) =>
      handler.post { () =>
        // we may need to update the segment lines 
        handleWaypoints()

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

    case MsgFenceChanged =>
      handler.post(handleWaypoints _)

    case MsgWaypointCurrentChanged(n) =>
      handler.post(handleWaypoints _)

    // geofence options might have changed - so redraw
    case MsgParametersDownloaded =>
      handler.post(handleWaypoints _)

    // geofence options might have changed - so redraw
    case MsgParameterReceived(index) =>
      handler.post(handleWaypoints _)
  }

  private def redrawMarker() {
    markerOpt.foreach(_.redraw())
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)

    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity) == ConnectionResult.SUCCESS) {
      initMap()
    }
  }

  override def onDestroy() {

    // Our scene is attached to the gmap (which might live on past our view - make sure to remove any left-overs from it)
    scene.foreach(_.close())
    scene = None

    super.onDestroy()
  }

  def initMap() {
    mapOpt.foreach { map =>
      map.clear() // We need to be the exclusive owner of all polylines/markers on the map

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

    invalidateContextMenu() // menu choices might have changed

    // Start up action menu if necessary
    if (!actionMode.isDefined) {
      // FIXME - temp hack to not raise menu for clicks on plane
      if (!m.isInstanceOf[VehicleMarker]) {
        startActionMode(contextMenuCallback)
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

  /// For testing
  val fakeRadio = Some(new msg_radio(1, 1) {
    rssi = 130
    remrssi = 130
    noise = 10
    remnoise = 10
  })

  /**
   * Generate our scene
   */
  def handleWaypoints() {
    myVehicle.foreach { v =>
      val wpts = v.waypointsForMap

      scene.foreach { scene =>

        def createWaypointSegments() {
          // Generate drawables going between each pair of waypoints (FIXME, won't work with waypoints that don't have x,y position)
          val pairs = waypointMarkers.zip(waypointMarkers.tail)
          scene.drawables.appendAll(pairs.map { p =>
            val color = if (p._1.isAutocontinue)
              Color.GREEN
            else
              Color.GRAY

            Segment(p, color)
          })
        }

        def createFenceSegments() = {
          val fenceRadius = v.fenceRadius
          val homeOpt = v.home
          if (v.fenceIsCircle && v.isFenceEnable && fenceRadius.isDefined && homeOpt.isDefined) {

            // An arducopter style fence

            val color = Color.YELLOW
            val home = homeOpt.get.location
            val center = new LatLng(home.lat, home.lon)
            val circle = new CircleFactory((new CircleOptions).center(center).strokeColor(color).strokeWidth(8).radius(fenceRadius.get))
            scene.drawables.append(circle)
          } else {

            // A plane style fence

            val points = v.fenceBoundary.map { p =>
              new LatLng(p.lat, p.lon)
            }

            val color = if (v.fenceAction != FENCE_ACTION.FENCE_ACTION_NONE)
              Color.YELLOW
            else
              Color.GRAY

            val line = PolylineFactory(points, color)
            scene.drawables.append(line)
          }
        }

        def createRangeCircles() = {
          for {
            r <- v.radio // fakeRadio 
            vLoc <- v.location
            map <- mapOpt
            gcsAndroidLoc <- Option(map.getMyLocation)
          } yield {
            val gcsLoc = Location(gcsAndroidLoc.getLatitude, gcsAndroidLoc.getLongitude)
            val curDist = vLoc.distance(gcsLoc).toFloat
            val (localRange, remRange) = RadioTools.estimateRangePair(r, curDist)
            val maxRangeMeters = 50 * 1000 // 50km limit - If we have bogus location for one of the nodes we can get _really_ huge ranges reported

            if (localRange > 0 && localRange < maxRangeMeters) {
              val gcsLocLatLng = new LatLng(gcsLoc.lat, gcsLoc.lon)
              scene.drawables.append(new CircleFactory((new CircleOptions).center(gcsLocLatLng).
                strokeColor(Color.BLUE).strokeWidth(5).radius(localRange)))
            }

            if (remRange > 0 && localRange < maxRangeMeters) {
              val vLocLatLng = new LatLng(vLoc.lat, vLoc.lon)
              scene.drawables.append(new CircleFactory((new CircleOptions).center(vLocLatLng).
                strokeColor(Color.BLUE).strokeWidth(5).radius(remRange)))
            }
          }
        }

        val isAuto = v.currentMode == "AUTO"
        var destMarker: Option[MyMarker] = None

        debug("Handling new waypoints")
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

        createRangeCircles()

        createFenceSegments()

        fenceMarker.foreach(_.remove())
        fenceMarker = v.fenceReturnPoint.map { p =>
          val m = new FenceReturnMarker(p)
          scene.addMarker(m)
          m
        }

        // Update any guided marker we might have
        guidedMarker.foreach(_.remove())
        guidedMarker = v.guidedDest.map { gwp =>
          val marker = new GuidedWaypointMarker(gwp)
          scene.addMarker(marker)
          marker
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
          scene.drawables.append(Segment(pm -> dm, Color.RED))
        }

        scene.render()
      }
    }
  }
}