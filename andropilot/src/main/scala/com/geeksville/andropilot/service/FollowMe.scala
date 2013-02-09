package com.geeksville.andropilot.service

import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.VehicleMonitor
import android.content.Context
import android.location._
import android.os.Bundle
import com.geeksville.flight.DoGotoGuided
import com.geeksville.util.Throttled
import com.ridemission.scandroid.AndroidLogger
import android.hardware._
import com.geeksville.util.MathTools
import com.ridemission.scandroid.UsesPreferences

/**
 * Try to drive vehicle to stay near us
 */
class FollowMe(val context: Context, val v: VehicleMonitor) extends AndroidLogger with UsesPreferences {

  private val throttle = new Throttled(2000)

  private var userGpsLoc: Option[Location] = None
  private var userBearing: Int = 0

  /**
   * Add an android location listener
   */
  private val locListener = new LocationListener {
    private val locManager = context.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 3, this)

    override def onLocationChanged(location: Location) {
      userGpsLoc = Some(location)
      updateTarget()
    }

    override def onProviderDisabled(provider: String) {}
    override def onProviderEnabled(provider: String) {}
    override def onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    def close() {
      locManager.removeUpdates(this)
    }
  }

  private val compassListener = new SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE).asInstanceOf[SensorManager]
    val sensorType = Sensor.TYPE_ORIENTATION

    /**
     * If we have our sensor this will be !None
     */
    val sensor = {
      val sensors = sensorManager.getSensorList(sensorType);
      if (sensors.size > 0)
        Some(sensors.get(0))
      else
        None
    }

    sensor.foreach { s =>
      sensorManager.registerListener(
        this,
        s,
        SensorManager.SENSOR_DELAY_NORMAL)
    }

    override def onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override def onSensorChanged(event: SensorEvent) {
      userBearing = event.values(0).toInt // Direction in degrees
      updateTarget()
    }

    def close() {
      sensor.foreach { s => sensorManager.unregisterListener(this) }
    }
  }

  def close() {
    locListener.close()
    compassListener.close()
  }

  /**
   * Update our goal position
   */
  private def updateTarget() {
    throttle { () =>
      for (loc <- userGpsLoc) yield {

        val followDistance = floatPreference("follow_distance", 0.0f)

        val (lat, lon) = MathTools.applyBearing(loc.getLatitude, loc.getLongitude, followDistance, userBearing)
        debug("Follow distance %s, bearing %s -> %s, %s".format(followDistance, userBearing, lat, lon))

        val myloc = new com.geeksville.flight.Location(lat, lon, loc.getAltitude)

        // FIXME - support using magnetic heading to have vehicle be in _lead or follow_ of the user
        val msg = v.makeGuided(myloc)
        debug("Following " + myloc)
        v ! DoGotoGuided(msg)
      }
    }
  }
}