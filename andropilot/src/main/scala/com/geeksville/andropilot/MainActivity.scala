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
import scala.language.postfixOps
import android.hardware.usb.UsbManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import com.geeksville.util.Throttled
import com.google.android.gms.maps.CameraUpdateFactory

class MainActivity extends Activity with TypedActivity with AndroidLogger with FlurryActivity {

  implicit val context = this

  val ardupilotId = 1 // the sysId of our plane

  private var myVehicle: Option[MyVehicleMonitor] = None
  private var service: Option[AndropilotService] = None

  /**
   * If an intent arrives before our service is up, squirell it away until we can handle it
   */
  private var waitingForService: Option[Intent] = None

  // We don't cache these - so that if we get rotated we pull the correct one
  def mFragment = getFragmentManager.findFragmentById(R.id.map).asInstanceOf[MapFragment]
  def map = Option(mFragment.getMap).get // Could be null if no maps app
  def textView = findView(TR.textview)

  var planeMarker: Option[Marker] = None

  /**
   * Does work in the GUIs thread
   */
  var handler: Handler = null

  /**
   * We install this receiver only once we're connected to a device
   */
  val disconnectReceiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      if (intent.getAction == UsbManager.ACTION_USB_DEVICE_DETACHED)
        serialDetached()
    }
  }

  /**
   * Used to eavesdrop on location/status changes for our vehicle
   */
  class MyVehicleMonitor extends VehicleMonitor {
    // We can receive _many_ position updates.  Limit to one update per second (to keep from flooding the gui thread)
    private val throttle = new Throttled(1000)

    /// On first position update zoom in on plane
    private var hasLocation = false

    def titleStr = "Mode " + currentMode + (if (!service.get.isSerialConnected) " (No USB)" else (if (hasHeartbeat) "" else " (Lost Comms)"))
    def snippet = {
      // Generate a few optional lines of text

      val locStr = location.map { l =>
        "Altitude %.1fm".format(l.alt)
      }

      val batStr = batteryPercent.map { p => "Battery %d%%".format(p * 100 toInt) }

      val r = Seq(status, locStr, batStr).flatten.mkString("\n")
      //log.debug("snippet: " + r)
      r
    }

    private def updateMarker() {
      location.foreach { l => marker.setPosition(new LatLng(l.lat, l.lon)) }
      marker.setTitle(titleStr)
      marker.setSnippet(snippet)
      if (marker.isInfoWindowShown)
        marker.showInfoWindow() // Force redraw
    }

    private def showInfoWindow() {
      updateMarker()
      marker.showInfoWindow()
    }

    def marker() = {
      if (!planeMarker.isDefined) {

        val icon = if (hasHeartbeat && service.get.isSerialConnected) R.drawable.plane_blue else R.drawable.plane_red

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
      super.onLocationChanged(l)

      throttle {
        // log.debug("Handling location: " + l)
        handler.post { () =>
          //log.debug("GUI set position")
          val pos = new LatLng(l.lat, l.lon)
          marker.setPosition(pos)
          if (!hasLocation) {
            // On first update zoom in to plane
            hasLocation = true
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12.0f))
          }
          updateMarker()
        }
      }
    }

    override def onStatusChanged(s: String) {

      super.onStatusChanged(s)
      throttle {
        log.debug("Status changed: " + s)
        handler.post(updateMarker _)
      }
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
    def onServiceConnected(className: ComponentName, serviceIn: IBinder) {
      val s = serviceIn.asInstanceOf[ServiceAPI].service
      service = Some(s)

      debug("Service is bound")

      // Don't use akka until the service is created
      val actor = MockAkka.actorOf(new MyVehicleMonitor, "vmon")
      MavlinkEventBus.subscribe(actor, ardupilotId)
      myVehicle = Some(actor)

      val logmsg = s.logfile.map { f => "Logging to " + f }.getOrElse("No sdcard, logging suppressed...")
      toast(logmsg)

      // Ask for any already connected serial devices
      requestAccess()

      waitingForService.foreach { intent =>
        handleIntent(intent)
        waitingForService = None
      }
    }

    def onServiceDisconnected(className: ComponentName) {
      error("Service disconnected")

      myVehicle = None
      service = None
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    warn("GooglePlayServices = " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this))

    setContentView(R.layout.main)

    // textView.setText("hello, world!")

    handler = new Handler

    initMap()

    startService()

    // Did the user just plug something in?
    Option(getIntent).foreach(handleIntent)
  }

  /**
   * We are a singleTop app, so when other intents arrive we will not start a new instance, rather handle them here
   */
  override def onNewIntent(i: Intent) {
    handleIntent(i)
  }

  private def toast(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_LONG).show()
  }

  private def serialDetached() {
    debug("Handling serial disconnect")
    unregisterReceiver(disconnectReceiver)
    toast("3DR Telemetry disconnected...")
    service.get.serialDetached()
  }

  private def handleIntent(intent: Intent) {
    debug("Received intent: " + intent)
    service.map { s =>
      intent.getAction match {
        case UsbManager.ACTION_USB_DEVICE_ATTACHED =>
          if (AndroidSerial.getDevice.isDefined && !s.isSerialConnected) {
            // Find out when the device goes away
            registerReceiver(disconnectReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))

            toast("3DR Telemetry connected...")
            s.serialAttached()
          } else
            warn("Ignoring attach for some other device")

        case x @ _ =>
          error("Ignoring unknown intent: " + intent)
      }
    }.getOrElse {
      // No service yet, store the intent until we can do something about it
      waitingForService = Some(intent)
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
    map.getUiSettings.setTiltGesturesEnabled(false)
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
          // requestAccess is not called until the service is up, so we can safely access this
          // If we are already talking to the serial device ignore this
          if (!service.get.isSerialConnected) {
            toast("3DR Telemetry allowed...")
            service.get.serialAttached()
          }
        }, { d =>
          toast("User denied access to USB device")
        })
      case None =>
        toast("Please attach 3dr telemetry device")
      // startService() // FIXME, remove this
    }

  }
}
