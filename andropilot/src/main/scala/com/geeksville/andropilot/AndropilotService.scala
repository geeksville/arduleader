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

class AndropilotService extends Service with AndroidLogger with FlurryService {
  val groundControlId = 255
  val arduPilotId = 1

  implicit val context = this

  /**
   * Class for clients to access.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with
   * IPC.
   */
  private val binder = new Binder {
    def service = AndropilotService.this
  }

  override def onBind(intent: Intent) = binder

  /**
   * Read a file in assets
   */
  def assetToString(name: String) = Source.fromInputStream(getAssets().open(name)).
    getLines().mkString("\n")

  override def onCreate() {
    super.onCreate()

    info("Creating service")

    requestForeground()

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

    val startSerial = true
    if (startSerial) {
      info("Starting serial")

      val port = MavlinkAndroid.create(57600)
      val baudRate = 57600 // Use 115200 for a local connection, 57600 for 3dr telemetry

      // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
      val mavSerial = MockAkka.actorOf(port(), "serrx")

      // Anything coming from the controller app, forward it to the serial port
      MavlinkEventBus.subscribe(mavSerial, groundControlId)

      // Watch for failures - not needed , we watch in the activity with MyVehicleMonitor
      // MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), arduPilotId)
    }

    val dumpSerialRx = false
    if (dumpSerialRx) {
      info("Starting packet log")

      // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
      MockAkka.actorOf(new LogIncomingMavlink(arduPilotId,
        if (dumpSerialRx)
          LogIncomingMavlink.allowDefault
        else
          LogIncomingMavlink.allowNothing), "ardlog")
    }

    info("Done starting service")
  }

  override def onDestroy() {
    info("in onDestroy")
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
      .build()

    startForeground(ONGOING_NOTIFICATION, notification)
  }
}