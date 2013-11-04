package com.geeksville.gcsapi

import com.geeksville.flight.VehicleModel
import com.ridemission.rest.JsonConverters._
import com.ridemission.rest.JArray
import com.ridemission.rest.JNull
import com.geeksville.flight.DoGotoGuided
import com.geeksville.flight.DoSetMode

/**
 * This is the singleton used to access vehicle and GCS state from javascript or other languages.
 *
 * See SmallAPI for the design philosophy of this object.
 */
class VehicleAdapter(v: VehicleModel) extends SmallAdapter {
  override def getters = Map(
    "location" -> { () =>
      v.location.asJson
    },
    "parameters" -> { () =>
      v.parameters.asJson
    },
    "waypoints" -> { () =>
      v.waypoints.asJson
    },
    "gps_hdop" -> { () =>
      v.hdop.asJson
    },
    "has_heartbeat" -> { () => v.hasHeartbeat.asJson },
    "is_armed" -> { () => v.isArmed.asJson },
    "status_messages" -> { () => v.statusMessages.asJson },
    "rc_channels" -> { () =>
      v.rcChannels.asJson
    },
    "rc_connected" -> { () => v.hasRealRc.asJson },
    "current_mode" -> { () =>
      v.currentMode.asJson
    })

  override def methods = Map(

    "set_mode" -> { (args: Seq[Any]) =>
      val s = args(0).asInstanceOf[String]
      println(s"GCSAPI set mode to $s")
      v ! DoSetMode(s)
      JNull
    },

    "goto_guided" -> { (args: Seq[Any]) =>
      println(s"Got args $args: " + args.mkString(","))
      val lat = args(0).asInstanceOf[Double]
      val lon = args(1).asInstanceOf[Double]
      val alt = args(2).asInstanceOf[Double]

      val myloc = new com.geeksville.flight.Location(lat, lon, Some(alt))

      // FIXME - support using magnetic heading to have vehicle be in _lead or follow_ of the user
      val msg = v.makeGuided(myloc)
      v ! DoGotoGuided(msg, false)

      JNull
    })
}

class GCSAdapter(gcs: GCSModel) extends MapAdapter {
  val vadapters = gcs.vehicles.map(new VehicleAdapter(_))

  // FIXME - make vehicle map to the currently selected vehicle
  override def children = Map("vehicle" -> vadapters(0),
    "vehicles" -> new ArrayAdapter(vadapters))
}
