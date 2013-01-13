package com.geeksville.andropilot

import akka.actor._
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
import com.typesafe.config.ConfigFactory

class AndropilotService extends Service with AndroidLogger {

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

  def getAkkaConfig() = {
    val overrides = ConfigFactory.defaultOverrides()
    val refStr = assetToString("reference.conf")
    info(refStr)
    val refConfig = ConfigFactory.parseString(refStr)
    val appConfig = ConfigFactory.parseString(assetToString("application.conf"))

    ConfigFactory.load(overrides.withFallback(appConfig.withFallback(refConfig)))
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int) = {
    info("Received start id " + startId + ": " + intent)

    /**
     * Our global akka system (use a name convention similar to playframework)
     */
    val Akka = ActorSystem("flight", getAkkaConfig)

    val startFlightLead = true
    if (startFlightLead) {
      // Create flightlead actors
      // If you want logging uncomment the following line
      // Akka.actorOf(Props(new LogIncomingMavlink(VehicleSimulator.systemId)), "hglog")
      Akka.actorOf(Props[FlightLead], "lead")
      val stream = getAssets().open("testdata.igc")
      Akka.actorOf(Props(new IGCPublisher(stream)), "igcpub")

      // Watch for failures
      MavlinkEventBus.subscribe(Akka.actorOf(Props[HeartbeatMonitor]), FlightLead.systemId)
    }

    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    Service.START_STICKY
  }
}