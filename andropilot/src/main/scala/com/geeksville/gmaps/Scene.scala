package com.geeksville.gmaps

import scala.collection.mutable.ListBuffer
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import scala.collection.mutable.HashMap
import android.graphics.Color
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._

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
      val sm = getMarker(m)
      sm.lat = m.getPosition.latitude
      sm.lon = m.getPosition.longitude
      handleMarkerDrag(sm)
      sm.onDrag()
    }
    override def onMarkerDragEnd(m: Marker) {
      val sm = getMarker(m)
      debug("End drag: " + sm)
      sm.lat = m.getPosition.latitude
      sm.lon = m.getPosition.longitude
      handleMarkerDrag(sm)
      sm.onDragEnd()
    }
    override def onMarkerDragStart(m: Marker) {
      val sm = getMarker(m)
      debug("Start drag: " + sm)
    }
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
   * Move any segments as we are dragged
   */
  private def handleMarkerDrag(sm: SmartMarker) {
    segments.foreach { s =>
      if (s.endpoints._1 == sm || s.endpoints._2 == sm) {
        val points = List(s.endpoints._1, s.endpoints._2).map(_.latLng)
        s.polyline.foreach(_.setPoints(points.asJava))
      }
    }
  }

  /**
   * Redraw all segments
   */
  private def renderSegments() {
    debug("Rendering " + segments.size + " segments")
    segments.foreach { m =>
      m.polyline = Some(map.addPolyline(m.lineOptions))
    }
  }

  def render() {
    //map.clear() // FIXME - don't add this back until we are sure it won't blow away the plane icon
    renderMarkers()
    renderSegments()
  }
}

