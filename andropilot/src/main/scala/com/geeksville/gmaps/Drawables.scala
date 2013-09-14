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
 * A ducktype that adds a uniform API for maps drawable widgets (Polyline/Circle)
 */
object MapsTypes {
  type Drawable = {
    def remove()
  }
}

trait DrawableFactory {
  protected var drawn: Option[MapsTypes.Drawable] = None

  def remove() {
    drawn.foreach(_.remove())
    drawn = None
  }

  def render(map: GoogleMap)

  /**
   * Move any drawables as we are dragged (if we care)
   */
  def handleMarkerDrag(sm: SmartMarker) {}
}

trait LineFactory extends DrawableFactory {
  def polyline: Option[Polyline] = drawn.asInstanceOf[Option[Polyline]]

  protected def lineOptions: PolylineOptions

  def render(map: GoogleMap) {
    drawn = Option(map.addPolyline(lineOptions))
  }
}

class CircleFactory(val circleOptions: CircleOptions) extends DrawableFactory {
  def circle: Option[Circle] = drawn.asInstanceOf[Option[Circle]]

  def render(map: GoogleMap) {
    drawn = Option(map.addCircle(circleOptions))
  }
}

/**
 * A line between two SmartMarkers
 */
case class Segment(endpoints: (SmartMarker, SmartMarker), color: Int) extends LineFactory {
  final def lineOptions = (new PolylineOptions).color(color).add(endpoints._1.latLng).add(endpoints._2.latLng)

  /**
   * Move any drawables as we are dragged (if we care)
   */
  override def handleMarkerDrag(sm: SmartMarker) {
    if (endpoints._1 == sm || endpoints._2 == sm) {
      val points = List(endpoints._1, endpoints._2).map(_.latLng)
      polyline.foreach(_.setPoints(points.asJava))
    }
  }
}

/**
 * Just a series of poly points
 */
case class PolylineFactory(points: Iterable[LatLng], color: Int) extends LineFactory {
  final def lineOptions = (new PolylineOptions).color(color).addAll(points.asJava)
}
