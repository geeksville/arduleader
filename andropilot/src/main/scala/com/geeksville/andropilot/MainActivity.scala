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
import android.view.MenuItem
import com.ridemission.scandroid.UsesPreferences
import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.MsgStatusChanged
import com.geeksville.mavlink.MsgHeartbeatLost
import com.geeksville.mavlink.MsgHeartbeatFound
import com.geeksville.flight.MsgWaypointsDownloaded
import com.geeksville.flight.MsgParametersDownloaded

class MainActivity extends Activity with TypedActivity with AndroidLogger with FlurryActivity with UsesPreferences {

  implicit val context = this

  private var myVehicle: Option[MyVehicleListener] = None
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
  private var guidedMarker: Option[Marker] = None

  var planeMarker: Option[Marker] = None

  def parameterFragment = Option(getFragmentManager.findFragmentById(R.id.parameter_fragment).asInstanceOf[ParameterListFragment])

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
  class MyVehicleListener(val v: VehicleMonitor) extends InstrumentedActor {

    /// On first position update zoom in on plane
    private var hasLocation = false

    val subscription = v.eventStream.subscribe(this, { evt: Any => true })

    def titleStr = "Mode " + v.currentMode + (if (!service.get.isSerialConnected) " (No USB)" else (if (v.hasHeartbeat) "" else " (Lost Comms)"))
    def snippet = {
      // Generate a few optional lines of text

      val locStr = v.location.map { l =>
        "Altitude %.1fm".format(l.alt)
      }

      val batStr = v.batteryPercent.map { p => "Battery %sV (%d%%)".format(v.batteryVoltage.get, p * 100 toInt) }

      val r = Seq(v.status, locStr, batStr).flatten.mkString("\n")
      //log.debug("snippet: " + r)
      r
    }

    override def postStop() {
      v.eventStream.removeSubscription(subscription)
      super.postStop()
    }

    private def updateMarker() {
      setModeSpinner() // FIXME, do this someplace better

      v.location.foreach { l => marker.setPosition(new LatLng(l.lat, l.lon)) }
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

        val icon = if (v.hasHeartbeat && service.get.isSerialConnected) R.drawable.plane_blue else R.drawable.plane_red

        log.debug("Creating vehicle marker")
        planeMarker = Some(map.addMarker(new MarkerOptions()
          .position(v.location.map { l => new LatLng(l.lat, l.lon) }.getOrElse(new LatLng(0, 0)))
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

    override def onReceive: Receiver = {
      case l: Location =>
        // log.debug("Handling location: " + l)
        handler.post { () =>
          //log.debug("GUI set position")
          val pos = new LatLng(l.lat, l.lon)
          marker.setPosition(pos)
          if (!hasLocation) {
            // On first update zoom in to plane
            hasLocation = true
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0f))
          }
          updateMarker()
        }

      case MsgStatusChanged(s) =>
        log.debug("Status changed: " + s)
        handler.post(updateMarker _)

      case MsgHeartbeatLost(_) =>
        //log.debug("heartbeat lost")
        handler.post(redrawMarker _)

      case MsgHeartbeatFound(_) =>
        log.debug("heartbeat found")
        handler.post(redrawMarker _)

      case MsgWaypointsDownloaded(wp) =>
        handler.post { () =>
          handleWaypoints(wp)
        }

      // Super crufty - do this someplace else
      case MsgParametersDownloaded =>
        handler.post { () =>
          parameterFragment.foreach(_.setVehicle(v))
        }
    }

    private def redrawMarker() {
      val wasShown = removeMarker()
      marker() // Recreate in red 
      if (wasShown)
        showInfoWindow()
    }
  }

  val serviceConnection = new ServiceConnection() {
    def onServiceConnected(className: ComponentName, serviceIn: IBinder) {
      val s = serviceIn.asInstanceOf[ServiceAPI].service
      service = Some(s)

      debug("Service is bound")

      // We're about to recreate waypoint and plane icons, so for now blow away the map
      map.clear()

      // Don't use akka until the service is created
      s.vehicle.foreach { v =>
        val actor = MockAkka.actorOf(new MyVehicleListener(v), "lst")
        myVehicle = Some(actor)
      }

      toast(s.logmsg)

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

    if (!scene.markers.isEmpty) {
      scene.markers ++= wpts.map { w => new WaypointMarker(w) }

      // Generate segments going between each pair of waypoints (FIXME, won't work with waypoints that don't have x,y position)
      val pairs = scene.markers.zip(scene.markers.tail)
      scene.segments.clear()
      scene.segments ++= pairs.map(p => Segment(p))
      scene.render()
    }
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
      myVehicle.foreach { v =>
        val n = findIndex(v.v.currentMode)
        //debug("Setting mode spinner to: " + n)

        s.setSelection(n)
      }
    }
  }

  /**
   * This really useful method is not on ICS, alas...
   */
  private def getThemedContext = {
    try {
      getActionBar.getThemedContext
    } catch {
      case ex: NoSuchMethodError =>
        this
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.action_bar, menu) // inflate the menu
    val s = menu.findItem(R.id.menu_mode).getActionView().asInstanceOf[Spinner] // find the spinner
    modeSpinner = Some(s)
    val spinnerAdapter = ArrayAdapter.createFromResource(getThemedContext, R.array.mode_names, android.R.layout.simple_spinner_dropdown_item); //  create the adapter from a StringArray
    s.setAdapter(spinnerAdapter); // set the adapter
    setModeSpinner()

    def modeListener(parent: Spinner, selected: View, pos: Int, id: Long) {
      val modeName = spinnerAdapter.getItem(pos)
      debug("Mode selected: " + modeName)
      myVehicle.foreach { v =>
        if (modeName != "unknown" && modeName != v.v.currentMode)
          v.v.setMode(modeName.toString)
      }
    }
    s.onItemSelected(modeListener) // (optional) reference to a OnItemSelectedListener, that you can use to perform actions based on user selection

    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    if (item.getItemId() == R.id.menu_settings)
      startActivity(new Intent(this, classOf[SettingsActivity]))

    super.onOptionsItemSelected(item)
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
        val alt = intPreference("guide_alt", 100)
        val loc = Location(l.latitude, l.longitude, alt)
        myVehicle.foreach { v =>
          val wp = v.v.setGuided(loc)
          val marker = new WaypointMarker(wp)
          guidedMarker.foreach(_.remove())
          guidedMarker = Some(map.addMarker(marker.markerOptions)) // For now we just plop it into the map
          toast("Guided flight selected (alt %dm AGL)".format(alt))
        }
      }
    })
  }

  /** Ask for permission to access our device */
  def requestAccess() {
    AndroidSerial.getDevice match {
      case Some(device) =>
        accessGrantReceiver = Some(AndroidSerial.requestAccess(device, { d =>
          // This gets called from inside our broadcast receiver - apparently the device is not ready yet, so queue some work for 
          // our GUI thread
          // requestAccess is not called until the service is up, so we can safely access this
          // If we are already talking to the serial device ignore this
          handler.post { () =>
            if (!service.get.isSerialConnected) {
              toast("Connecting link...")
              service.get.serialAttached()
            }
          }
        }, { d =>
          handler.post { () =>
            toast("User denied access to USB device")
          }
        }))
      case None =>
        toast("Please attach telemetry or APM")
      // startService() // FIXME, remove this
    }
  }
}
