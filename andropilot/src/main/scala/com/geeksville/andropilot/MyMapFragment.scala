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

/**
 * Our customized map fragment
 */
class MyMapFragment extends com.google.android.gms.maps.MapFragment with AndroidLogger
  with UsesPreferences with AndroServiceClient {

  implicit def context = getActivity

  var scene: Scene = null

  def mapOpt = Option(getMap)

  private var guidedMarker: Option[Marker] = None

  var planeMarker: Option[Marker] = None

  /**
   * Does work in the GUIs thread
   */
  var handler: Handler = null

  override def onServiceConnected(s: AndropilotService) {
    // We're about to recreate waypoint and plane icons, so for now blow away the map
    mapOpt.foreach(_.clear())

    // The monitor might already have state - in which case we should just draw what he has
    updateMarker()
    handleWaypoints()
  }

  /// On first position update zoom in on plane
  private var hasLocation = false

  def titleStr = (for { s <- service; v <- myVehicle } yield {
    "Mode " + v.currentMode + (if (!s.isSerialConnected) " (No USB)" else (if (v.hasHeartbeat) "" else " (Lost Comms)"))
  }).getOrElse("No service")

  def snippet = {
    myVehicle.map { v =>
      // Generate a few optional lines of text

      val locStr = v.location.map { l =>
        "Altitude %.1fm".format(l.alt)
      }

      val batStr = v.batteryPercent.map { p => "Battery %sV (%d%%)".format(v.batteryVoltage.get, p * 100 toInt) }

      val r = Seq(v.status, locStr, batStr).flatten.mkString("\n")
      //log.debug("snippet: " + r)
      r
    }.getOrElse("No service")
  }

  private def updateMarker() {
    markerOpt.foreach { marker =>
      for (v <- myVehicle; l <- v.location) yield {
        val pos = new LatLng(l.lat, l.lon)
        marker.setPosition(pos)
      }
      marker.setTitle(titleStr)
      marker.setSnippet(snippet)
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

      val v = myVehicle.get // FIXME - this is dangerous
      val icon = if (v.hasHeartbeat && service.get.isSerialConnected) R.drawable.plane_blue else R.drawable.plane_red

      debug("Creating vehicle marker")
      mapOpt.foreach { map =>
        planeMarker = Some(map.addMarker(new MarkerOptions()
          .position(v.location.map { l => new LatLng(l.lat, l.lon) }.getOrElse(new LatLng(0, 0)))
          .draggable(false)
          .title(titleStr)
          .snippet(snippet)
          .icon(BitmapDescriptorFactory.fromResource(icon))))
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
        val pos = new LatLng(l.lat, l.lon)
        markerOpt.foreach { marker => marker.setPosition(pos) }
        if (!hasLocation) {
          // On first update zoom in to plane
          hasLocation = true
          mapOpt.foreach(_.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0f)))
        }
        updateMarker()
      }

    case MsgStatusChanged(s) =>
      debug("Status changed: " + s)
      handler.post { () =>
        updateMarker()
      }

    case MsgHeartbeatLost(_) =>
      //log.debug("heartbeat lost")
      handler.post(redrawMarker _)

    case MsgHeartbeatFound(_) =>
      debug("heartbeat found")
      handler.post(redrawMarker _)

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

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)

    debug("Maps onCreate")

    handler = new Handler

    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity) == ConnectionResult.SUCCESS) {
      initMap()
    }
  }

  override def onStart() {
    super.onStart()

  }

  def initMap() {
    mapOpt.foreach { map =>
      scene = new Scene(map)
      map.setMyLocationEnabled(true)
      map.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
      map.getUiSettings.setTiltGesturesEnabled(false)
      map.setOnMapLongClickListener(new OnMapLongClickListener {

        // On click set guided to there
        def onMapLongClick(l: LatLng) {
          // FIXME show a menu instead & don't loose the icon if we get misled
          val alt = intPreference("guide_alt", 100)
          val loc = Location(l.latitude, l.longitude, alt)
          myVehicle.foreach { v =>
            val wp = v.setGuided(loc)
            val marker = new WaypointMarker(wp)
            guidedMarker.foreach(_.remove())
            guidedMarker = Some(map.addMarker(marker.markerOptions)) // For now we just plop it into the map
            toast("Guided flight selected (alt %dm AGL)".format(alt))
          }
        }
      })
    }
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

      // Crufty - shouldn't touch this
      scene.markers.clear()

      if (!wpts.isEmpty) {
        scene.markers ++= wpts.map { w => new DraggableWaypointMarker(v, w) }

        // Generate segments going between each pair of waypoints (FIXME, won't work with waypoints that don't have x,y position)
        val pairs = scene.markers.zip(scene.markers.tail)
        scene.segments.clear()
        scene.segments ++= pairs.map(p => Segment(p))
        scene.render()
      }
    }
  }
}