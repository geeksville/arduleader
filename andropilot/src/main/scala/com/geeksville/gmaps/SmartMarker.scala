package com.geeksville.gmaps

import scala.collection.mutable.ListBuffer
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import scala.collection.mutable.HashMap
import android.graphics.Color

/**
 * Allows callbacks to subclasses when markers change or need new data for the gmaps v2 view.
 * Also understands graphs of polylines
 */
abstract class SmartMarker {
  def lat: Double
  def lon: Double
  def title: Option[String] = None
  def snippet: Option[String] = None
  def icon: Option[BitmapDescriptor] = None
  def draggable = false

  /**
   * Someone has just moved our marker
   */
  def onDrag() {}

  /**
   * Someone has just moved our marker
   */
  def onDragEnd() {}

  final def latLng = new LatLng(lat, lon)

  /**
   * Generate options given the current state of this marker
   */
  final def markerOptions: MarkerOptions = {
    var r = (new MarkerOptions).position(latLng).draggable(draggable)

    icon.foreach { t => r = r.icon(t) }
    title.foreach { t => r = r.title(t) }
    snippet.foreach { t => r = r.snippet(t) }
    r
  }
}

/**
 * A line between two SmartMarkers
 */
case class Segment(endpoints: (SmartMarker, SmartMarker), colorOptions: PolylineOptions = (new PolylineOptions).color(Color.GREEN)) {
  var polyline: Option[Polyline] = None

  final def lineOptions = colorOptions.add(endpoints._1.latLng).add(endpoints._2.latLng)
}

