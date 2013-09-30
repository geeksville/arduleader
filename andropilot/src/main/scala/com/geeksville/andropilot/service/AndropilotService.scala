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
import com.geeksville.akka.PoisonPill
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

trait ServiceAPI extends IBinder {
  def service: AndropilotService
}

class AndropilotService extends Service with AndroidLogger
  with FlurryService with AndropilotPrefs with BluetoothConnection with UsesResources with UsesDirectories {
  val groundControlId = 255

  /**
   * If we are logging the file is here
   */
  var logfile: Option[File] = None
  private var logger: Option[LogBinaryMavlink] = None
  private var prefListeners: Seq[OnSharedPreferenceChangeListener] = Seq()

  var vehicle: Option[VehicleModel] = None

  private var serial: Seq[MavlinkStream] = Seq()
  private var udp: Option[InstrumentedActor] = None

  private var btInputStream: Option[InputStream] = None
  private var btStream: Option[MavlinkStream] = None

  private var follower: Option[FollowMe] = None

  private var uploader: Option[AndroidDirUpload] = None

  private var pebbleListener: Option[PebbleVehicleListener] = None

  implicit val context = this

  private lazy val wakeLock = getSystemService(Context.POWER_SERVICE).asInstanceOf[PowerManager].newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU")

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
   * FIXME - support multiple devices
   */
  private val disconnectReceiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      warn("In disconnect receiver")
      if (intent.getAction == UsbManager.ACTION_USB_DEVICE_DETACHED)
        serialDetached()
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
  def isConnected = isSerialConnected || udp.isDefined || isBluetoothConnected

  /**
   * A human readable description of our logging state
   */
  def serviceStatus = {
    val linkMsg = if (isSerialConnected)
      S(R.string.usb_link)
    else if (btStream.isDefined)
      S(R.string.bluetooth_link)
    else
      udp.map { u =>
        udpMode + " " + NetTools.localIPAddresses.mkString(",")
      }.getOrElse(S(R.string.no_link))

    val logmsg = if (loggingEnabled)
      logfile.map { f => S(R.string.logging) }.getOrElse(S(R.string.no_sd_card))
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

  override def onCreate() {
    super.onCreate()

    info("Creating service")

    // Send any previously spooled files
    perhapsUpload()

    val startFlightLead = false
    if (startFlightLead) {
      info("Starting flight-lead")

      val flightSysId = 1 // FlightLead.systemId // FIXME, really should be 2, but we pretend to be a real plane for now

      // Create flightlead actors
      // If you want logging uncomment the following line
      // Akka.actorOf(Props(new LogIncomingMavlink(VehicleSimulator.systemId)), "hglog")
      // For testing I pretend to be a real arduplane (id 1)
      MockAkka.actorOf(new FlightLead(flightSysId), "lead")
      val stream = getAssets().open("testdata.igc")
      MockAkka.actorOf(new IGCPublisher(stream), "igcpub")

      // Watch for failures
      // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), flightSysId)
    }

    val actor = MockAkka.actorOf(new VehicleModel, "vmon")

    // This lets the vehicle model receive messages from its vehicle...
    // FIXME - somehow direct by port id instead
    MavlinkEventBus.subscribe(actor, actor.targetSystem)
    vehicle = Some(actor)

    val dumpSerialRx = false
    if (dumpSerialRx) {
      info("Starting packet log")

      // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
      MockAkka.actorOf(new LogIncomingMavlink(actor.targetSystem,
        if (dumpSerialRx)
          LogIncomingMavlink.allowDefault
        else
          LogIncomingMavlink.allowNothing), "ardlog")
    }

    if (PebbleClient.hasPebble(this))
      pebbleListener = Some(MockAkka.actorOf(new PebbleVehicleListener(this), "pebble"))

    setLogging()
    serialAttached()
    startUDP()
    // We now do this only on user input
    // connectToDevices()

    // If preferences change, automatically toggle logging as needed
    val handlers = Seq("log_to_file" -> setLogging _,
      "udp_mode" -> startUDP _, "outbound_udp_host" -> startUDP _, "inbound_port" -> startUDP _, "outbound_port" -> startUDP _)
    prefListeners = prefListeners ++ handlers.map { p => registerOnPreferenceChanged(p._1)(p._2) }

    info("Done starting service")
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
      val a = MockAkka.actorOf(new MavlinkUDP(destHostName = Some(outboundUdpHost), destPortNumber = Some(outboundPort)), "mavudp")

      // Anything from the ardupilot, forward it to the controller app
      MavlinkEventBus.subscribe(a, vehicleId)

      FlurryAgent.logEvent("udp_outbound")
      Some(a)
    } else if (inboundUdpEnabled) {
      // Let aircraft port
      info("Creating inbound UDP port")
      val a = MockAkka.actorOf(new MavlinkUDP(localPortNumber = Some(inboundPort)), "mavudp")

      // Send our control packets to this UDP link
      MavlinkEventBus.subscribe(a, vehicleId)

      FlurryAgent.logEvent("udp_inbound")
      Some(a)
    } else if (outboundTcpEnabled) {
      // Let aircraft port
      info("Creating outbound TCP port")
      val a = MockAkka.actorOf(MavlinkTCP.connect(outboundUdpHost, outboundPort), "mavtcp")

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

  def setLogging() {
    // Generate log files mission control would understand
    if (loggingEnabled) {
      // If already logging ignore
      if (!logger.isDefined)
        logDirectory.foreach { d =>
          try {
            logfile = Some(LogBinaryMavlink.getFilename(d))
            val l = MockAkka.actorOf(LogBinaryMavlink.create(!loggingKeepBoring, logfile.get), "gclog")
            MavlinkEventBus.subscribe(l, -1)
            logger = Some(l)
          } catch {
            case ex: Exception =>
              //BugSenseHandler.sendExceptionMessage("sdwrite", "exception", ex)
              error("Can't access sdcard")
              logger = None
              logfile = None
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
        logfile = None
      }
  }

  def setFollowMe(b: Boolean) {
    debug("Setting follow: " + b)
    if (b && follower.map(_.isTerminated).getOrElse(true) && FollowMe.isAvailable(this))
      vehicle.foreach { v =>
        follower = Some(MockAkka.actorOf(new FollowMe(this, v), "follow"))
      }

    if (!b) {
      follower.foreach(_ ! PoisonPill)
      follower = None
    }
  }

  protected def onBluetoothConnect(in: InputStream, outs: OutputStream) {
    info("Starting bluetooth")
    val out = new BufferedOutputStream(outs, 512)

    val port = new MavlinkStream(out, in)

    btInputStream = Some(in)
    // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
    val mavSerial = MockAkka.actorOf(port, "btrx")
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

  def serialAttached() {
    serial = AndroidSerial.getDevices.zipWithIndex.flatMap {
      case (sdev, i) =>

        info(s"Starting serial $i")

        val baudRate = if (AndroidSerial.isTelemetry(sdev))
          baudWireless
        else
          baudDirect

        try {
          val sysIdOverride = if (i != 0)
            Some(i + 1) // Renumber vehicles on later interfaces as 2, 3, etc...
          else
            None

          val port = MavlinkAndroid.create(sdev, baudRate, sysIdOverride)

          // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
          val mavSerial = MockAkka.actorOf(port, "serrx")

          // Anything coming from the controller app, forward it to the serial port
          MavlinkEventBus.subscribe(mavSerial, groundControlId)

          // Also send anything from our active agent to the serial port (FIXME, only send stuff destined for that interface)
          MavlinkEventBus.subscribe(mavSerial, VehicleSimulator.andropilotId)

          // Watch for failures - not needed , we watch in the activity with MyVehicleModel
          // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), arduPilotId)

          FlurryAgent.logEvent("serial_attached")

          Some(mavSerial)
        } catch {
          case ex: NoAcquirePortException => // Some crummy android devices do not allow device to be acquired
            error("Can't acquire port")
            usageEvent("serial_error", "message" -> ex.getMessage)
            None
          case ex: IOException =>
            error("Error opening port: " + ex.getMessage)
            usageEvent("serial_error", "message" -> ex.getMessage)
            None
        }
    }.toSeq

    if (!serial.isEmpty) {
      startHighValue()

      // Find out when the device goes away
      // FIXME - work with multiple itnerfaces
      registerReceiver(disconnectReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))
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

      vehicle.foreach(_ ! OnInterfaceChanged(true))
    }
  }

  private def stopHighValue() {
    warn("In stopHighValue")
    if (!isConnected) {
      vehicle.foreach(_ ! OnInterfaceChanged(false))

      endTimedEvent("high_value")

      if (wakeLock.isHeld)
        wakeLock.release()
      stopForeground(true) // Get rid of our notification

      warn("Stopping our service, because no serial means not useful...")
      stopSelf()
    }
  }

  private def serialDetached() {
    warn("In serialDetached")
    serial.foreach { a =>
      warn("dettaching one serial device")
      unregisterReceiver(disconnectReceiver)

      a ! PoisonPill
    }
    serial = Seq()
    stopHighValue()
  }

  private def btDetached() {
    btStream.foreach { a =>
      a ! PoisonPill

      btStream = None
      stopHighValue()
    }
  }

  override def onDestroy() {
    warn("in onDestroy ******************************")

    pebbleListener.foreach(_ ! PoisonPill)
    pebbleListener = None

    setFollowMe(false)
    prefListeners.foreach(unregisterOnPreferenceChanged)
    prefListeners = Seq()
    udp.foreach(_ ! PoisonPill)
    udp = None
    serialDetached()
    btDetached()
    MockAkka.shutdown()
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

