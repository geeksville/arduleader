package com.geeksville.andropilot

import android.app.Activity
import _root_.android.os.Bundle
import android.content.Intent
import com.ridemission.scandroid.AndroidLogger
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.maps.MapFragment
import android.widget._
import com.google.android.gms.maps.GoogleMap
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.content.Context
import com.google.android.gms.maps.model._
import android.content.res.Configuration
import com.geeksville.flight.VehicleMonitor
import com.geeksville.flight.Location
import com.geeksville.akka.MockAkka
import com.geeksville.mavlink.MavlinkEventBus
import android.os.Handler
import com.geeksville.util.ThreadTools._

class MainActivity extends Activity with TypedActivity with AndroidLogger with FlurryActivity {

  implicit val context = this

  val ardupilotId = 1 // the sysId of our plane

  var myVehicle: Option[MyVehicleMonitor] = None

  // We don't cache these - so that if we get rotated we pull the correct one
  def mFragment = getFragmentManager.findFragmentById(R.id.map).asInstanceOf[MapFragment]
  def map = Option(mFragment.getMap).get // Could be null if no maps app
  def textView = findView(TR.textview)

  var planeMarker: Option[Marker] = None

  /**
   * Does work in the GUIs thread
   */
  var handler: Handler = null

  class MyVehicleMonitor extends VehicleMonitor {
    def titleStr = "Mode " + currentMode + (if (!hasHeartbeat) " (lost com)" else "")
    def snippet = {
      // Generate a few optional lines of text

      val locStr = location.map { l =>
        "Altitude %.1fm".format(l.alt)
      }

      val batStr = batteryPercent.map { p => "Battery %d%%".format(p * 100 toInt) }

      val r = Seq(status, locStr, batStr).flatten.mkString("\n")
      log.debug("snippet: " + r)
      r
    }

    private def updateInfoWindow() {
      marker.setTitle(titleStr)
      marker.setSnippet(snippet)
      if (marker.isInfoWindowShown)
        marker.showInfoWindow() // Force redraw
    }

    private def showInfoWindow() {
      updateInfoWindow()
      marker.showInfoWindow()
    }

    def marker() = {
      if (!planeMarker.isDefined) {

        val icon = if (hasHeartbeat) R.drawable.plane_blue else R.drawable.plane_red

        log.debug("Creating vehicle marker")
        planeMarker = Some(map.addMarker(new MarkerOptions()
          .position(location.map { l => new LatLng(l.lat, l.lon) }.getOrElse(new LatLng(0, 0)))
          .draggable(false)
          .title(titleStr)
          .snippet(snippet)
          .icon(BitmapDescriptorFactory.fromResource(icon))))
      }

      planeMarker.get
    }

    /**
     * Return true if we were showing the info window
     */
    def removeMarker() = {
      val wasShown = planeMarker.isDefined && marker.isInfoWindowShown

      planeMarker.foreach(_.remove())
      planeMarker = None // Will be recreated when we need it

      wasShown
    }

    override def onLocationChanged(l: Location) {
      log.debug("Handling location: " + l)
      super.onLocationChanged(l)

      handler.post { () =>
        //log.debug("GUI set position")
        marker.setPosition(new LatLng(l.lat, l.lon))
        updateInfoWindow()
      }
    }

    override def onStatusChanged(s: String) {
      log.debug("Status changed: " + s)
      super.onStatusChanged(s)
      handler.post(updateInfoWindow _)
    }

    override def onHeartbeatLost() {
      super.onHeartbeatLost()
      //log.debug("heartbeat lost")
      handler.post(redrawMarker _)
    }

    private def redrawMarker() {
      val wasShown = removeMarker()
      marker() // Recreate in red 
      if (wasShown)
        showInfoWindow()
    }

    override def onHeartbeatFound() {
      super.onHeartbeatFound()
      log.debug("heartbeat found")
      handler.post(redrawMarker _)
    }
  }

  val serviceConnection = new ServiceConnection() {
    def onServiceConnected(className: ComponentName, service: IBinder) {
      debug("Service is bound")

      // Don't use akka until the service is created
      val actor = MockAkka.actorOf(new MyVehicleMonitor, "vmon")
      MavlinkEventBus.subscribe(actor, ardupilotId)
      myVehicle = Some(actor)
    }

    def onServiceDisconnected(className: ComponentName) {
      error("Service disconnected")

      myVehicle = None
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    warn("GooglePlayServices = " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this))

    setContentView(R.layout.main)

    // textView.setText("hello, world!")

    handler = new Handler

    initMap()

    // Did the user just plug something in?
    Option(getIntent) match {
      case Some(intent) =>
        info("Received intent: " + intent)
        if (intent.getAction == "android.hardware.usb.action.USB_DEVICE_ATTACHED") {
          textView.setText("Device connected!  Starting service")
          startService()
        } else
          requestAccess()
      case None =>
        requestAccess()
    }
  }

  /**
   * We handle this ourselves - so as to not try to rebind to the service
   */
  override def onConfigurationChanged(c: Configuration) {
    val hadMarker = planeMarker.isDefined

    setContentView(R.layout.main)

    // Reattach to view widgets
    initMap()
    myVehicle.foreach { v =>
      v.removeMarker() // Force new marker creation for new view

      // If we had a marker before, goahead and remake it
      if (hadMarker)
        v.marker()
    }
  }

  def initMap() {
    map.setMyLocationEnabled(true)
    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
  }

  def startService() {
    debug("Asking to start service")
    val intent = new Intent(this, classOf[AndropilotService])
    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT)
  }

  /** Ask for permission to access our device */
  def requestAccess() {
    AndroidSerial.getDevice match {
      case Some(device) =>
        AndroidSerial.requestAccess(device, { d =>
          textView.setText("Access granted!  Starting service")
          startService()
        }, { d =>
          textView.setText("User denied access to USB device")
        })
      case None =>
        textView.setText("Please attach 3dr telemetry device")
      // startService() // FIXME, remove this
    }

  }
}
