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
import com.geeksville.mavlink._
import com.geeksville.flight._

/**
 * Common client side goo for any GUI widget that binds to our service
 */
trait AndroServiceClient extends AndroidLogger {

  def context: Context

  protected var myVehicle: Option[VehicleMonitor] = None
  private var myVListener: Option[MyVehicleListener] = None
  protected var service: Option[AndropilotService] = None

  /**
   * Override if you need to do stuff once the connection is up
   */
  def onServiceConnected(s: AndropilotService) {
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

      AndroServiceClient.this.onServiceConnected(s)
    }

    def onServiceDisconnected(className: ComponentName) {
      error("Service disconnected")

      // No service anymore - don't need my actor
      stopVehicleMonitor()
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
  class MyVehicleListener(val v: VehicleMonitor) extends InstrumentedActor {

    /// On first position update zoom in on plane
    private var hasLocation = false

    val subscription = v.eventStream.subscribe(this, isInterested _)

    override def postStop() {
      v.eventStream.removeSubscription(subscription)
      super.postStop()
    }

    override def onReceive = onVehicleReceive
  }

  def onVehicleReceive: MyVehicleListener#Receiver

  private def stopVehicleMonitor() {
    myVListener.foreach { v =>
      debug("Shutting down VListener")
      v ! PoisonPill
      myVehicle = None
      myVListener = None
    }
  }

  protected def serviceOnPause() {
    stopVehicleMonitor()

    debug("Unbinding from service")
    context.unbindService(serviceConnection)
    service = None
  }

  protected def serviceOnResume() {
    debug("Binding to service")
    assert(context != null)
    val intent = new Intent(context, classOf[AndropilotService])
    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT)
  }
}