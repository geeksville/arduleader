package com.geeksville.andropilot.service

import scala.concurrent.duration._
import com.geeksville.flight.FlightLead
import com.geeksville.flight.IGCPublisher
import android.app._
import android.content.Intent
import com.ridemission.scandroid.AndroidLogger
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

trait ServiceAPI extends IBinder {
  def service: AndropilotService
}

class AndropilotService extends Service with AndroidLogger with FlurryService with AndropilotPrefs {
  val groundControlId = 255

  /**
   * If we are logging the file is here
   */
  var logfile: Option[File] = None
  private var logger: Option[LogBinaryMavlink] = None
  private var logPrefListener: Option[OnSharedPreferenceChangeListener] = None
  private var udpPrefListener: Option[OnSharedPreferenceChangeListener] = None

  var vehicle: Option[VehicleModel] = None

  private var serial: Option[MavlinkStream] = None
  private var udp: Option[InstrumentedActor] = None

  private var follower: Option[FollowMe] = None

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
   */
  private val disconnectReceiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
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

  def isSerialConnected = serial.isDefined
  def isFollowMe = follower.isDefined

  // Are we talking to a device at all?
  def isConnected = isSerialConnected || (udp.isDefined && inboundUdpEnabled)

  /**
   * A human readable description of our logging state
   */
  def serviceStatus = {
    val linkMsg = if (isSerialConnected)
      "USB Link"
    else
      udp.map { u =>
        udpMode + " " + NetTools.localIPAddresses.mkString(",")
      }.getOrElse("No Link")

    val logmsg = if (loggingEnabled)
      logfile.map { f => "Logging" }.getOrElse("No SD card")
    else
      "No logging"

    if (!isConnected)
      linkMsg
    else
      linkMsg + " " + logmsg
  }

  def inboundUdpEnabled = udpMode == UDPMode.Downlink
  def outboundUdpEnabled = udpMode == UDPMode.Uplink
  def outboundTcpEnabled = udpMode == UDPMode.TCPUplink

  override def onCreate() {
    super.onCreate()

    info("Creating service")

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

    val dumpSerialRx = false
    if (dumpSerialRx) {
      info("Starting packet log")

      // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
      MockAkka.actorOf(new LogIncomingMavlink(AndropilotService.arduPilotId,
        if (dumpSerialRx)
          LogIncomingMavlink.allowDefault
        else
          LogIncomingMavlink.allowNothing), "ardlog")
    }

    val actor = MockAkka.actorOf(new VehicleModel, "vmon")
    MavlinkEventBus.subscribe(actor, AndropilotService.arduPilotId)
    vehicle = Some(actor)

    setLogging()
    serialAttached()
    startUDP()

    // If preferences change, automatically toggle logging as needed
    logPrefListener = Some(registerOnPreferenceChanged("log_to_file")(setLogging _))
    udpPrefListener = Some(registerOnPreferenceChanged("udp_mode")(startUDP _))

    info("Done starting service")
  }

  def startUDP() {
    val wasOn = udp.map { u =>
      info("Shutting down old UDP daemon")
      u ! PoisonPill
      true
    }.getOrElse(false)

    udp = if (outboundUdpEnabled) {
      info("Creating outbound UDP port")
      val a = MockAkka.actorOf(new MavlinkUDP(destHostName = Some(outboundUdpHost), destPortNumber = Some(outboundPort)), "mavudp")

      // Anything from the ardupilot, forward it to the controller app
      MavlinkEventBus.subscribe(a, AndropilotService.arduPilotId)

      FlurryAgent.logEvent("udp_outbound")
      Some(a)
    } else if (inboundUdpEnabled) {
      // Let aircraft port
      info("Creating inbound UDP port")
      val a = MockAkka.actorOf(new MavlinkUDP(localPortNumber = Some(inboundPort)), "mavudp")

      // Send our control packets to this UDP link
      MavlinkEventBus.subscribe(a, VehicleSimulator.andropilotId)

      FlurryAgent.logEvent("udp_inbound")
      Some(a)
    } else if (outboundTcpEnabled) {
      // Let aircraft port
      info("Creating inbound UDP port")
      val a = MockAkka.actorOf(MavlinkTCP.connect(outboundUdpHost, 5760), "mavtcp")

      // Send our control packets to this UDP link
      MavlinkEventBus.subscribe(a, VehicleSimulator.andropilotId)

      FlurryAgent.logEvent("udp_inbound")
      Some(a)
    } else {
      info("No UDP port enabled")
      None
    }

    if (udp.isDefined)
      startHighValue()
    else if (wasOn)
      stopHighValue()
  }

  def setLogging() {
    // Generate log files mission control would understand
    if (loggingEnabled) {
      // If already logging ignore
      if (!logger.isDefined)
        AndropilotService.logDirectory.foreach { d =>
          logfile = Some(LogBinaryMavlink.getFilename(d))
          val l = MockAkka.actorOf(LogBinaryMavlink.create(logfile.get), "gclog")
          MavlinkEventBus.subscribe(l, -1)
          logger = Some(l)
        }
    } else
      // Shut down any existing loggers
      logger.foreach { l =>
        l ! PoisonPill
        logger = None
        logfile = None
      }
  }

  def setFollowMe(b: Boolean) {
    debug("Setting follow: " + b)
    if (b && follower.map(_.isTerminated).getOrElse(true))
      vehicle.foreach { v =>
        follower = Some(MockAkka.actorOf(new FollowMe(this, v), "follow"))
      }

    if (!b) {
      follower.foreach(_ ! PoisonPill)
      follower = None
    }
  }

  def serialAttached() {
    AndroidSerial.getDevice.map { sdev =>

      info("Starting serial")

      val baudRate = if (AndroidSerial.isTelemetry(sdev))
        baudWireless
      else
        baudDirect

      try {
        val port = MavlinkAndroid.create(baudRate)

        // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
        val mavSerial = MockAkka.actorOf(port, "serrx")
        serial = Some(mavSerial)

        // Anything coming from the controller app, forward it to the serial port
        MavlinkEventBus.subscribe(mavSerial, groundControlId)

        // Also send anything from our active agent to the serial port
        MavlinkEventBus.subscribe(mavSerial, VehicleSimulator.andropilotId)

        // Watch for failures - not needed , we watch in the activity with MyVehicleModel
        // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), arduPilotId)

        FlurryAgent.logEvent("serial_attached")
        startHighValue()

        // Find out when the device goes away
        registerReceiver(disconnectReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))
      } catch {
        case ex: NoAcquirePortException => // Some crummy android devices do not allow device to be acquired
          error("Can't acquire port")
      }
    }.getOrElse {
      warn("No serial port found by service")
    }
  }

  /**
   * We are now doing something important - don't kill us just because the activity goes away
   */
  private def startHighValue() {
    if (serial.isDefined || udp.isDefined) {
      // The service will now want a way to override our service lifecycle
      warn("Manually starting service - need to stop it somewhere...")
      startService(new Intent(this, classOf[AndropilotService]))

      FlurryAgent.logEvent("high_value", true)

      // We are doing something important now - please don't kill us
      requestForeground()

      if (stayAwakeEnabled)
        wakeLock.acquire()
    }
  }

  private def stopHighValue() {
    if (!serial.isDefined && !udp.isDefined) {
      FlurryAgent.endTimedEvent("high_value")

      if (wakeLock.isHeld)
        wakeLock.release()
      stopForeground(true) // Get rid of our notification

      warn("Stopping our service, because no serial means not useful...")
      stopSelf()
    }
  }

  private def serialDetached() {
    serial.foreach { a =>
      unregisterReceiver(disconnectReceiver)

      a ! PoisonPill

      serial = None
      stopHighValue()
    }
  }

  override def onDestroy() {
    warn("in onDestroy ******************************")
    setFollowMe(false)
    logPrefListener.foreach(unregisterOnPreferenceChanged)
    udpPrefListener.foreach(unregisterOnPreferenceChanged)
    udp.foreach(_ ! PoisonPill)
    udp = None
    serialDetached()
    MockAkka.shutdown()
    super.onDestroy()
  }

  private val ONGOING_NOTIFICATION = 1

  private def requestForeground() {
    val notificationIntent = new Intent(this, classOf[MainActivity])
    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

    val notification = new Notification.Builder(this)
      .setContentTitle("Andropilot")
      .setContentText("Receiving Mavlink")
      .setSmallIcon(R.drawable.icon)
      .setContentIntent(pendingIntent)
      .getNotification() // Don't use .build, it isn't in rev12

    startForeground(ONGOING_NOTIFICATION, notification)
  }

  private def initBluetooth() {
    /*
    Log.v("Communication Service", "Defaulting to Bluetooth");
    module = new BluetoothModule();
    // -------------------------------------
    // Ensure that when the service starts we allow the client to reconnect
    // After a first request, don't bother the user.
    module.setFirstTimeEnableDevice(true);
    module.setFirstTimeListDevices(true);
    * 
    * 			case MSG_CONNECT_DEVICE:
				if (msg.getData().containsKey(module.getDeviceAddressString())) {
					String address = msg.getData().getString(
							module.getDeviceAddressString());
					if (address.length() != 0) {
						connect(address);

					}

				}

				break;
				* 
				* then in connect()
				* 
					module.connect(device);

					// Start a receive thread.
					receive = new ReceiveThread();
					receive.start();

					notifyDeviceConnected();
    */
  }
}

object AndropilotService {
  val arduPilotId = 1

  /**
   * Where we should spool our output files (if allowed)
   */
  def sdDirectory = {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
      None
    else {
      val sdcard = Environment.getExternalStorageDirectory()
      if (!sdcard.exists())
        None
      else
        Some(new File(sdcard, "andropilot"))
    }
  }

  def logDirectory = sdDirectory.map { sd => new File(sd, "logs") }
  def paramDirectory = sdDirectory.map { sd => new File(sd, "param-files") }
}