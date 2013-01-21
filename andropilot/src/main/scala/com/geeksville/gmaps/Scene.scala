package com.geeksville.gmaps

import scala.collection.mutable.ListBuffer
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import scala.collection.mutable.HashMap
import android.graphics.Color
import com.ridemission.scandroid.AndroidLogger

/**
 * A collection of SmartMarkers and the segments that connect them
 */
class Scene(val map: GoogleMap) extends AndroidLogger {
  val segments = ListBuffer[Segment]()
  val markers = ListBuffer[SmartMarker]()

  /**
   * Used to find our smart marker based on a google marker handle
   */
  private var markerMap = Map[Marker, SmartMarker]()

  val markerDragListener = new OnMarkerDragListener {
    override def onMarkerDrag(m: Marker) {
      getMarker(m).onDrag()
    }
    override def onMarkerDragEnd(m: Marker) {
      getMarker(m).onDragEnd()
    }
    override def onMarkerDragStart(m: Marker) {}
  }

  map.setOnMarkerDragListener(markerDragListener)

  def getMarker(m: Marker) = markerMap(m)

  /**
   * Redraw all markers
   */
  private def renderMarkers() {
    debug("Rendering " + markers.size + " markers")
    markerMap = Map(markers.map { m =>
      map.addMarker(m.markerOptions) -> m
    }: _*)
  }

  /**
   * Redraw all segments
   */
  private def renderSegments() {
    debug("Rendering " + segments.size + " segments")
    segments.foreach { m => map.addPolyline(m.lineOptions) }
  }

  def render() {
    map.clear()
    renderMarkers()
    renderSegments()
  }
}

