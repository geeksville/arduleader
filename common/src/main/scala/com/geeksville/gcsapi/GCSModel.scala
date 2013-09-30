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