package com.geeksville.andropilot.service

import com.geeksville.akka.InstrumentedActor
import android.content.Context
import com.geeksville.flight.MsgSysStatusChanged
import com.geeksville.flight.Location
import com.geeksville.util.Throttled
import com.geeksville.flight.MsgModeChanged
import com.geeksville.flight.VehicleListener
import com.ridemission.scandroid.AndroidLogger
import com.geeksville.mavlink.MsgHeartbeatLost

/**
 * Crudely use the pebble watch 'music' app to show flight data
 */
class PebbleVehicleListener(context: AndropilotService) extends VehicleListener(context.vehicle.get) with AndroidLogger {

  // Only update pebble every 10 secs (to save battery)
  private val throttle = new Throttled(10 * 1000)

  override def onReceive = {
    case l: Location =>
      perhapsUpdate()
    case MsgSysStatusChanged =>
      perhapsUpdate()
    case MsgHeartbeatLost(_) =>
      updatePebble()
    case MsgModeChanged(_) =>
      updatePebble() // Show this change promptly
  }

  private def perhapsUpdate() {
    throttle { updatePebble _ }
  }

  private def updatePebble() {
    context.vehicle.foreach { v =>
      val bat = v.batteryVoltage.map { s => "Bat: %1.2f V".format(s) }.getOrElse("")
      val loc = "Alt: %1.1f m".format(v.bestAltitude)
      val mode = v.currentModeOrStatus
      warn(s"Setting pebble $bat/$loc/$mode")
      PebbleClient.sendMusicToPebble(context, bat, loc, mode)
    }
  }

  override def postStop() {
    PebbleClient.sendMusicToPebble(context, "", "", "Exited")
    super.postStop()
  }
}