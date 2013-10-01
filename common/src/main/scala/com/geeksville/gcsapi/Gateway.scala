package com.geeksville.gcsapi

import com.geeksville.flight.VehicleModel
import com.ridemission.rest.JsonConverters._

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
    "waypoints" -> { () =>
      v.waypoints.asJson
    },
    "currentMode" -> { () =>
      v.currentMode.asJson
    })
}

class GCSAdapter(gcs: GCSModel) extends SmallAdapter with HierarchicalAdapter {
  val vadapter = new VehicleAdapter(gcs.vehicles(0))

  def children = Map { "vehicle" -> vadapter }
}
