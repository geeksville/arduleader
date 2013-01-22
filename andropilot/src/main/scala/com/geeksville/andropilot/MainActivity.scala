package com.geeksville.andropilot

import android.app.Activity
import _root_.android.os.Bundle
import android.content.Intent
import com.ridemission.scandroid.AndroidLogger
import com.ridemission.scandroid.AndroidUtil._
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
import android.view.View
import com.geeksville.akka.PoisonPill
import android.view.Menu
import android.widget.AdapterView.OnItemSelectedListener
import com.geeksville.gmaps.Scene
import org.mavlink.messages.ardupilotmega.msg_mission_item
import com.geeksville.gmaps.Segment
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener

class MainActivity extends Activity with TypedActivity with AndroidLogger with FlurryActivity {

  implicit val context = this

  val ardupilotId = 1 // the sysId of our plane

  private var myVehicle: Option[MyVehicleMonitor] = None
  private var service: Option[AndropilotService] = None

  private var mainView: View = null
  private var modeSpinner: Option[Spinner] = None

  /**
   * If an intent arrives before our service is up, squirell it away until we can handle it
   */
  private var waitingForService: Option[Intent] = None

  private var watchingSerial = false
  private var accessGrantReceiver: Option[BroadcastReceiver] = None

  // We don't cache these - so that if we get rotated we pull the correct one
  def mFragment = getFragmentManager.findFragmentById(R.id.map).asInstanceOf[MapFragment]
  def map = Option(mFragment.getMap).get // Could be null if no maps app
  var scene: Scene = null

  var planeMarker: Option[Marker] = None

  /**
   * Does work in the GUIs thread
   */
  var handler: Handler = null

  /**
   * We install this receiver only once we're connected to a device -
   * only used to show a Toast about disconnection...
   */
  val disconnectReceiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      if (intent.getAction == UsbManager.ACTION_USB_DEVICE_DETACHED)
        serialDetached()
    }
  }

  /**
   * Used to eavesdrop on location/state changes for our vehicle
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
      setModeSpinner() // FIXME, do this someplace better

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

    override def onWaypointsDownloaded() {
      handler.post { () =>
        handleWaypoints(waypoints)
      }
      super.onWaypointsDownloaded()
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

      // If we already had a serial port open start watching it
      registerSerialReceiver()

      // Ask for any already connected serial devices
      requestAccess()

      waitingForService.foreach { intent =>
        handleIntent(intent)
        waitingForService = None
      }
    }

    def onServiceDisconnected(className: ComponentName) {
      error("Service disconnected")

      // No service anymore - don't need my actor
      stopVehicleMonitor()
      service = None
    }
  }

  def stopVehicleMonitor() {
    myVehicle.foreach { v =>
      v ! PoisonPill
      myVehicle = None
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    warn("GooglePlayServices = " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this))

    mainView = getLayoutInflater.inflate(R.layout.main, null)
    setContentView(mainView)

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

  override def onResume() {
    super.onResume()

    debug("Binding to service")
    val intent = new Intent(this, classOf[AndropilotService])
    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT)
  }

  override def onPause() {
    stopVehicleMonitor()

    accessGrantReceiver.foreach { r =>
      unregisterReceiver(r)
      accessGrantReceiver = None
    }

    unregisterSerialReceiver()
    unbindService(serviceConnection)
    super.onPause()
  }

  override def onStop() {
    super.onStop()
  }

  private def toast(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_LONG).show()
  }

  private def serialDetached() {
    debug("Handling serial disconnect")
    unregisterSerialReceiver()

    toast("3DR Telemetry disconnected...")
  }

  private def unregisterSerialReceiver() {
    if (watchingSerial) {
      unregisterReceiver(disconnectReceiver)
      watchingSerial = false
    }
  }

  private def registerSerialReceiver() {
    service.foreach { s =>
      if (!watchingSerial && s.isSerialConnected) {
        // Find out when the device goes away
        registerReceiver(disconnectReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))
        watchingSerial = true
      }
    }
  }

  private def handleIntent(intent: Intent) {
    debug("Received intent: " + intent)
    service.map { s =>
      intent.getAction match {
        case UsbManager.ACTION_USB_DEVICE_ATTACHED =>
          if (AndroidSerial.getDevice.isDefined && !s.isSerialConnected) {

            toast("3DR Telemetry connected...")
            s.serialAttached()
            registerSerialReceiver()
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
   * Generate our scene
   */
  def handleWaypoints(wpts: Seq[msg_mission_item]) {
    // Crufty - shouldn't touch this
    scene.markers.clear()
    scene.markers ++= wpts.map { w => new WaypointMarker(w) }

    // Generate segments going between each pair of waypoints (FIXME, won't work with waypoints that don't have x,y position)
    val pairs = scene.markers.zip(scene.markers.tail)
    scene.segments.clear()
    scene.segments ++= pairs.map(p => Segment(p))
    scene.render()
  }

  /**
   * Update our mode display
   */
  def setModeSpinner() {
    modeSpinner.foreach { s =>
      // Crufty way of finding which element of spinner needs selecting
      def findIndex(str: String) = {
        val adapter = s.getAdapter

        (0 until adapter.getCount).find { i =>
          val is = adapter.getItem(i).toString
          is == str || is == "unknown"
        }.get
      }
      val n = findIndex(myVehicle.get.currentMode)
      //debug("Setting mode spinner to: " + n)

      s.setSelection(n)
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.action_bar, menu) // inflate the menu
    val s = menu.findItem(R.id.menu_mode).getActionView().asInstanceOf[Spinner] // find the spinner
    modeSpinner = Some(s)
    val spinnerAdapter = ArrayAdapter.createFromResource(getActionBar.getThemedContext, R.array.mode_names, android.R.layout.simple_spinner_dropdown_item); //  create the adapter from a StringArray
    s.setAdapter(spinnerAdapter); // set the adapter
    setModeSpinner()

    def modeListener(parent: Spinner, selected: View, pos: Int, id: Long) {
      val modeName = spinnerAdapter.getItem(pos)
      debug("Mode selected: " + modeName)
      if (modeName != "unknown" && modeName != myVehicle.get.currentMode)
        myVehicle.get.setMode(modeName.toString)
    }
    s.onItemSelected(modeListener) // (optional) reference to a OnItemSelectedListener, that you can use to perform actions based on user selection

    true
  }

  def initMap() {
    scene = new Scene(map)
    map.setMyLocationEnabled(true)
    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
    map.getUiSettings.setTiltGesturesEnabled(false)
    map.setOnMapLongClickListener(new OnMapLongClickListener {

      // On click set guided to there
      def onMapLongClick(l: LatLng) {
        // FIXME show a menu instead & don't loose the icon if we get misled
        val alt = 100
        val loc = Location(l.latitude, l.longitude, alt)
        myVehicle.foreach { v =>
          val wp = v.setGuided(loc)
          val marker = new WaypointMarker(wp)
          scene.markers.clear()
          scene.markers += marker // This is _totally_ not correct FIXME, just goofing around
          scene.render()
          toast("Guided flight selected (alt %dm AGL)".format(alt))
        }
      }
    })
  }

  def startService() {
    warn("FIXME, manually starting service - need to stop it somewhere...")
    startService(new Intent(this, classOf[AndropilotService]))
  }

  /** Ask for permission to access our device */
  def requestAccess() {
    AndroidSerial.getDevice match {
      case Some(device) =>
        accessGrantReceiver = Some(AndroidSerial.requestAccess(device, { d =>
          // requestAccess is not called until the service is up, so we can safely access this
          // If we are already talking to the serial device ignore this
          if (!service.get.isSerialConnected) {
            toast("3DR Telemetry allowed...")
            service.get.serialAttached()
          }
        }, { d =>
          toast("User denied access to USB device")
        }))
      case None =>
        toast("Please attach 3dr telemetry device")
      // startService() // FIXME, remove this
    }
  }
}
