package com.geeksville.flight

import org.mavlink.messages.ardupilotmega.msg_param_value
import org.mavlink.messages.MAV_TYPE
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.mavlink.MavlinkConstants
import com.ridemission.rest.ASJSON
import com.ridemission.rest.JObject
import org.mavlink.messages.MAV_AUTOPILOT

/**
 * Parameter access, but only read-only (so it can work at log playback time)
 */
trait ParametersReadOnlyModel extends MavlinkConstants { self: LiveOrPlaybackModel =>
  // Load this at start
  val docFile = new ParameterDocFile

  // Parse it later
  lazy val paramDocs = docFile.forVehicle(vehicleTypeName)

  /**
   * Wrap the raw message with clean accessors, when a value is set, apply the change to the target
   */
  class ROParamValue extends ASJSON {
    var raw: Option[msg_param_value] = None

    /// The docs for this parameter (if we can find them)
    def docs = for { id <- getId; d <- paramDocs.get(id) } yield { d }

    /**
     * Check this parameter to see if it is within the range expected by the documentation, if not in range return false
     */
    def isInRange = (for {
      range <- rangeOpt
      v <- raw
    } yield {
      v.param_value >= range._1 && v.param_value <= range._2
    }).getOrElse(true)

    def rangeOpt = for {
      doc <- docs
      range <- doc.range
    } yield {
      range
    }

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

    def asJSON = JObject("id" -> getId, "value" -> getValue, "as_str" -> asString)

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

  // Rover uses a different mode prefix than the other vehicle types
  def flightModePrefix = if (isRover) "MODE" else "FLTMODE"
}