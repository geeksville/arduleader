package com.geeksville.andropilot.service
import android.content.Intent
import com.ridemission.scandroid.AndroidLogger
import com.ridemission.scandroid.AndroidUtil._
import android.widget._
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.content.Context
import com.google.android.gms.maps.model._
import com.geeksville.flight.VehicleModel
import com.geeksville.akka.MockAkka
import com.geeksville.util.ThreadTools._
import com.geeksville.akka.PoisonPill
import com.geeksville.akka.InstrumentedActor
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.andropilot.AndropilotPrefs

/**
 * Common client side goo for any GUI widget that binds to our service
 */
trait AndroServiceClient extends AndroidLogger with AndropilotPrefs {

  def context: Context

  protected var myVehicle: Option[VehicleModel] = None
  private var myVListener: Option[MyVehicleListener] = None
  protected var service: Option[AndropilotService] = None

  def isLowVolt = (for { v <- myVehicle; volt <- v.batteryVoltage } yield { v.hasHeartbeat && volt < minVoltage }).getOrElse(false)

  /// Apparently ardupane treats -1 for pct charge as 'no idea'
  def isLowBatPercent = (for { v <- myVehicle; pct <- v.batteryPercent } yield {
    v.hasHeartbeat && (pct < minBatPercent && pct >= -1.0)
  }).getOrElse(false)
  def isLowRssi = (for { v <- myVehicle; r <- v.radio } yield {
    val span = minRssiSpan

    v.hasHeartbeat && (r.rssi - span < r.noise || r.remrssi - span < r.remnoise)
  }).getOrElse(false)
  def isLowNumSats = (for { v <- myVehicle; n <- v.numSats } yield { v.hasHeartbeat && n < minNumSats }).getOrElse(false)
  def isWarning = isLowVolt || isLowBatPercent || isLowRssi || isLowNumSats

  /// Are we talking to at least one vehicle
  def isVehicleConnected = service.map(_.isConnected).getOrElse(false)

  /**
   * Override if you need to do stuff once the connection is up
   */
  protected def onServiceConnected(s: AndropilotService) {
  }

  private val serviceConnection = new ServiceConnection() {
    def onServiceConnected(className: ComponentName, serviceIn: IBinder) {
      val s = serviceIn.asInstanceOf[ServiceAPI].service
      service = Some(s)

      debug("Service is bound")

      // Don't use akka until the service is created
      s.vehicle.foreach { v =>
        val actor = MockAkka.actorOf(new MyVehicleListener(v), "lst")
        myVehicle = Some(v)
        myVListener = Some(actor)
      }

      import com.geeksville.andropilot.service.ServiceAPI
      import com.geeksville.andropilot.service.AndropilotService
      AndroServiceClient.this.onServiceConnected(s)
    }

    def onServiceDisconnected(className: ComponentName) {
      error("Service disconnected")

      // No service anymore - don't need my actor
      stopVehicleModel()
      service = None
    }
  }

  /**
   * Subclasses can override to filter the set of events that are delivered to them.
   * Though usually this check of partially defined functions will do the right thing...
   */
  protected def isInterested(evt: Any) = {
    val r = onVehicleReceive.isDefinedAt(evt)
    //if (!r) debug("%s is not interested in %s".format(this, evt))
    r
  }

  /**
   * Used to eavesdrop on location/state changes for our vehicle
   */
  class MyVehicleListener(val v: VehicleModel) extends InstrumentedActor {

    val subscription = v.eventStream.subscribe(this, isInterested _)

    override def postStop() {
      v.eventStream.removeSubscription(subscription)
      super.postStop()
    }

    override def onReceive = onVehicleReceive
  }

  def onVehicleReceive: InstrumentedActor.Receiver

  private def stopVehicleModel() {
    myVListener.foreach { v =>
      //debug("Shutting down VListener")
      v ! PoisonPill
      myVehicle = None
      myVListener = None
    }
  }

  protected def serviceOnPause() {
    stopVehicleModel()

    debug("Unbinding from service")
    try {
      context.unbindService(serviceConnection)
    } catch {
      case ex: IllegalArgumentException =>
        warn("Ignoring error on unbind") // If we get paused before the service has started, we could get this
    }
    service = None
  }

  protected def serviceOnResume() {
    //debug("Binding to service")
    assert(context != null)
    val intent = new Intent(context, classOf[AndropilotService])
    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT)
  }
}