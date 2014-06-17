package com.geeksville.andropilot.service

import scala.concurrent.duration._
import com.geeksville.flight.FlightLead
import com.geeksville.flight.IGCPublisher
import android.app._
import android.content.Intent
import com.ridemission.scandroid._
import com.geeksville.mavlink.MavlinkEventBus
import android.os._
import scala.io.Source
import com.geeksville.mavlink.LogIncomingMavlink
import com.geeksville.akka.MockAkka
import java.io.File
import com.geeksville.mavlink.LogBinaryMavlink
import com.geeksville.mavlink._
import com.geeksville.flight.VehicleSimulator
import android.content.BroadcastReceiver
import android.content.Context
import android.hardware.usb.UsbManager
import android.content.IntentFilter
import com.ridemission.scandroid.UsesPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.geeksville.flight.VehicleModel
import com.geeksville.util.ThreadTools._
import com.geeksville.mavlink.MavlinkUDP
import com.flurry.android.FlurryAgent
import com.geeksville.andropilot.R
import com.geeksville.andropilot.gui.MainActivity
import com.geeksville.andropilot.FlurryService
import com.geeksville.andropilot.AndropilotPrefs
import com.geeksville.util.NetTools
import com.geeksville.akka.InstrumentedActor
import android.bluetooth.BluetoothSocket
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.IOException
import android.support.v4.app.NotificationCompat
import com.geeksville.andropilot.gui.NotificationIds
import com.bugsense.trace.BugSenseHandler
import com.geeksville.andropilot.UsesDirectories
import com.geeksville.flight.OnInterfaceChanged
import android.hardware.usb.UsbDevice
import scala.collection.mutable.HashMap
import com.geeksville.gcsapi.GCSAdapter
import com.geeksville.gcsapi.TempGCSModel
import com.geeksville.gcsapi.Webserver
import com.geeksville.gcsapi.AndroidWebserver
import com.geeksville.flight.ParameterDocFile
import com.geeksville.util.ThreadTools
import com.geeksville.aspeech.TTSClient
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import com.geeksville.flight.EventBusVehicleReceiver
import com.geeksville.mavlink.MavlinkReceiver
import com.geeksville.mavlink.SendsMavlinkToEventbus
import com.geeksville.apiproxy.APIConstants
import com.geeksville.apiproxy.LiveUploader
import com.geeksville.apiproxy.APIProxyActor

trait ServiceAPI extends IBinder {
  def service: AndropilotService
}

class AndropilotService extends Service with TTSClient with AndroidLogger
  with FlurryService with AndropilotPrefs with BluetoothConnection with UsesResources with UsesDirectories {

  val groundControlId = 255

  /**
   * If we are logging the file is here
   */
  private var logger: Option[ActorRef] = None
  private var prefListeners: Seq[OnSharedPreferenceChangeListener] = Seq()

  var vehicle: Option[VehicleModel] = None

  /**
   * A mapping from usb unique device id to the stream for that device
   */
  private val serial: HashMap[Int, ActorRef] = HashMap()

  private var udp: Option[ActorRef] = None

  private var speaker: Option[ActorRef] = None

  private var btInputStream: Option[InputStream] = None
  private var btStream: Option[ActorRef] = None

  private var follower: Option[ActorRef] = None

  private var uploader: Option[AndroidDirUpload] = None

  private var pebbleListener: Option[ActorRef] = None

  private var errorMessage: Option[String] = None

  private var webServer: Option[ActorRef] = None

  private var droneshare: Option[ActorRef] = None

  implicit val acontext = this

  def system = MockAkka.system

  /// This handler needs to be created in the foreground thread (shared with actors such as Speaker)
  lazy val handler = new Handler

  private lazy val wakeLock = getSystemService(Context.POWER_SERVICE).asInstanceOf[PowerManager].newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU")

  //
  // Not really the ideal place for these status things - but leave here for now
  //

  def isLowVolt = (for { v <- vehicle; volt <- v.batteryVoltage } yield { v.hasHeartbeat && volt < minVoltage }).getOrElse(false)

  /// Apparently ardupane treats -1 for pct charge as 'no idea'
  def isLowBatPercent = (for { v <- vehicle; pct <- v.batteryPercent } yield {
    v.hasHeartbeat && (pct < minBatPercent && pct >= -1.0)
  }).getOrElse(false)
  def isLowRssi = (for { v <- vehicle; r <- v.radio } yield {
    val span = minRssiSpan

    v.hasHeartbeat && (r.rssi - span < r.noise || r.remrssi - span < r.remnoise)
  }).getOrElse(false)
  def isLowNumSats = (for { v <- vehicle; n <- v.numSats } yield { v.hasHeartbeat && n < minNumSats }).getOrElse(false)
  def isWarning = isLowVolt || isLowBatPercent || isLowRssi || isLowNumSats

  /**
   * Class for clients to access.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with
   * IPC.
   */
  private val binder = new Binder with ServiceAPI {
    def service = AndropilotService.this
  }

  /**
   * We install this receiver only once we're connected to a device
   */
  private val disconnectReceiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      warn("In disconnect receiver")

      def dev = intent.getParcelableExtra[UsbDevice](UsbManager.EXTRA_DEVICE)

      intent.getAction match {
        case UsbManager.ACTION_USB_DEVICE_DETACHED =>
          serialDetached(dev.getDeviceId)
        // We rely on the activity for this (currently)
        //case UsbManager.ACTION_USB_DEVICE_ATTACHED =>
        //  serialAttached(dev)
        case x @ _ =>
          debug(s"Ignoring USB action $x")
      }
    }
  }

  override def onBind(intent: Intent) = binder

  /**
   * Read a file in assets
   */
  def assetToString(name: String) = Source.fromInputStream(getAssets().open(name)).
    getLines().mkString("\n")

  def isSerialConnected = !serial.isEmpty
  def isBluetoothConnected = btStream.isDefined
  def isFollowMe = follower.isDefined

  // Are we talking to a device at all?
  def isConnected = {
    val r = isSerialConnected || udp.isDefined || isBluetoothConnected
    warn(s"Service returning connected=$r")
    r
  }

  /**
   * The USB device ids of any connected serial adapter
   */
  def serialDevices = serial.keySet

  /**
   * A human readable description of our logging state
   */
  def serviceStatus = {
    val linkMsg = if (errorMessage.isDefined)
      errorMessage.get
    else if (isSerialConnected)
      S(R.string.usb_link)
    else if (btStream.isDefined)
      S(R.string.bluetooth_link)
    else
      udp.map { u =>
        udpMode + " " + NetTools.localIPAddresses.mkString(",")
      }.getOrElse(S(R.string.no_link))

    val logmsg = if (loggingEnabled)
      logger.map { f => S(R.string.logging) }.getOrElse(S(R.string.no_sd_card))
    else
      S(R.string.no_logging)

    if (!isConnected)
      linkMsg
    else
      linkMsg + " " + logmsg
  }

  def inboundUdpEnabled = udpMode == UDPMode.Downlink && inboundPort <= 65535
  def outboundUdpEnabled = udpMode == UDPMode.Uplink && outboundPort <= 65535
  def outboundTcpEnabled = udpMode == UDPMode.TCPUplink && outboundPort <= 65535

  private def perhapsUpload() {
    startService(AndroidDirUpload.createIntent(this))
  }

  private def startNewDroneshare() {
    if (dshareUseNew) {
      if (dshareUsername.isEmpty)
        errorMessage = Some("Invalid droneshare username")
      else if (dsharePassword.size < 1)
        errorMessage = Some("Droneshare password too short")
      else {
        //val host = "localhost"
        warn("Creating droneshare link")
        val host = APIConstants.DEFAULT_SERVER
        droneshare = Some(LiveUploader.create(system,
          APIProxyActor.LoginMsg(dshareUsername, dsharePassword, None), host, dshareServerControl))
      }
    } else {
      warn("User doesn't want droneshare")
    }
  }

  /**
   * Init operations that can only proceed after the vehicle model is up
   */
  private def postVehicleInit() {
    speaker = Some(system.actorOf(Props(new Speaker(this, vehicle.get)), "speaker"))

    startNewDroneshare()

    if (runWebserver) {
      warn("Starting web server")
      val gcs = new TempGCSModel(vehicle.get)
      val adapter = new GCSAdapter(gcs)
      webServer = Some(system.actorOf(Props(new AndroidWebserver(this, adapter, !allowOtherHosts))))
    }

    val dumpSerialRx = false
    if (dumpSerialRx) {
      info("Starting packet log")

      // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
      system.actorOf(Props(new LogIncomingMavlink(vehicle.get.targetSystem,
        if (dumpSerialRx)
          LogIncomingMavlink.allowDefault
        else
          LogIncomingMavlink.allowNothing)), "ardlog")
    }

    if (PebbleClient.hasPebble(this))
      pebbleListener = Some(system.actorOf(Props(new PebbleVehicleListener(this)), "pebble"))

    setLogging(true)
    serialAttachToExisting()
    startUDP()
    // We now do this only on user input
    // connectToDevices()

    // If preferences change, automatically toggle logging as needed
    val handlers = Seq("log_to_file" -> setLoggingOn _,
      "udp_mode" -> startUDP _, "outbound_udp_host" -> startUDP _, "inbound_port" -> startUDP _, "outbound_port" -> startUDP _)
    prefListeners = prefListeners ++ handlers.map { p => registerOnPreferenceChanged(p._1)(p._2) }

    info("Done starting service")
  }

  override def onCreate() {
    super.onCreate()

    info("Creating service")

    // Force handler creation
    val h = handler

    initSpeech()

    // Not really ideal - but good enough for now
    ParameterDocFile.cacheDir = Some(getFilesDir)
    ThreadTools.start("docupdate")(ParameterDocFile.updateParamDocs)

    // Send any previously spooled files
    perhapsUpload()

    // Find out when the device goes away
    registerReceiver(disconnectReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))

    val startFlightLead = false
    if (startFlightLead) {
      info("Starting flight-lead")

      val flightSysId = 1 // FlightLead.systemId // FIXME, really should be 2, but we pretend to be a real plane for now

      // Create flightlead actors
      // If you want logging uncomment the following line
      // Akka.actorOf(Props(new LogIncomingMavlink(VehicleSimulator.systemId)), "hglog")
      // For testing I pretend to be a real arduplane (id 1)
      system.actorOf(Props(new FlightLead(flightSysId)), "lead")
      val stream = getAssets().open("testdata.igc")
      system.actorOf(Props(new IGCPublisher(stream)), "igcpub")

      // Watch for failures
      // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), flightSysId)
    }

    val vactor = system.actorOf(Props {
      val a = new VehicleModel with EventBusVehicleReceiver with SendsMavlinkToEventbus
      a.useRequestById = !useOldArducopter
      vehicle = Some(a)

      postVehicleInit()
      a
    }, "vmon")

    // Use a sync operation because this 
    // vactor.ask(GetInstance)(5 seconds)

  }

  def startUDP() {
    val wasOn = udp.map { u =>
      info("Shutting down old UDP daemon")
      u ! PoisonPill
      true
    }.getOrElse(false)

    val vehicleId = 1 // FIXME support multiple vehicles for UDP

    udp = if (outboundUdpEnabled) {
      info("Creating outbound UDP port")
      val a = system.actorOf(Props(new MavlinkUDP(destHostName = Some(outboundUdpHost), destPortNumber = Some(outboundPort))), "mavudp")

      // Anything from the ardupilot, forward it to the controller app
      MavlinkEventBus.subscribe(a, vehicleId)

      FlurryAgent.logEvent("udp_outbound")
      Some(a)
    } else if (inboundUdpEnabled) {
      // Let aircraft port
      info("Creating inbound UDP port")
      val a = system.actorOf(Props(new MavlinkUDP(localPortNumber = Some(inboundPort))), "mavudp")

      // Send our control packets to this UDP link
      MavlinkEventBus.subscribe(a, vehicleId)

      FlurryAgent.logEvent("udp_inbound")
      Some(a)
    } else if (outboundTcpEnabled) {
      // Let aircraft port
      info("Creating outbound TCP port")
      val a = system.actorOf(Props(MavlinkTCP.connect(outboundUdpHost, outboundPort)), "mavtcp")

      // Send our control packets to this UDP link
      MavlinkEventBus.subscribe(a, vehicleId)

      FlurryAgent.logEvent("tcp_outbound")
      Some(a)
    } else {
      info("No UDP port enabled")
      None
    }

    // We don't consider outbound udp high value, if there is a serial port connected it will need to keep us awake
    if (udp.isDefined && !outboundUdpEnabled)
      startHighValue()
    else if (wasOn)
      stopHighValue()
  }

  private def setLoggingOn() = setLogging(true)

  private def setLogging(enable: Boolean) {
    // Generate log files mission control would understand
    if (loggingEnabled && enable) {
      // If already logging ignore
      if (!logger.isDefined)
        logDirectory.foreach { d =>
          try {
            val logfile = LogBinaryMavlink.getFilename(d)
            val l = system.actorOf(Props(LogBinaryMavlink.create(!loggingKeepBoring, logfile)), "gclog")
            MavlinkEventBus.subscribe(l, -1)
            logger = Some(l)
          } catch {
            case ex: Exception =>
              //BugSenseHandler.sendExceptionMessage("sdwrite", "exception", ex)
              error("Can't access sdcard")
              logger = None
          }
        }
    } else
      // Shut down any existing loggers
      logger.foreach { l =>
        l ! PoisonPill

        // Crufty way to upload any generated files
        while (!l.isTerminated)
          Thread.sleep(1000)
        perhapsUpload()

        logger = None
      }
  }

  def setFollowMe(b: Boolean) {
    debug("Setting follow: " + b)
    if (b && follower.map(_.isTerminated).getOrElse(true) && FollowMe.isAvailable(this))
      vehicle.foreach { v =>
        follower = Some(system.actorOf(Props(new FollowMe(this, v)), "follow"))
      }

    if (!b) {
      follower.foreach(_ ! PoisonPill)
      follower = None
    }
  }

  protected def onBluetoothConnect(in: InputStream, outs: OutputStream) {
    info("Starting bluetooth")
    assert(outs != null)
    assert(in != null)
    val out = new BufferedOutputStream(outs, 512)

    //port.simulateUnreliable = true

    btInputStream = Some(in)
    // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
    val mavSerial = system.actorOf(Props(new MavlinkStream(out, in)), "btrx")
    btStream = Some(mavSerial)

    // Anything coming from the controller app, forward it to the serial port
    MavlinkEventBus.subscribe(mavSerial, groundControlId)

    // Also send anything from our active agent to the serial port
    MavlinkEventBus.subscribe(mavSerial, VehicleSimulator.andropilotId)

    // Watch for failures - not needed , we watch in the activity with MyVehicleModel
    // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), arduPilotId)

    FlurryAgent.logEvent("bt_attached")
    startHighValue()
  }

  protected def onBluetoothDisconnect() {
    btDetached()
  }

  def forceBluetoothDisconnect() {
    info("Force stopping bluetooth")
    btInputStream.foreach(_.close())
  }

  /**
   * Connect to any serial devices which were present at boot
   */
  def serialAttachToExisting() {
    val justcreated = AndroidSerial.getDevices.foreach { sdev =>

      info(s"Connecting to existing serial $sdev")
      serialAttached(sdev)
    }
  }

  def serialAttached(sdev: UsbDevice) {
    val i = serial.size // Number based on what ports we've seen

    info(s"Starting serial $i $sdev")
    errorMessage = None

    val baudRate = if (AndroidSerial.isTelemetry(sdev))
      baudWireless
    else
      baudDirect

    try {
      val sysIdOverride = if (i != 0)
        Some(i + 1) // Renumber vehicles on later interfaces as 2, 3, etc...
      else
        None

      val mavSerial = system.actorOf(Props(MavlinkAndroid.create(sdev, baudRate, sysIdOverride)), "serrx-" + i)

      // Anything coming from the controller app, forward it to the serial port
      MavlinkEventBus.subscribe(mavSerial, groundControlId)

      // Also send anything from our active agent to the serial port (FIXME, only send stuff destined for that interface)
      MavlinkEventBus.subscribe(mavSerial, VehicleSimulator.andropilotId)

      // Watch for failures - not needed , we watch in the activity with MyVehicleModel
      // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), arduPilotId)

      FlurryAgent.logEvent("serial_attached")

      serial += (sdev.getDeviceId -> mavSerial)

      startHighValue()
    } catch {
      case ex: NoAcquirePortException =>
        error("Can't acquire port")
        errorMessage = Some("Some other application has crashed without releasing the USB port.")
        usageEvent("serial_error", "message" -> ex.getMessage)

      case ex: IOException =>
        error("Error opening port: " + ex.getMessage)
        usageEvent("serial_error", "message" -> ex.getMessage)
    }
  }

  /**
   * We are now doing something important - don't kill us just because the activity goes away
   */
  private def startHighValue() {
    if (isConnected) {
      // The service will now want a way to override our service lifecycle
      warn("Manually starting service - need to stop it somewhere...")
      startService(new Intent(this, classOf[AndropilotService]))

      beginTimedEvent("high_value")

      // We are doing something important now - please don't kill us
      requestForeground()

      if (stayAwakeEnabled)
        wakeLock.acquire()

      vehicle.foreach(_.self ! OnInterfaceChanged(true))
    }
  }

  private def stopHighValue() {
    warn("In stopHighValue")
    if (!isConnected) {
      vehicle.foreach(_.self ! OnInterfaceChanged(false))

      endTimedEvent("high_value")

      if (wakeLock.isHeld)
        wakeLock.release()
      stopForeground(true) // Get rid of our notification

      // Finish any log files
      setLogging(false)

      warn("Stopping our service, because no serial means not useful...")
      stopSelf()
    }
  }

  private def serialDetached(id: Int) {
    warn(s"In serialDetached, id=$id")
    serial.remove(id).foreach { a =>
      warn("dettaching one serial device")

      a ! PoisonPill
    }
    stopHighValue()
  }

  private def serialDetachAll() {
    warn("In serialDetachAll")
    serial.keys.toSeq.foreach { a =>
      serialDetached(a)
    }
  }

  private def btDetached() {
    btStream.foreach { a =>
      a ! PoisonPill

      btStream = None
    }
    stopHighValue()
  }

  override def onDestroy() {
    warn("in onDestroy ******************************")

    setLogging(false)

    pebbleListener.foreach(_ ! PoisonPill)
    pebbleListener = None

    setFollowMe(false)
    speaker.foreach(_ ! PoisonPill)
    speaker = None
    prefListeners.foreach(unregisterOnPreferenceChanged)
    prefListeners = Seq()
    udp.foreach(_ ! PoisonPill)
    udp = None
    webServer.foreach(_ ! PoisonPill)
    webServer = None
    droneshare.foreach(_ ! PoisonPill)
    droneshare = None
    serialDetachAll()
    unregisterReceiver(disconnectReceiver)
    btDetached()

    destroySpeech()

    MockAkka.shutdown()
    warn("done onDestroy ******************************")
    super.onDestroy()
  }

  private def requestForeground() {
    val notificationIntent = new Intent(this, classOf[MainActivity])
    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

    val notification = new NotificationCompat.Builder(this)
      .setContentTitle(S(R.string.app_name))
      .setContentText(S(R.string.receiving_mavlink))
      .setSmallIcon(R.drawable.icon)
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .getNotification() // Don't use .build, it isn't in rev12

    startForeground(NotificationIds.vehicleConnectedId, notification)
  }

}

