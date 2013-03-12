package com.geeksville.flight

import org.mavlink.messages.ardupilotmega.msg_param_value
import org.mavlink.messages.MAV_TYPE
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.mavlink.MavlinkConstants

/**
 * Parameter access, but only read-only (so it can work at log playback time)
 */
trait ParametersReadOnlyModel extends MavlinkConstants {
  lazy val paramDocs = (new ParameterDocFile).forVehicle(vehicleTypeName)

  private val planeCodeToModeMap = Map(0 -> "MANUAL", 1 -> "CIRCLE", 2 -> "STABILIZE",
    3 -> "TRAINING",
    5 -> "FBW_A", 6 -> "FBW_B", 10 -> "AUTO",
    11 -> "RTL", 12 -> "LOITER", 15 -> "GUIDED", 16 -> "INITIALIZING")

  private val copterCodeToModeMap = Map(
    0 -> "STABILIZE",
    1 -> "ACRO",
    2 -> "ALT_HOLD",
    3 -> "AUTO",
    4 -> "GUIDED",
    5 -> "LOITER",
    6 -> "RTL",
    7 -> "CIRCLE",
    8 -> "POSITION",
    9 -> "LAND",
    10 -> "OF_LOITER",
    11 -> "TOY_A",
    12 -> "TOY_B")

  private val planeModeToCodeMap = planeCodeToModeMap.map { case (k, v) => (v, k) }
  private val copterModeToCodeMap = copterCodeToModeMap.map { case (k, v) => (v, k) }

  /**
   * Wrap the raw message with clean accessors, when a value is set, apply the change to the target
   */
  class ROParamValue {
    var raw: Option[msg_param_value] = None

    /// The docs for this parameter (if we can find them)
    def docs = for { id <- getId; d <- paramDocs.get(id) } yield { d }

    def getId = raw.map(_.getParam_id)

    def getValue: Option[AnyVal] = raw.map { v =>
      val asfloat = v.param_value

      raw.get.param_type match {
        case MAVLINK_TYPE_FLOAT => asfloat: AnyVal
        case _ => asfloat.toInt: AnyVal
      }
    }

    def getInt = raw.map { v => v.param_value.toInt }

    /**
     * @return a nice human readable version of this value (decoding based on documentation if possible)
     */
    def asString = {
      (for {
        v <- raw
        doc <- docs
        asstr <- doc.decodeValue(v.param_value)
      } yield {
        Some(asstr)
      }).getOrElse(getValue.map(_.toString))
    }

    override def toString = (for { id <- getId; v <- getValue } yield { id + " = " + v }).getOrElse("undefined")
  }

  /// Either ArduCopter or ArduPlane etc... used to find appropriate parameter docs
  // FIXME handle other vehicle types
  def vehicleTypeName = if (isCopter) "ArduCopter" else "ArduPlane"

  def isPlane = vehicleType.map(_ == MAV_TYPE.MAV_TYPE_FIXED_WING).getOrElse(false)
  def isCopter = vehicleType.map { t =>
    (t == MAV_TYPE.MAV_TYPE_QUADROTOR) || (t == MAV_TYPE.MAV_TYPE_HELICOPTER) || (t == MAV_TYPE.MAV_TYPE_HEXAROTOR) || (t == MAV_TYPE.MAV_TYPE_OCTOROTOR)
  }.getOrElse(true)

  /// A MAV_TYPE vehicle code
  def vehicleType: Option[Int]

  protected def codeToModeMap = if (isPlane)
    planeCodeToModeMap
  else if (isCopter)
    copterCodeToModeMap
  else
    Map[Int, String]()

  protected def modeToCodeMap = if (isPlane)
    planeModeToCodeMap
  else if (isCopter)
    copterModeToCodeMap
  else
    Map[String, Int]()

  /// Convert a custom mode int into a human readable string
  def modeToString(modeCode: Int) = codeToModeMap.getOrElse(modeCode, "unknown")
}