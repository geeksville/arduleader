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
import com.geeksville.flight.MsgModeChanged

class MainActivity extends Activity with TypedActivity
  with AndroidLogger with FlurryActivity with UsesPreferences
  with AndroServiceClient {

  implicit def context = this

  /**
   * If the user just changed the mode menu, ignore device mode msgs briefly
   */
  private var ignoreModeChangesTill = 0L

  private var mainView: View = null
  private var modeSpinner: Option[Spinner] = None

  /**
   * If an intent arrives before our service is up, squirell it away until we can handle it
   */
  private var waitingForService: Option[Intent] = None

  private var watchingSerial = false
  private var accessGrantReceiver: Option[BroadcastReceiver] = None

  // We don't cache these - so that if we get rotated we pull the correct one
  def mFragment = getFragmentManager.findFragmentById(R.id.map).asInstanceOf[MyMapFragment]

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

  override def onVehicleReceive = {
    case MsgModeChanged(_) =>
      setModeSpinner() // FIXME, do this someplace better

    case MsgStatusChanged(s) =>
      debug("Status changed: " + s)
      handler.post { () =>
        toast(s)
      }

    // Super crufty - do this someplace else
    case MsgParametersDownloaded =>
      handler.post { () =>
        for { f <- parameterFragment; v <- myVehicle } yield { f.setVehicle(v) }
      }
  }

  override def onServiceConnected(s: AndropilotService) {
    toast(s.logmsg)

    // If we already had a serial port open start watching it
    registerSerialReceiver()

    // Ask for any already connected serial devices
    //requestAccess()

    waitingForService.foreach { intent =>
      handleIntent(intent)
      waitingForService = None
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    debug("Main onCreate")
    // warn("GooglePlayServices = " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this))

    mainView = getLayoutInflater.inflate(R.layout.main, null)
    setContentView(mainView)

    // textView.setText("hello, world!")

    handler = new Handler

    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
      // Did the user just plug something in?
      Option(getIntent).foreach(handleIntent)
    } else {
      val v = findView(TR.maps_error)
      v.setText("Google Maps is not installed - you will not be able to run this application...")
      v.setVisibility(View.VISIBLE)
      Option(mFragment.getView).foreach(_.setVisibility(View.GONE))
    }
  }

  /**
   * We are a singleTop app, so when other intents arrive we will not start a new instance, rather handle them here
   */
  override def onNewIntent(i: Intent) {
    handleIntent(i)
  }

  override def onResume() {
    super.onResume()

    serviceOnResume()
  }

  override def onPause() {
    serviceOnPause()

    accessGrantReceiver.foreach { r =>
      unregisterReceiver(r)
      accessGrantReceiver = None
    }

    unregisterSerialReceiver()

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
        case Intent.ACTION_MAIN =>
          // Normal app start - just ask for access to any connected devices
          requestAccess()

        case UsbManager.ACTION_USB_DEVICE_ATTACHED =>
          if (AndroidSerial.getDevice.isDefined) {
            toast("3DR Telemetry connected...")
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
   * Update our mode display
   */
  def setModeSpinner() {
    if (System.currentTimeMillis > ignoreModeChangesTill)
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
          val n = findIndex(v.currentMode)
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
        if (modeName != "unknown" && modeName != v.currentMode) {
          // Give up to two seconds before we pay attention to mode msgs - so we don't get confused by stale msgs in our queue
          ignoreModeChangesTill = System.currentTimeMillis + 2000
          v.setMode(modeName.toString)
        }
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

  /** Ask for permission to access our device */
  def requestAccess() {
    warn("Requesting USB access")
    AndroidSerial.getDevice match {
      case Some(device) =>
        accessGrantReceiver = Some(AndroidSerial.requestAccess(device, { d =>

          // Do nothing in here - we will receive a USB attached event.  Only need to post a message if the user _denyed_ access
          warn("USB access received")

          handler.post { () =>
            service.foreach { s =>
              if (!s.isSerialConnected) {
                toast("Connecting link...")
                s.serialAttached()
              }
            }
          }
        }, { d =>

          // This gets called from inside our broadcast receiver - apparently the device is not ready yet, so queue some work for 
          // our GUI thread
          // requestAccess is not called until the service is up, so we can safely access this
          // If we are already talking to the serial device ignore this

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
