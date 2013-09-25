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

  /**
   * A set of modes that are selectable when the vehicle is flying in simple mode
   */
  protected val simpleFlightModes = Map("LAND" -> true, "RTL" -> false,
    "LOITER" -> false, "AUTO" -> true, "STABILIZE" -> false, "FBW_B" -> false)

  // FIXME - add support for a takeoff waypoint set
  protected val simpleGroundModes = Map("Arm" -> false, "LOITER" -> false, "AUTO" -> true, "STABILIZE" -> false, "Disarm" -> false)

  // Modes to allow while downloading wpts or params
  protected val initializingModes = Map("Disarm" -> false)

  /**
   * A mapping to RGB tuples (chosen to match the colors used by Tridge's python tool)
   */
  val modeToColorMap = Map(
    "MANUAL" -> (255, 0, 0),
    "AUTO" -> (0, 255, 0),
    "LOITER" -> (0, 0, 255),
    "FBWA" -> (255, 100, 0),
    "RTL" -> (255, 0, 100),
    "STABILIZE" -> (100, 255, 0),
    "LAND" -> (0, 255, 100),
    "STEERING" -> (100, 0, 255),
    "HOLD" -> (0, 100, 255),
    "ALT_HOLD" -> (255, 100, 100),
    "CIRCLE" -> (100, 255, 100),
    "GUIDED" -> (100, 100, 255),
    "ACRO" -> (255, 255, 0))

  private val roverCodeToModeMap = Map(
    0 -> "MANUAL", 2 -> "LEARNING", 3 -> "STEERING",
    4 -> "HOLD",
    10 -> "AUTO",
    11 -> "RTL", 15 -> "GUIDED", 16 -> "INITIALIZING")

  private val planeModeToCodeMap = planeCodeToModeMap.map { case (k, v) => (v, k) }
  private val copterModeToCodeMap = copterCodeToModeMap.map { case (k, v) => (v, k) }
  private val roverModeToCodeMap = roverCodeToModeMap.map { case (k, v) => (v, k) }
  /**
   * Wrap the raw message with clean accessors, when a value is set, apply the change to the target
   */
  class ROParamValue {
    var raw: Option[msg_param_value] = None

    /// The docs for this parameter (if we can find them)
    def docs = for { id <- getId; d <- paramDocs.get(id) } yield { d }

    /**
     * Check this parameter to see if it is within the range expected by the documentation, if not in range return false
     */
    def isInRange = (for {
      doc <- docs
      range <- doc.range
      v <- raw
    } yield {
      v.param_value >= range._1 && v.param_value <= range._2
    }).getOrElse(true)

    /**
     * Should this parameter be shared with others (or is it so instance specific it should not be passed around)
     */
    def isSharable =
      (for {
        doc <- docs
        share <- doc.share
      } yield {
        share.toLowerCase == "vehicle"
      }).getOrElse(false)

    def getId = raw.map(_.getParam_id)

    def getValue: Option[AnyVal] = raw.map { v =>
      val asfloat = v.param_value

      raw.get.param_type match {
        case MAVLINK_TYPE_FLOAT => asfloat: AnyVal
        case _ => asfloat.toInt: AnyVal
      }
    }

    def getInt = raw.map { v => v.param_value.toInt }
    def getFloat = raw.map { v => v.param_value }

    def getBoolean = getInt.map(_ != 0)

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
  def vehicleTypeName = if (isCopter)
    "ArduCopter"
  else if (isRover)
    "APMrover2"
  else
    "ArduPlane"

  def isPlane = vehicleType.map(_ == MAV_TYPE.MAV_TYPE_FIXED_WING).getOrElse(false)

  /**
   * Are we on a copter? or None if not sure
   */
  def isCopterOpt = vehicleType.map { t =>
    (t == MAV_TYPE.MAV_TYPE_QUADROTOR) || (t == MAV_TYPE.MAV_TYPE_HELICOPTER) || (t == MAV_TYPE.MAV_TYPE_HEXAROTOR) || (t == MAV_TYPE.MAV_TYPE_OCTOROTOR)
  }

  def isCopter = isCopterOpt.getOrElse(true)
  def isRover = vehicleType.map(_ == MAV_TYPE.MAV_TYPE_GROUND_ROVER).getOrElse(false)

  // Rover uses a different mode prefix than the other vehicle types
  def flightModePrefix = if (isRover) "MODE" else "FLTMODE"

  /// A MAV_TYPE vehicle code
  def vehicleType: Option[Int]

  protected def codeToModeMap = if (isPlane)
    planeCodeToModeMap
  else if (isCopter)
    copterCodeToModeMap
  else if (isRover)
    roverCodeToModeMap
  else
    Map[Int, String]()

  protected def modeToCodeMap = if (isPlane)
    planeModeToCodeMap
  else if (isCopter)
    copterModeToCodeMap
  else if (isRover)
    roverModeToCodeMap
  else
    Map[String, Int]()

  /// Convert a custom mode int into a human readable string
  def modeToString(modeCode: Int) = codeToModeMap.getOrElse(modeCode, "unknown")
}