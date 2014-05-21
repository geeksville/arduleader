package com.geeksville.gmaps

import scala.collection.mutable.ListBuffer
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import scala.collection.mutable.HashMap
import android.graphics.Color
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import scala.collection.mutable.HashSet

/**
 * A collection of SmartMarkers and the drawables that connect them
 */
class Scene(val map: GoogleMap) extends AndroidLogger {
  val drawables = ListBuffer[DrawableFactory]()
  private val markers = HashSet[SmartMarker]()

  /**
   * Used to find our smart marker based on a google marker handle
   */
  private val markerMap = HashMap[Marker, SmartMarker]()

  val markerClickListener = new OnMarkerClickListener {
    override def onMarkerClick(m: Marker) = getMarker(m).onClick()
  }

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
  map.setOnMarkerClickListener(markerClickListener)

  def getMarker(m: Marker) = markerMap(m)

  def addMarker(sm: SmartMarker) = synchronized {
    val gm = map.addMarker(sm.markerOptions)
    sm.gmarker = Some(gm)
    sm.myScene = Some(this)
    markerMap(gm) = sm
    markers.add(sm)
    gm
  }

  private[gmaps] def removeMarker(sm: SmartMarker) {
    sm.gmarker.get.remove()
    markers.remove(sm)
    markerMap.remove(sm.gmarker.get)
    sm.myScene = None
    sm.gmarker = None
  }

  /**
   * Move any drawables as we are dragged
   */
  private def handleMarkerDrag(sm: SmartMarker) {
    drawables.foreach(_.handleMarkerDrag(sm))
  }

  /**
   * Redraw all drawables
   */
  private def renderSegments() = synchronized {
    debug("Rendering " + drawables.size + " drawables")
    drawables.foreach(_.render(map))
  }

  def clearSegments() = synchronized {
    drawables.foreach(_.remove())
    drawables.clear()
  }

  def clearMarkers() = synchronized {
    markers.foreach(_.remove())
    markers.clear()
  }

  def render() {
    //map.clear() // FIXME - don't add this back until we are sure it won't blow away the plane icon
    //renderMarkers()
    renderSegments()
  }

  def close() {
    clearSegments()
    clearMarkers()
  }
}

