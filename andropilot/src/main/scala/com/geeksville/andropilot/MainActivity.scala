package com.geeksville.andropilot

import android.app.Activity
import _root_.android.os.Bundle
import android.content.Intent
import com.ridemission.scandroid.AndroidLogger
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

class MainActivity extends Activity with TypedActivity with AndroidLogger with FlurryActivity {

  implicit val context = this

  lazy val textView = findView(TR.textview)

  val serviceConnection = new ServiceConnection() {
    def onServiceConnected(className: ComponentName, service: IBinder) {
      debug("Service is bound")
    }

    def onServiceDisconnected(className: ComponentName) {
      error("Service disconnected")
    }
  };

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    warn("GooglePlayServices = " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this))

    setContentView(R.layout.main)

    // textView.setText("hello, world!")

    initMap()

    // Did the user just plug something in?
    Option(getIntent) match {
      case Some(intent) =>
        info("Received intent: " + intent)
        if (intent.getAction == "android.hardware.usb.action.USB_DEVICE_ATTACHED") {
          textView.setText("Device connected!  Starting service")
          startService()
        } else
          requestAccess()
      case None =>
        requestAccess()
    }
  }

  def initMap() {
    val mfrag = getFragmentManager.findFragmentById(R.id.map).asInstanceOf[MapFragment]
    // Could be null if no maps app
    Option(mfrag.getMap).foreach { map =>
      map.setMyLocationEnabled(true)

      val planeMarker = map.addMarker(new MarkerOptions()
        .position(new LatLng(0, 0))
        .draggable(false)
        .title("Airspeed = fixme"));
    }
  }

  def startService() {
    val intent = new Intent(this, classOf[AndropilotService])
    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
  }

  /** Ask for permission to access our device */
  def requestAccess() {
    AndroidSerial.getDevice match {
      case Some(device) =>
        AndroidSerial.requestAccess(device, { d =>
          textView.setText("Access granted!  Starting service")
          startService()
        }, { d =>
          textView.setText("User denied access to USB device")
        })
      case None =>
        textView.setText("Please attach 3dr telemetry device")
        startService() // FIXME, remove thi
    }

  }
}
