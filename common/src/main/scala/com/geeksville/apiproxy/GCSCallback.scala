package com.geeksville.apiproxy

trait GCSCallback {
  /// Dear GCS, please send this packet
  def sendMavlink(b: Array[Byte])
}