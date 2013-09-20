package com.geeksville.andropilot.service

import com.geeksville.akka.InstrumentedActor
import com.geeksville.andropilot.gui.PebbleClient
import android.content.Context
import com.geeksville.flight.MsgSysStatusChanged
import com.geeksville.flight.Location
import com.geeksville.util.Throttled
import com.geeksville.flight.MsgModeChanged

/**
 * Crudely use the pebble watch 'music' app to show flight data
 */
class PebbleVehicleListener(context: AndropilotService) extends InstrumentedActor {

  // Only update pebble every 10 secs (to save battery)
  private val throttle = new Throttled(10 * 1000)

  override def onReceive = {
    case l: Location =>
      perhapsUpdate()
    case MsgSysStatusChanged =>
      perhapsUpdate()
    case MsgModeChanged(_) =>
      updatePebble() // Show this change promptly
  }

  private def perhapsUpdate() {
    throttle { updatePebble _ }
  }

  private def updatePebble() {
    context.vehicle.foreach { v =>
      val bat = v.batteryVoltage.map { s => "Bat: %sV".format(s) }.getOrElse("")
      val loc = v.location.flatMap(_.alt).map { l => "Alt: %s meters".format(l.toInt) }.getOrElse("")
      val mode = v.currentMode
      PebbleClient.sendMusicToPebble(context, bat, loc, mode)
    }
  }

}