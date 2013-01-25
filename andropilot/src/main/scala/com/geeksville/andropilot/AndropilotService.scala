package com.geeksville.andropilot

import scala.concurrent.duration._
import com.geeksville.flight.FlightLead
import com.geeksville.flight.IGCPublisher
import android.app._
import android.content.Intent
import com.ridemission.scandroid.AndroidLogger
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.mavlink.HeartbeatMonitor
import android.os._
import scala.io.Source
import com.geeksville.mavlink.LogIncomingMavlink
import com.geeksville.akka.MockAkka
import java.io.File
import com.geeksville.mavlink.LogBinaryMavlink
import com.geeksville.mavlink.MavlinkStream
import com.geeksville.akka.PoisonPill
import com.geeksville.flight.VehicleSimulator
import android.content.BroadcastReceiver
import android.content.Context
import android.hardware.usb.UsbManager
import android.content.IntentFilter
import com.ridemission.scandroid.UsesPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.geeksville.flight.VehicleMonitor

trait ServiceAPI extends IBinder {
  def service: AndropilotService
}

object AndropilotService {
  val arduPilotId = 1
}

class AndropilotService extends Service with AndroidLogger with FlurryService with UsesPreferences {
  val groundControlId = 255

  /**
   * If we are logging the file is here
   */
  var logfile: Option[File] = None
  var logger: Option[LogBinaryMavlink] = None
  var logPrefListener: Option[OnSharedPreferenceChangeListener] = None

  var vehicle: Option[VehicleMonitor] = None

  private var serial: Option[MavlinkStream] = None

  implicit val context = this

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

  /**
   * A human readable description of our logging state
   */
  def logmsg = if (loggingEnabled)
    logfile.map { f => "Logging to " + f }.getOrElse("No sdcard, logging suppressed...")
  else
    "Logging disabled"

  def loggingEnabled = boolPreference("log_to_file", false)
  def baudWireless = intPreference("baud_wireless", 57600)
  def baudDirect = intPreference("baud_direct", 115200)

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

    val actor = MockAkka.actorOf(new VehicleMonitor, "vmon")
    MavlinkEventBus.subscribe(actor, AndropilotService.arduPilotId)
    vehicle = Some(actor)

    setLogging()

    // If preferences change, automatically toggle logging as needed
    logPrefListener = Some(registerOnPreferenceChanged("log_to_file")(setLogging _))

    info("Done starting service")
  }

  def setLogging() {
    // Generate log files mission control would understand
    if (loggingEnabled) {
      // If already logging ignore
      if (!logger.isDefined)
        logDirectory.foreach { d =>
          logfile = Some(LogBinaryMavlink.getFilename(d))
          val l = MockAkka.actorOf(LogBinaryMavlink.create(logfile.get), "gclog")
          MavlinkEventBus.subscribe(l, AndropilotService.arduPilotId)
          MavlinkEventBus.subscribe(l, groundControlId)
          MavlinkEventBus.subscribe(l, VehicleSimulator.andropilotId)
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

  def serialAttached() {
    info("Starting serial")

    val baudRate = if (AndroidSerial.isTelemetry(AndroidSerial.getDevice.get))
      baudWireless
    else
      baudDirect
    val port = MavlinkAndroid.create(baudRate)

    // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
    val mavSerial = MockAkka.actorOf(port, "serrx")
    serial = Some(mavSerial)

    // Anything coming from the controller app, forward it to the serial port
    MavlinkEventBus.subscribe(mavSerial, groundControlId)

    // Also send anything from our active agent to the serial port
    MavlinkEventBus.subscribe(mavSerial, VehicleSimulator.andropilotId)

    // Watch for failures - not needed , we watch in the activity with MyVehicleMonitor
    // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), arduPilotId)

    // We are doing something important now - please don't kill us
    requestForeground()

    // We now want a way to override our service lifecycle
    warn("Manually starting service - need to stop it somewhere...")
    startService(new Intent(this, classOf[AndropilotService]))

    // Find out when the device goes away
    registerReceiver(disconnectReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))
  }

  private def serialDetached() {
    serial.foreach { a =>
      unregisterReceiver(disconnectReceiver)

      a ! PoisonPill
      stopForeground(true) // Get rid of our notification
      serial = None

      warn("Stopping our service, because no serial means not useful...")
      stopSelf()
    }
  }

  /**
   * Where we should spool our output files (if allowed)
   */
  def logDirectory = {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
      None
    else {
      val sdcard = Environment.getExternalStorageDirectory()
      if (!sdcard.exists())
        None
      else
        Some(new File(sdcard, "ardupilot"))
    }
  }

  override def onDestroy() {
    warn("in onDestroy")
    logPrefListener.foreach(unregisterOnPreferenceChanged)
    serialDetached()
    MockAkka.shutdown()
    super.onDestroy()
  }

  val ONGOING_NOTIFICATION = 1

  def requestForeground() {
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
}
