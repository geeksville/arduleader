package com.geeksville.flight

import org.mavlink.messages.ardupilotmega.msg_mission_item
import org.mavlink.messages.MAV_CMD
import org.mavlink.messages.MAV_FRAME

/**
 * A wrapper for waypoints - to provide a higher level API
 */
case class Waypoint(val msg: msg_mission_item) {

  private val frameCodes = Map(
    MAV_FRAME.MAV_FRAME_GLOBAL -> "MSL",
    MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT -> "AGL")

  def commandStr = Waypoint.commandCodes.get(msg.command).getOrElse("cmd=" + msg.command)
  def frameStr = frameCodes.get(msg.frame).getOrElse("frame=" + msg.frame)

  def commandStr_=(s: String) {
    msg.command = Waypoint.commandToCodes(s)
  }

  def seq = msg.seq

  /// The magic home position
  def isHome = (msg.current != 2) && (msg.seq == 0)

  /// If the airplane is heading here
  def isCurrent = msg.current == 1

  def isMSL = msg.frame == MAV_FRAME.MAV_FRAME_GLOBAL

  // For virgin APMs with no GPS they will deliver a home WP with command of 255
  def isCommandValid = Waypoint.commandCodes.contains(msg.command) || msg.command == 255

  def altitude = msg.z

  def location = Location(msg.x, msg.y, Some(msg.z))

  /**
   * Should we show this waypoint on the map?
   */
  def isForMap = (msg.x != 0 || msg.y != 0) && isNavCommand

  //
  // Accessors for particular waypoint types
  //
  def isJump = msg.command == MAV_CMD.MAV_CMD_DO_JUMP
  def jumpSequence = msg.param1.toInt
  def loiterTime = msg.param1
  def loiterTurns = msg.param1

  def isNavCommand = !Waypoint.nonNavCommands.contains(msg.command)

  def isValidLatLng = msg.x != 0 || msg.y != 0

  /**
   * Allows access to params using a civilized index
   */
  def getParam(i: Int) = {
    i match {
      case 0 => msg.param1
      case 1 => msg.param2
      case 2 => msg.param3
      case 3 => msg.param4
    }
  }

  /**
   * Allows access to params using a civilized index
   */
  def setParam(i: Int, f: Float) {
    i match {
      case 0 => msg.param1 = f
      case 1 => msg.param2 = f
      case 2 => msg.param3 = f
      case 3 => msg.param4 = f
    }
  }

  /**
   * Just the type of the waypoint (RTL, LoiterN, etc...) or Home (as a special case)
   */
  def typeString = {
    if (isHome)
      "Home"
    else
      commandStr
  }

  /**
   * A short description of this waypoint
   */
  def shortString = {
    msg.command match {
      case MAV_CMD.MAV_CMD_DO_JUMP => "Jump to WP #%d".format(jumpSequence)
      case MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM => "Loiter (forever)"
      case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS => "Loiter (%.1f turns)".format(loiterTurns)
      case MAV_CMD.MAV_CMD_NAV_LOITER_TIME => "Loiter (%.1f seconds)".format(loiterTime)
      case MAV_CMD.MAV_CMD_NAV_TAKEOFF => Some("Take-off (MinPitch %.1f)".format(msg.param1))

      // FIXME - parse takeoff/land
      case _ =>
        typeString
    }
  }

  /**
   * Try to decode arguments into something understandable by a human
   */
  private def decodedArguments = {
    msg.command match {
      case MAV_CMD.MAV_CMD_DO_JUMP => Some("Jump to WP #%d".format(jumpSequence))
      case MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM => Some("forever")
      case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS => Some("%.1f turns".format(loiterTurns))
      case MAV_CMD.MAV_CMD_NAV_LOITER_TIME => Some("%.1f seconds".format(loiterTime))
      case MAV_CMD.MAV_CMD_NAV_TAKEOFF => Some("MinPitch %.1f".format(msg.param1))

      case _ =>
        None
    }
  }

  def numParamsUsed = {
    msg.command match {
      case MAV_CMD.MAV_CMD_DO_JUMP => 2
      case MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM => 1
      case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS => 1
      case MAV_CMD.MAV_CMD_NAV_LOITER_TIME => 1
      case MAV_CMD.MAV_CMD_NAV_TAKEOFF => 1
      case MAV_CMD.MAV_CMD_DO_SET_HOME => 1
      case MAV_CMD.MAV_CMD_CONDITION_DISTANCE => 1
      case MAV_CMD.MAV_CMD_CONDITION_DELAY => 1
      case MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT => 1
      case MAV_CMD.MAV_CMD_DO_CHANGE_SPEED => 3
      case MAV_CMD.MAV_CMD_DO_SET_SERVO => 2
      case MAV_CMD.MAV_CMD_DO_SET_RELAY => 2
      case MAV_CMD.MAV_CMD_DO_REPEAT_SERVO => 4
      case MAV_CMD.MAV_CMD_DO_REPEAT_RELAY => 3
      case MAV_CMD.MAV_CMD_DO_MOUNT_CONFIGURE => 4
      case MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST => 1
      case MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL => 3
      case MAV_CMD.MAV_CMD_DO_DIGICAM_CONTROL => 6
      case MAV_CMD.MAV_CMD_DO_DIGICAM_CONFIGURE => 7
      case _ =>
        0
    }
  }

  /**
   * Longer descriptiong (with arguments)
   */
  def longString = shortString + ": " + argumentsString

  /**
   * The arguments as a humang readable string
   */
  def argumentsString = {
    import msg._

    val altStr = "Altitude %sm (%s)".format(z, frameStr)

    val paramsStr = decodedArguments.map(", " + _).getOrElse {
      val params = Seq(param1, param2, param3, param4)
      val hasParams = params.find(_ != 0.0f).isDefined
      if (hasParams)
        ", params=%s".format(params.mkString(","))
      else
        ""
    }

    altStr + paramsStr
  }
}

object Waypoint {
  /**
   * Commands that should not show on a map
   */
  val nonNavCommands = Set(
    MAV_CMD.MAV_CMD_DO_JUMP,
    MAV_CMD.MAV_CMD_CONDITION_DISTANCE,
    MAV_CMD.MAV_CMD_CONDITION_DELAY,
    MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT,
    MAV_CMD.MAV_CMD_DO_CHANGE_SPEED,
    MAV_CMD.MAV_CMD_DO_SET_SERVO,
    MAV_CMD.MAV_CMD_DO_SET_RELAY,
    MAV_CMD.MAV_CMD_DO_REPEAT_SERVO,
    MAV_CMD.MAV_CMD_DO_REPEAT_RELAY,
    MAV_CMD.MAV_CMD_DO_CONTROL_VIDEO,
    MAV_CMD.MAV_CMD_DO_MOUNT_CONFIGURE,
    MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL,
    MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST,
    MAV_CMD.MAV_CMD_DO_DIGICAM_CONFIGURE,
    MAV_CMD.MAV_CMD_DO_DIGICAM_CONTROL)

  /**
   * We keep this separate so we can preserve order
   */
  private val commandCodesSeq = Seq(
    MAV_CMD.MAV_CMD_DO_JUMP -> "Jump",
    MAV_CMD.MAV_CMD_NAV_TAKEOFF -> "Takeoff",
    MAV_CMD.MAV_CMD_NAV_WAYPOINT -> "Waypoint", // Navigate to Waypoint
    MAV_CMD.MAV_CMD_NAV_LAND -> "Land", // LAND to Waypoint
    MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM -> "Loiter", // Loiter indefinitely
    MAV_CMD.MAV_CMD_NAV_LOITER_TURNS -> "LoiterN", // Loiter N Times
    MAV_CMD.MAV_CMD_NAV_LOITER_TIME -> "LoiterT",
    MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH -> "RTL",
    MAV_CMD.MAV_CMD_CONDITION_DISTANCE -> "CondDist",
    MAV_CMD.MAV_CMD_CONDITION_DELAY -> "CondDelay",
    MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT -> "CondAlt",
    MAV_CMD.MAV_CMD_DO_CHANGE_SPEED -> "ChangeSpd",
    MAV_CMD.MAV_CMD_DO_SET_SERVO -> "SetServo",
    MAV_CMD.MAV_CMD_DO_SET_RELAY -> "SetRelay",
    MAV_CMD.MAV_CMD_DO_REPEAT_SERVO -> "RepeatServo",
    MAV_CMD.MAV_CMD_DO_REPEAT_RELAY -> "RepeatRelay",
    MAV_CMD.MAV_CMD_DO_DIGICAM_CONFIGURE -> "DigiCfg",
    MAV_CMD.MAV_CMD_DO_DIGICAM_CONTROL -> "DigiCtrl",
    MAV_CMD.MAV_CMD_DO_MOUNT_CONFIGURE -> "MountCfg",
    MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST -> "SetCamTriggDist",
    MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL -> "MountCtrl")

  val commandCodes = Map(commandCodesSeq: _*)

  val commandToCodes = commandCodes.map { case (k, v) => (v, k) }

  val commandNames = commandCodesSeq.map(_._2).toArray
}