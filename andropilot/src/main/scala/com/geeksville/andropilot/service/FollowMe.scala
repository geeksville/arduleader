package com.geeksville.andropilot.service

import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.VehicleMonitor
import android.content.Context
import android.location._
import android.os.Bundle
import com.geeksville.flight.DoGotoGuided
import com.geeksville.util.Throttled
import com.ridemission.scandroid.AndroidLogger

/**
 * Try to drive vehicle to stay near us
 */
class FollowMe(val context: Context, val v: VehicleMonitor) extends AndroidLogger {

  private val lm = context.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
  private val throttle = new Throttled(2000)

  /**
   * Add an android location listener
   */
  private val locListener = new LocationListener {
    override def onLocationChanged(location: Location) {
      throttle { () =>
        val myloc = new com.geeksville.flight.Location(location.getLatitude, location.getLongitude, location.getAltitude)

        // FIXME - support using magnetic heading to have vehicle be in _lead or follow_ of the user
        val msg = v.makeGuided(myloc)
        debug("Following " + myloc)
        v ! DoGotoGuided(msg)
      }
    }

    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 3, locListener)

    override def onProviderDisabled(provider: String) {}
    override def onProviderEnabled(provider: String) {}
    override def onStatusChanged(provider: String, status: Int, extras: Bundle) {}
  }

  def close() {
    lm.removeUpdates(locListener)
  }
}