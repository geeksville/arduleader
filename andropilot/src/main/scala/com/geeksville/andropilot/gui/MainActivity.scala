package com.geeksville.andropilot.gui

import _root_.android.os.Bundle
import android.content.Intent
import com.ridemission.scandroid._
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
import com.geeksville.flight.VehicleModel
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
import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight._
import com.geeksville.mavlink._
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentActivity
import android.support.v4.view._
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import com.geeksville.aspeech.TTSClient
import com.geeksville.util.ThrottleByBucket
import com.geeksville.andropilot.service._
import com.geeksville.andropilot._
import android.net.Uri
import android.view.MotionEvent
import com.geeksville.android.AndroidJUtil
import com.geeksville.util.Using._
import com.geeksville.flight.FenceModel
import com.geeksville.flight.DoLoadWaypoints
import scala.concurrent.duration._
import android.view.InputDevice
import scala.collection.JavaConverters._
import android.view.KeyEvent
import android.content.pm.ActivityInfo
import android.os.Build
import com.geeksville.android.PlayTools
import scala.concurrent._
import ExecutionContext.Implicits.global
import android.support.v4.app.NotificationCompat
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.AlertDialog
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw
import com.geeksville.flight.StatusText
import android.os.Debug
import com.geeksville.flight.MsgReportBug
import com.bugsense.trace.BugSenseHandler
import com.geeksville.flight.MsgRCModeChanged
import android.view.WindowManager
import com.geeksville.gcsapi.WebActivity
import com.geeksville.gcsapi.Webserver

class MainActivity extends FragmentActivity with TypedActivity
  with AndroidLogger with FlurryActivity with AndropilotPrefs with TTSClient
  with AndroServiceClient with JoystickHID with UsesResources with UsesDirectories {

  implicit def context = this

  private var mainView: View = null
  private var modeSpinner: Option[Spinner] = None

  private var oldArmed = false

  /**
   * The first time we receive something that would make the mini overview appear, hide the sidebar - as a cue to the
   * user we are ready to go in simple mode.
   */
  private var didAutohideSidebar = false

  /**
   * If an intent arrives before our service is up, squirel it away until we can handle it
   */
  private var waitingForService: Option[Intent] = None

  private var accessGrantReceiver: Option[BroadcastReceiver] = None

  private lazy val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

  // These pages might be on the main view, so no need to include them in the pager
  private lazy val waypointPageInfo = PageInfo(S(R.string.waypoints), { () => new WaypointListFragment })
  private lazy val overviewPageInfo = PageInfo(S(R.string.overview), { () => new OverviewFragment })

  private lazy val stdPages = List(
    PageInfo(S(R.string.parameters), { () => new ParameterPane() }),
    PageInfo(S(R.string.hud), { () => new HudFragment }),
    PageInfo("Status", { () => new StatusMsgFragment }),
    PageInfo(S(R.string.rc_channels), { () => new RcChannelsFragment }),
    PageInfo(S(R.string.servos), { () => new ServoOutputFragment }))

  /**
   * If we don't have enough horizontal width - the layout will move the map into the only (pager) view.
   * Make it the first/default page
   */
  private lazy val mapPageInfo = PageInfo(S(R.string.map), { () => new MyMapFragment })

  // We don't cache these - so that if we get rotated we pull the correct one
  // Also - might not always be present, so we make it an option
  def mapFragment = Option(getFragmentManager.findFragmentById(R.id.map).asInstanceOf[MyMapFragment])
  def viewPager = Option(findViewById(R.id.pager).asInstanceOf[ViewPager])

  lazy val joystickPanel = Option(findViewById(R.id.joysticks))
  lazy val leftJoystickView = Option(findViewById(R.id.leftStick).asInstanceOf[JoystickView])
  lazy val rightJoystickView = Option(findViewById(R.id.rightStick).asInstanceOf[JoystickView])
  lazy val cancelOverrideButton = Option(findViewById(R.id.cancelOverride).asInstanceOf[Button])

  // On some layouts we have dedicated versions of these views
  def waypointFragment = Option(findViewById(R.id.waypoint_fragment))
  def overviewFragment = Option(findViewById(R.id.overview_fragment))

  /**
   * Does work in the GUIs thread
   */
  var handler: Handler = null

  private var oldVehicleType: Option[Int] = None

  private lazy val throttleAlt = new ThrottleByBucket(speechAltBucket)
  private val throttleBattery = new ThrottleByBucket(10)

  val warningChecker = MockAkka.scheduler.schedule(60 seconds, 60 seconds) { () =>
    val warning = if (isLowVolt)
      R.string.spk_warn_volt
    else if (isLowBatPercent)
      R.string.spk_warn_battery
    else if (isLowRssi)
      R.string.spk_warn_radio
    else if (isLowNumSats)
      R.string.spk_warn_gps
    else
      -1

    if (warning != -1 && handler != null)
      handler.post { () =>
        speak(S(warning), true)
      }
  }

  override def onVehicleReceive: InstrumentedActor.Receiver = {

    case l: Location =>
      myVehicle.foreach { v =>
        throttleAlt(v.bestAltitude.toInt) { alt =>
          handler.post { () =>
            debug("Speak alt: " + alt)
            speak(S(R.string.spk_meter).format(alt))
          }
        }
      }

    case MsgFenceBreached =>
      handler.post { () => speak("Fence Breached", urgent = true) }

    case MsgSysStatusChanged =>
      for { v <- myVehicle; pct <- v.batteryPercent } yield {
        throttleBattery((pct * 100).toInt) { pct =>
          handler.post { () =>
            debug("Speak battery: " + pct)
            speak(S(R.string.spk_percent).format(pct))
          }
        }
      }

    case MsgReportBug(m) =>
      handler.post { () =>
        val e = new Exception(m)
        BugSenseHandler.sendExceptionMessage("model_bug", "state_machine", e)
        speak("Warning, non fatal bug")
        toast("Non fatal andropilot bug - please post on diydrones.com")
      }

    case MsgWaypointCurrentChanged(n) =>
      handler.post { () =>
        speak("Waypoint " + n)
      }

    case MsgRCModeChanged(_) =>
      handler.post { () =>
        stopOverrides()
      }

    case MsgModeChanged(_) =>
      handler.post { () =>
        debug("modeChanged received")

        myVehicle.foreach { v =>
          if (oldVehicleType != v.vehicleType)
            usageEvent("vehicle_type", "type" -> v.vehicleType.toString)

          usageEvent("set_mode", "mode" -> v.currentMode)
        }

        invalidateOptionsMenu()
      }

    case MsgHeartbeatLost =>
      handler.post { () =>
        usageEvent("heartbeat_lost")
        speak(S(R.string.spk_heartbeat_lost), urgent = true)
        setModeSpinner()
      }

    case StatusText(s, severity) =>
      handler.post { () =>
        handleStatus(s, severity)
      }

    case MsgParametersDownloaded =>
      handler.post { () =>
        autohideSidebar()
        setModeSpinner()
        handleParameters()
      }

    case MsgRcChannelsChanged =>
      handler.post { () =>
        myVehicle.foreach { v =>
          v.rcChannelsRaw.foreach(handleRCChannels)
        }
      }
  }

  private def handleStatus(s: String, severity: Int) {
    debug("Status changed: " + s)
    if (severity != MsgStatusChanged.SEVERITY_USER_RESPONSE) {
      val isImportant = severity >= MsgStatusChanged.SEVERITY_HIGH
      // toast(s, isImportant) - we show this on the map view now

      if (isImportant)
        speak(s)

    } else {
      // Show a user dialog and have them ack what the APM wants acked

      val builder = new AlertDialog.Builder(this)
      builder.setMessage(s).setCancelable(false).setPositiveButton("Ok", { which: Int =>
        myVehicle.foreach { v =>
          v.sendMavlink(v.commandAck())
        }
      })

      builder.create().show()
    }
  }

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    toast(s.serviceStatus)

    // Ask for any already connected serial devices
    requestAccess()

    // If the menu is already up - update the set of options & selected mode
    invalidateOptionsMenu()

    waitingForService.foreach { intent =>
      handleIntent(intent)
      waitingForService = None
    }

    myVehicle.foreach { v =>
      if (v.hasParameters)
        initAndShowJoystick()
    }
  }

  private def screenWidthDp = try {
    getResources.getConfiguration.screenWidthDp
  } catch {
    case ex: NoSuchFieldError =>
      // Must be on an old version of android 
      0
  }

  private def screenSmallestWidthDp = try {
    getResources.getConfiguration.smallestScreenWidthDp
  } catch {
    case ex: NoSuchFieldError =>
      // Must be on an old version of android 
      0
  }

  private def screenHeightDp = try {
    getResources.getConfiguration.screenHeightDp
  } catch {
    case ex: NoSuchFieldError =>
      // Must be on an old version of android 
      0
  }

  private def screenIsLong = try {
    (getResources.getConfiguration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) == Configuration.SCREENLAYOUT_LONG_YES
  } catch {
    case ex: NoSuchFieldError =>
      // Must be on an old version of android 
      false
  }

  protected def selectNextPage(toRight: Boolean) {
    for { v <- viewPager } yield {
      val numPages = {
        val adapter = Option(v.getAdapter.asInstanceOf[ScalaPagerAdapter])
        adapter.map(_.getCount).getOrElse(0)
      }

      val c = v.getCurrentItem
      if (toRight && c < numPages - 1)
        v.setCurrentItem(c + 1)
      else if (!toRight && c > 0)
        v.setCurrentItem(c - 1)
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    debug("Main onCreate")
    warn("HW make " + Build.MANUFACTURER)
    warn("HW model " + Build.MODEL)
    warn("HW device " + Build.DEVICE)
    warn("HW product " + Build.PRODUCT)
    warn(s"Screen width $screenWidthDp")
    warn(s"Screen height $screenHeightDp")
    warn(s"Screen smallest width $screenSmallestWidthDp")
    // warn("GooglePlayServices = " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this))

    val isArchosGamepad = Build.MANUFACTURER == "Archos" && Build.DEVICE == "A70GP"

    // If we are on a phone sized device disallow landscape mode (our panes become too small)
    // Check for a 'long' screen as a hack to turn off this code for samsung note
    // Width: Samsung note returns 360 and it seems like regular phones are about 320 or 360...
    // Height: My galaxy nexus is 567, so say <600 means a phone...
    if (screenSmallestWidthDp <= 360 && !screenIsLong)
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    if (screenHeightDp < 360) { // Are we on a phone screen and in landscape - turn off the titlebar, cause we need all we can get
      warn("Requesting full screen")
      getWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // The archos gamepad has joysticks on the side - it really only makes sense in landscape
    if (isArchosGamepad)
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    try {
      mainView = getLayoutInflater.inflate(R.layout.main, null)
      setContentView(mainView)

      // Hide the navigation bar at the bottom
      //mainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

      if (screenWidthDp < 600) // Screen is pretty narrow, default to no sidebar
        showSidebar(false)

      // textView.setText("hello, world!")

      handler = new Handler

      // Set up the ViewPager with the sections adapter (if it is present on this layout)
      viewPager.foreach { v =>
        v.setAdapter(sectionsPagerAdapter)
      }

      val hasPlay = PlayTools.checkForServices(this)
      if (hasPlay) {
        // Did the user just plug something in?
        Option(getIntent).foreach(handleIntent)
      } else {
        Option(findView(TR.maps_error)).map { v =>
          val probe = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
          val msg = if (probe == ConnectionResult.SERVICE_INVALID)
            """|Google Maps can not be embedded - Google reports that 'Google Play Services' is not authentic.
             |(If you are seeing this message and using a 'hobbyist' ROM, check with the
             |person who made that ROM - it seems like they made a mistake repackaging the service)""".stripMargin
          else
            """|Google Maps V2 is not installed (code=%d) - you will not be able to run this application... 
             |(If you are seeing this message and using a 'hobbyist' ROM, check with the
             |person who made that ROM - it seems like they forgot to include a working version of 'Google
             |Play Services')""".stripMargin.format(probe)
          v.setText(msg)
          v.setVisibility(View.VISIBLE)

          // Alas - this seems to not work
          // GooglePlayServicesUtil.getErrorDialog(probe, this, 1).show()
        }.getOrElse {
          error("Some chinese WonderMe device is out there failing to find google maps, sorry - you are out of luck")
        }
        for { map <- mapFragment; view <- Option(map.getView) } yield { view.setVisibility(View.GONE) }
      }
      initScreenJoysticks()
      initSpeech()
    } catch {
      case ex: NoSuchFieldError =>
        toast("Your tablet has a pirated/old version of google play - Andropilot can not start")
        BugSenseHandler.sendExceptionMessage("play-failure", "exception", new Exception(ex))
    }
  }

  def application = getApplication.asInstanceOf[MyApplication]

  /**
   * Ask our map view to center on a particular location (allows other views to push the map around)
   */
  def zoomMapTo(mappos: LatLng) {
    debug(s"map $mapFragment")
    for {
      mapf <- mapFragment
      gmap <- mapf.mapOpt
    } yield {
      debug(s"Zooming to $mappos")
      gmap.animateCamera(CameraUpdateFactory.newLatLng(mappos))
    }
  }

  def checkSunspots() {
    val t = new SunspotDetectorTask(this) {
      override protected def didSunspot(curLevel: Option[Int]) {
        application.lastSunspotCheck = System.currentTimeMillis()
      }
    }
    t.execute()
  }

  def perhapsCheckSunspots() {
    val now = System.currentTimeMillis()

    val expireTime = application.lastSunspotCheck + 60 * 60 * 1000
    if (now > expireTime)
      checkSunspots()
  }

  override def startOverride() {
    super.startOverride()
    cancelOverrideButton.foreach { v =>
      v.setVisibility(View.VISIBLE)
    }
  }

  override def stopOverrides() {
    super.stopOverrides()
    cancelOverrideButton.foreach { v =>
      v.setVisibility(View.INVISIBLE)
    }
  }

  private def initScreenJoysticks() {
    cancelOverrideButton.foreach { v =>
      v.onClick { b =>
        stopOverrides()
      }
    }

    leftJoystickView.foreach { v =>
      v.xLabel = "Yaw"
      v.yLabel = "Throttle"
      v.centerYonRelease = false
      v.listener = new JoystickListener {
        override def onMove(x: Float, y: Float) {
          rudder = x
          throttle = (-y + 1) / 2 // convert -1 -> 1 into 1 -> 0, so 0 on bottom and 1 and top (for more travel) (was -y)
          sendOverride()
        }
        override def onPress() {
          startOverride()
        }
      }
    }

    rightJoystickView.foreach { v =>
      v.listener = new JoystickListener {
        override def onMove(x: Float, y: Float) {
          aileron = x
          elevator = y // FIXME - not sure why this is not inverted
          sendOverride()
        }
        override def onPress() {
          startOverride()
        }
      }
    }
  }

  def handleRCChannels(x: msg_rc_channels_raw) = {
    if (joystickAvailable && !isOverriding) {
      leftJoystickView.foreach { v =>
        val thro = -axis(throttleAxisNum).unscale(x.chan3_raw)
        //debug(axis(throttleAxisNum) + " thro " + x.chan3_raw + " to " + thro)
        v.setReceived(axis(rudderAxisNum).unscale(x.chan4_raw), thro)
      }

      rightJoystickView.foreach { v =>
        val ail = axis(aileronAxisNum).unscale(x.chan1_raw)
        //debug(axis(aileronAxisNum) + " ail " + x.chan1_raw + " to " + ail)

        val ele = axis(elevatorAxisNum).unscale(x.chan2_raw)
        //debug(axis(elevatorAxisNum) + " ele " + x.chan2_raw + " to " + ele)
        v.setReceived(ail, ele)
      }
    }
  }

  // Workaround to make sure child fragment state is not saved on orientation page (makes fragment panes show correctly)
  // http://stackoverflow.com/questions/13910826/viewpager-fragmentstatepageradapter-orientation-change
  override def onSaveInstanceState(outState: Bundle) {
    // super.onSaveInstanceState(outState);
  }

  private def sectionsPagerAdapter = {

    new ScalaPagerAdapter(getSupportFragmentManager, pages) {

      // Force views to get recreated on orientation change - http://stackoverflow.com/questions/7263291/viewpager-pageradapter-not-updating-the-view
      override def getItemPosition(obj: Object) = {
        PagerAdapter.POSITION_NONE
      }
    }
  }

  def isWide = viewPager.map(_.getTag == "with-sidebar").getOrElse(false)

  private def pages = {
    var r = stdPages

    if (!waypointFragment.isDefined)
      r = waypointPageInfo :: r

    if (!overviewFragment.isDefined)
      r = overviewPageInfo :: r

    // If we need to add a map view add it first (FIXME, check for this need by looking for mapFragment in the layout)
    if (!isWide)
      r = mapPageInfo :: r

    debug("Using wide view=" + isWide + " pages=" + r.mkString(","))
    r.toIndexedSeq
  }

  /**
   * We are a singleTop app, so when other intents arrive we will not start a new instance, rather handle them here
   */
  override def onNewIntent(i: Intent) {
    handleIntent(i)
  }

  /**
   * If the user wants the screen always on or the joystick is up, keep the screen awake
   */
  private def setScreenOn() {
    mainView.setKeepScreenOn(isKeepScreenOn || isJoystickShown)
  }

  override def onResume() {
    super.onResume()

    serviceOnResume()

    // Force the screen on if the user wants that 
    setScreenOn()

    //toast("Screen layout=" + getResources.getConfiguration.screenLayout, isLong = true)

    if (shouldNagUser) {
      val pendingIntent = PendingIntent.getActivity(this, 0, SettingsActivity.sharingSettingsIntent(this), 0)

      val nBuilder = new NotificationCompat.Builder(context)
      nBuilder.setContentTitle("Configure droneshare.com settings")
        .setContentText("Select this to configure or disable sharing")
        .setSmallIcon(android.R.drawable.ic_menu_share)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setContentIntent(pendingIntent)
        .setTicker("Please configure droneshare settings")

      notifyManager.notify(NotificationIds.setupDroneshareId, nBuilder.build)
    } else
      notifyManager.cancel(NotificationIds.setupDroneshareId)

    perhapsCheckSunspots()
  }

  def showSplashDialog() {
    val fm = getSupportFragmentManager()
    val splash = new SplashFragment()
    splash.show(fm, "spash_fragment")
  }

  override def onPause() {
    serviceOnPause()

    accessGrantReceiver.foreach { r =>
      unregisterReceiver(r)
      accessGrantReceiver = None
    }

    super.onPause()
  }

  override def onDestroy() {
    warningChecker.cancel()
    destroySpeech()

    super.onDestroy()
  }

  def initAndShowJoystick() {
    initJoystickParams()

    // FIXME - show joystick based on user prefs or no RC
    // FIXME - if rc status changes, possibly show joysticks
  }

  /// If we are configured to upload, but have no username/psw tell user why we are ignoring them
  def shouldNagUser = dshareUpload && (dshareUsername.isEmpty || dsharePassword.isEmpty)

  private def handleParameters() {
    initAndShowJoystick()

    invalidateOptionsMenu()

    // Our parameters are valid, perhaps write them to disk (FIXME, this really should be done in the service)

    if (paramsToFile)
      for { dir <- paramDirectory; vm <- myVehicle } yield {
        val file = ParameterFile.getFilename(dir)
        try {
          usageEvent("params_saved")
          ParameterFile.create(vm.parameters, file)
          toast(S(R.string.parameters_backed_up).format(dir))
        } catch {
          case ex: Exception =>
            error("Can't write param file: " + ex.getMessage)
        }
      }
  }

  /**
   * Do our file loading in a background thread - but do the associate UI in the UI thread
   */
  class FileLoadTask(val uri: Uri) extends AsyncVoidTask {
    val filename = uri.getLastPathSegment
    private var message: Option[String] = None

    override protected def inBackground() {

      debug("Handling fileOpen (in background thread)")

      using(AndroidJUtil.getFromURI(context, uri)) { s =>
        debug("Opened fileOpen (in background thread)")
        myVehicle.foreach { v =>
          debug("Got vehicle in fileOpen (in background thread)")

          val vehicleReady = v.hasHeartbeat && !v.waypoints.isEmpty

          if (!vehicleReady) {
            message = Some("Vehicle not yet initialized, please try again.")
          } else {
            if (filename.toLowerCase.endsWith(".param")) {
              message = Some("Parameter file reading not yet supported")
            }

            if (filename.toLowerCase.endsWith(".fen")) {
              if (!v.isFenceAvailable)
                toast(R.string.fence_not_avail, true)
              else {
                usageEvent("fence_uploaded", "url" -> uri.toString)
                message = Some(S(R.string.uploading_fence).format(filename))
                val pts = FenceModel.pointsFromStream(s)

                v ! DoSetFence(pts, fenceMode)
              }
            }

            if (filename.toLowerCase.endsWith(".txt") || filename.toLowerCase.endsWith(".wpt")) {
              debug("Doing upload")

              usageEvent("waypoint_uploaded", "url" -> uri.toString)

              try {
                val pts = v.pointsFromStream(s)
                debug(s"Loaded ${pts.size} waypoints")

                message = Some(s"Loaded waypoints from $filename, please click to upload")
                v ! DoLoadWaypoints(pts)
                // v ! SendWaypoints
              } catch {
                case ex: Exception =>
                  message = Some(ex.getMessage) // Error reading from file (probably not a waypoint file)
              }
            }
          }
        }
      }
    }

    protected override def onPostExecute(unused: Void) {
      message.foreach { s => toast(s) }
    }
  }

  private def handleFileOpen(uri: Uri) {
    (new FileLoadTask(uri)).execute()
  }

  private def handleIntent(intent: Intent) {
    debug("Received intent: " + intent)
    service.map { s =>
      intent.getAction match {
        case Intent.ACTION_VIEW =>
          Option(intent.getData).foreach { uri =>
            // User wants to open a file
            handleFileOpen(uri)
          }
        case Intent.ACTION_MAIN =>
          // Normal app start - just ask for access to any connected devices
          requestAccess()

        case UsbManager.ACTION_USB_DEVICE_ATTACHED =>
          if (!AndroidSerial.getDevices.isEmpty) {
            // speak("Connected")
            toast(R.string.telem_connected, false)
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
    debug("in setMode")
    modeSpinner.foreach { s =>
      // Crufty way of finding which element of spinner needs selecting
      def findIndex(str: String) = {
        val adapter = s.getAdapter

        (0 until adapter.getCount).find { i =>
          val is = adapter.getItem(i).toString
          is == str || is == "unknown"
        }
      }
      myVehicle.foreach { v =>
        val modeName = if (v.hasHeartbeat) {
          val toSpeak = if (v.isArmed != oldArmed) {
            oldArmed = v.isArmed
            if (oldArmed) "Armed" else "Disarmed"
          } else
            v.currentMode
          speak(toSpeak)
          debug("Spinning to " + v.currentMode)
          v.currentMode
        } else {
          debug("No heartbeat - claiming unknown")
          "unknown"
        }

        findIndex(modeName) match {
          case Some(n) =>
            val curModeName = s.getSelectedItem.toString
            debug(s"Current mode string is $curModeName, modeName $modeName")
            if (curModeName != modeName) {
              debug(s"Setting mode spinner to: $n " + s.getAdapter.getItem(n))
              setSpinnerNoNotify(s, n)
            }
          case None =>
            error(s"Can't find spinner for $modeName")
        }
      }
    }
  }

  /**
   * Update the set of options in the mode menu (called when vehicle type changes)
   */
  private def setModeOptions() {
    debug("Considering modeOptions")
    for { s <- modeSpinner; v <- myVehicle } yield {
      if (oldVehicleType != v.vehicleType || s.getAdapter == null) {
        debug("Assigning new mode options")

        val spinnerAdapter = new ArrayAdapter(MainActivity.getThemedContext(this),
          android.R.layout.simple_spinner_dropdown_item, v.modeNames.toArray)
        // val spinnerAdapter = ArrayAdapter.createFromResource(getThemedContext, R.array.mode_names, android.R.layout.simple_spinner_dropdown_item); //  create the adapter from a StringArray
        s.setAdapter(spinnerAdapter); // set the adapter

        // We have now recorded our vehicle type
        oldVehicleType = v.vehicleType
      }
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    debug("Creating option menu")
    getMenuInflater.inflate(R.menu.action_bar, menu) // inflate the menu
    val s = menu.findItem(R.id.menu_mode).getActionView().asInstanceOf[Spinner] // find the spinner
    modeSpinner = Some(s)
    setModeOptions()
    setModeSpinner()

    val showSidebarMenu = menu.findItem(R.id.menu_showsidebar)
    showSidebarMenu.setVisible(isWide)
    showSidebarMenu.setChecked(viewPager.map(_.isShown).getOrElse(false))

    menu.findItem(R.id.menu_tracing).setVisible(developerMode)
    menu.findItem(R.id.menu_speech).setChecked(isSpeechEnabled)
    val checklist = menu.findItem(R.id.menu_checklist)
    checklist.setVisible(runWebserver)
    checklist.setEnabled(false)

    // Set some defaults in case we don't have a service
    val joystickMenu = menu.findItem(R.id.menu_showjoystick)
    val armMenu = menu.findItem(R.id.menu_arm)
    val hasJoystickView = joystickPanel.isDefined
    armMenu.setVisible(false) // If we don't know we have a copter leave this hidden

    service foreach { svc =>
      val follow = menu.findItem(R.id.menu_followme)

      follow.setEnabled(FollowMe.isAvailable(this))
      // If the user has customized min/max distances they are really going to be _leading_ instead
      val isLeading = minDistance != 0.0f || maxDistance != 0.0f
      follow.setTitle(if (isLeading) R.string.lead_it else R.string.follow_me)
      follow.setChecked(svc.isFollowMe)

      myVehicle.foreach { v =>
        checklist.setEnabled(v.hasHeartbeat)

        if (v.isCopter) {
          val armed = v.isArmed
          debug(s"Setting arm checkbox to $armed, hb ${v.hasHeartbeat} / conn ${svc.isConnected}")
          armMenu.setVisible(true)
          armMenu.setChecked(armed)
          armMenu.setEnabled(v.hasHeartbeat && svc.isConnected)
        }

        // Only possibly turn the joystick on if we have a vehicle
        joystickMenu.setEnabled(hasJoystickView && (developerMode || (isVehicleConnected && joystickAvailable)))
      }

      joystickMenu.setChecked(isJoystickShown)

      val gotoMenu = menu.findItem(R.id.menu_gotovehicle)
      gotoMenu.setEnabled(navToVehicleIntent.isDefined)

      // FIXME - host this help doc in some better location (local?) and possibly use a webview
      menu.findItem(R.id.menu_help).setIntent(viewHtmlIntent(
        Uri.parse("https://github.com/geeksville/arduleader/wiki/Andropilot-Users-Guide")))
    }

    true
  }
  /**
   * Are we currently showing the joystick?
   */
  private def isJoystickShown = joystickPanel.map(_.getVisibility == View.VISIBLE).getOrElse(false)

  /// Set a spinner selection without accidentally notifying ourselves
  private def setSpinnerNoNotify(s: Spinner, toSelect: Int) {
    def modeListener(parent: Spinner, selected: View, pos: Int, id: Long) {
      val modeName = s.getAdapter.getItem(pos).toString
      info(s"Mode selected: $modeName ($pos)")
      myVehicle.foreach { v =>
        if (modeName != "unknown" && modeName != v.currentMode) {
          service.foreach(_.setFollowMe(false)) // Immediately cancel any follow-me
          debug("Sending DoSetMode: " + modeName)
          v ! DoSetMode(modeName.toString)
        }
      }
    }

    s.setOnItemSelectedListener(null)
    s.post { () =>
      s.setSelection(toSelect)
      s.post { () =>
        s.onItemSelected(modeListener)
      }
    }
  }

  /**
   * If we are capable of navigating to the current vehicle location, return an intent that will get us there
   */
  private def navToVehicleIntent = {
    (for { v <- myVehicle; loc <- v.location } yield {

      val navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=%f,%f".format(loc.lat, loc.lon)))
      val info = context.getPackageManager().resolveActivity(navIntent, 0)

      if (info != null)
        Some(navIntent)
      else
        None
    }).getOrElse(None)
  }

  private def setShowJoystick(show: Boolean) {
    if (!show)
      stopOverrides()

    joystickPanel.foreach { panel =>
      panel.setVisibility(if (show) View.VISIBLE else View.INVISIBLE)

      if (show) {
        // If the joysticks are filling our screen, we need to turn off the sidebar to make space
        for {
          l <- leftJoystickView
          r <- rightJoystickView
        } yield {
          if (l.getWidth + r.getWidth >= panel.getWidth)
            showSidebar(false)
        }
      }
    }

    // We might need to turn screen sleep on/off
    setScreenOn()
  }

  /**
   * We automatically hide the sidebar once the mini overview is showing valid data
   */
  private def autohideSidebar() {
    if (!didAutohideSidebar) {
      didAutohideSidebar = true
      showSidebar(false)
    }
  }

  private def showSidebar(shown: Boolean) {
    if (isWide) // If we _only_ have the sidebar - don't hide it
      viewPager.foreach(_.setVisibility(if (shown) View.VISIBLE else View.GONE))
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.menu_settings =>
        startActivity(new Intent(this, classOf[SettingsActivity]))

      case R.id.menu_speech =>
        val n = !item.isChecked
        debug("Toggle speech, newmode " + n)
        isSpeechEnabled = n
        item.setChecked(n)

      case R.id.check_sun =>
        checkSunspots()

      case R.id.menu_arm =>
        val n = !item.isChecked
        myVehicle.foreach { v =>
          v ! DoSetMode(if (n) "Arm" else "Disarm")
          //item.setChecked(n) - wait for the next heartbeat msg
        }

      case R.id.menu_showsidebar =>
        val n = !item.isChecked
        item.setChecked(n)
        showSidebar(item.isChecked)

      case R.id.menu_tracing =>
        val n = !item.isChecked
        item.setChecked(n)
        if (n) {
          toast("Tracing enabled")
          Debug.startMethodTracing("andropilot")
        } else {
          Debug.stopMethodTracing()
          toast("Tracing disabled")
        }

      case R.id.menu_showjoystick =>
        val n = !item.isChecked
        item.setChecked(n)
        setShowJoystick(n)

      case R.id.menu_gotovehicle =>
        navToVehicleIntent.map { intent =>
          startActivity(intent)
        }

      case R.id.menu_checklist =>
        WebActivity.showURL(context, "http://localhost:%s/static/checklist.html".format(Webserver.portNumber))

      case R.id.menu_followme => // FIXME - move this into the map fragment
        service.foreach { s =>
          debug("Toggle followme")
          val n = !item.isChecked
          s.setFollowMe(n)
          item.setChecked(s.isFollowMe)
        }

      case R.id.menu_levelnow =>
        myVehicle.foreach { v =>
          if (inFlight)
            toast(R.string.no_level, true)
          else
            v.sendMavlink(v.commandDoCalibrate(calINS = true, calBaro = true))
        }

      case R.id.menu_calibrate =>
        myVehicle.foreach { v =>
          if (inFlight)
            toast(R.string.no_level, true)
          else
            v.sendMavlink(v.commandDoCalibrate(calAccel = true))
        }
      case _ =>
    }

    super.onOptionsItemSelected(item)
  }

  /**
   * Do we think the vehicle is flying?
   */
  private def inFlight = myVehicle.flatMap(_.vfrHud.map(_.groundspeed > 0.5f)).getOrElse(true)

  /** Ask for permission to access our device */
  def requestAccess() {
    warn("Requesting USB access")
    service.foreach { s =>
      val devs = AndroidSerial.getDevices
      if (!s.isConnected && devs.isEmpty)
        toast(R.string.please_attach, true)
      else
        // We only ask for access to devices we don't already have
        devs.filter { dev => !s.serialDevices.contains(dev.getDeviceId) }.foreach { device =>
          accessGrantReceiver = Some(AndroidSerial.requestAccess(device, { d =>

            // Do nothing in here - we will receive a USB attached event.  Only need to post a message if the user _denyed_ access
            warn("USB access received")

            handler.post(() =>
              if (!s.isSerialConnected) {
                // toast(R.string.connecting_link, false)
                s.serialAttached(d)
              })
          }, { d =>

            // This gets called from inside our broadcast receiver - apparently the device is not ready yet, so queue some work for 
            // our GUI thread
            // requestAccess is not called until the service is up, so we can safely access this
            // If we are already talking to the serial device ignore this

            handler.post { () =>
              toast(R.string.usb_access_denied, true)
            }
          }))
        }
    }
  }

  private def viewHtmlIntent(url: Uri) = new Intent(Intent.ACTION_VIEW, url)
}

object MainActivity {
  /**
   * This really useful method is not on ICS, alas...
   */
  def getThemedContext(c: FragmentActivity) = {
    try {
      c.getActionBar.getThemedContext
    } catch {
      case ex: NoSuchMethodError =>
        c
    }
  }
}