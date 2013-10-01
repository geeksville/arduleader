package com.geeksville.gcsapi

import com.geeksville.flight.VehicleModel

/**
 * The top level operations all GCSes must support
 */
trait GCSModel {
  /**
   * All the vehicles we know about
   */
  def vehicles: Seq[VehicleModel]

  // FIXME - add UI components/options
}

/**
 * A placeholder version of the GCS model until something more real exists
 */
class TempGCSModel(v: VehicleModel) extends GCSModel {
  val vehicles = Seq(v)
}