package com.geeksville.gmaps

import scala.collection.mutable.ListBuffer
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import scala.collection.mutable.HashMap

/**
 * A collection of SmartMarkers and the segments that connect them
 */
class Scene(map: GoogleMap) {
  val segments = ListBuffer[Segment]()
  val markers = HashMap[Marker, SmartMarker]()

  val markerDragListener = new OnMarkerDragListener {
    override def onMarkerDrag(m: Marker) {}
    override def onMarkerDragEnd(m: Marker) {}
    override def onMarkerDragStart(m: Marker) {}
  }

  map.setOnMarkerDragListener(markerDragListener)

  /**
   * Allows callbacks to subclasses when markers change or need new data for the gmaps v2 view.
   * Also understands graphs of polylines
   */
  class SmartMarker {
    /**
     * Generate options given the current state of this marker
     */
    def markerOptions: MarkerOptions = { null }
  }

  /**
   * A line between two SmartMarkers
   */
  case class Segment(endpoints: (SmartMarker, SmartMarker), lineOptions: PolylineOptions) {
    var polyline: Option[Polyline] = None

  }

  def getMarker(m: Marker) = markers(m)

  /**
   * Redraw all markers
   */
  private def renderMarkers() {
  }

  /**
   * Redraw all segments
   */
  private def renderSegments() {

  }

  private def render() {
    map.clear()
    renderMarkers()
    renderSegments()
  }
}

