package com.geeksville.andropilot.service

import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.VehicleModel
import android.content.Context
import android.location._
import android.os.Bundle
import com.geeksville.flight.DoGotoGuided
import com.geeksville.util.Throttled
import com.ridemission.scandroid.AndroidLogger
import android.hardware._
import com.geeksville.util.MathTools
import com.ridemission.scandroid.UsesPreferences
import com.geeksville.andropilot.AndropilotPrefs
import com.geeksville.flight.MsgModeChanged
import com.geeksville.akka.PoisonPill
import com.geeksville.mavlink.MsgHeartbeatLost

/**
 * Try to drive vehicle to stay near us
 */
class FollowMe(val context: Context, val v: VehicleModel) extends InstrumentedActor with AndroidLogger with AndropilotPrefs {

  private val throttle = new Throttled(2000)

  private var userGpsLoc: Option[Location] = None
  private var orientation = Array(0.0f, 0.0f, 0.0f)

  /**
   * If we see the mode change by someone who isn't us we assume the user wants to exit follow me mode
   */
  private val subscription = v.eventStream.subscribe(this)
  private val startTime = System.currentTimeMillis

  /**
   * Add an android location listener
   */
  private lazy val locListener = new LocationListener {
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

  private lazy val compassListener = new SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE).asInstanceOf[SensorManager]

    /**
     * If we have our sensor this will be !None
     */
    private val magSensor = Option(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
    private val accelSensor = Option(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))

    private var accelData: Option[Array[Float]] = None
    private var magData: Option[Array[Float]] = None

    magSensor.foreach { s => sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI) }
    accelSensor.foreach { s => sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI) }

    override def onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override def onSensorChanged(event: SensorEvent) {
      // userBearing = event.values(0).toInt // Direction in degrees
      val s = Some(event.sensor)
      if (s == magSensor)
        magData = Some(event.values)
      if (s == accelSensor)
        accelData = Some(event.values) // FIXME - it would have been better to just have two instances
      // of the sensor listener

      for (m <- magData; a <- accelData) yield {
        val r = new Array[Float](9)
        val i = new Array[Float](9)
        if (SensorManager.getRotationMatrix(r, i, a, m)) {
          SensorManager.getOrientation(r, orientation)

          updateTarget()
        }
      }
    }

    def close() {
      magSensor.foreach { s => sensorManager.unregisterListener(this, s) }
      accelSensor.foreach { s => sensorManager.unregisterListener(this, s) }
    }
  }

  override def postStop() {
    locListener.close()
    compassListener.close()
    v.eventStream.removeSubscription(subscription)
    super.postStop()
  }

  override def onReceive = {
    case MsgHeartbeatLost(_) =>
      error("Heartbeat lost - exit follow me")
      self ! PoisonPill

    case MsgModeChanged(m) =>
      // Ignore stale changes when we are starting up
      if ((System.currentTimeMillis - startTime) >= 2000) {
        if (m != "GUIDED") {
          error("Someone else changed modes - exit follow me")
          self ! PoisonPill
        }
      }
  }

  private var declinationDeg: Option[Float] = None

  def getDeclination = {
    if (!declinationDeg.isDefined)
      declinationDeg = userGpsLoc.map { loc =>
        val field = new GeomagneticField(loc.getLatitude.toFloat, loc.getLongitude.toFloat, loc.getAltitude.toFloat, loc.getTime)
        field.getDeclination
      }
    declinationDeg.getOrElse(0.0f)
  }

  /**
   * Update our goal position
   */
  private def updateTarget() {
    throttle { () =>
      for (loc <- userGpsLoc) yield {

        val bearing = MathTools.toDeg(orientation(0)) - getDeclination
        val pitch = MathTools.toDeg(-orientation(1))
        val roll = MathTools.toDeg(orientation(2))

        val closePitch = 45
        val farPitch = 20
        val clampedPitch = math.max(math.min(closePitch, pitch), farPitch)
        val distPercent = 1.0 - (clampedPitch - farPitch) / (closePitch - farPitch)
        val followDistance = distPercent * (maxDistance - minDistance) + minDistance
        val (lat, lon) = MathTools.applyBearing(loc.getLatitude, loc.getLongitude, followDistance, bearing.toInt)
        debug("Follow distance %s (%s), bearing %s/%s/%s -> %s, %s".format(
          followDistance, distPercent, bearing, pitch, roll, lat, lon))

        val myloc = new com.geeksville.flight.Location(lat, lon, Some(guideAlt))

        // FIXME - support using magnetic heading to have vehicle be in _lead or follow_ of the user
        val msg = v.makeGuided(myloc)
        debug("Following " + myloc)
        v ! DoGotoGuided(msg, false)
      }
    }
  }
}

object FollowMe {
  /**
   * Does this device have appropriate hardware
   */
  def isAvailable(context: Context) = {
    val locManager = context.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    locManager.getProvider(LocationManager.GPS_PROVIDER) != null && locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
  }
}
