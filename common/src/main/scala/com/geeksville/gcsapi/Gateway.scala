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
      val l = v.location
      println(s"Location is $l")
      l.asJson
    },
    "waypoints" -> { () =>
      val l = v.waypoints
      println(s"Waypoints are $l")
      l.asJson
    })
}

class GCSAdapter(gcs: GCSModel) extends SmallAdapter with HierarchicalAdapter {
  val vadapter = new VehicleAdapter(gcs.vehicles(0))

  def children = Map { "vehicle" -> vadapter }
}
