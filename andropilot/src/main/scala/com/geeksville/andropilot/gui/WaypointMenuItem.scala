package com.geeksville.andropilot.gui

/**
 * A standard interface for any waypoint like object that can be edited
 */
trait WaypointMenuItem {
  /**
   * Can the user see/change auto continue
   */
  def isAllowAutocontinue = false
  def isAutocontinue = false
  def isAutocontinue_=(b: Boolean) { throw new Exception("Not implemented") }

  def isAltitudeEditable = false
  def altitude = 0.0
  def altitude_=(n: Double) { throw new Exception("Not implemented") }

  def numParams = 0
  def getParam(i: Int) = 0.0f
  def setParam(i: Int, n: Float) { throw new Exception("Not implemented") }

  def isAllowGoto = false
  def isAllowAdd = false
  def isAllowChangeType = false
  def isAllowDelete = false

  /// Waypoint type
  def typStr = "unknown"
  def typStr_=(s: String) { throw new Exception("Not implemented") }

  def isAllowContextMenu = true

  /**
   * Have vehicle go to this waypoint
   */
  def doGoto() { throw new Exception("Not yet implemented") }
  def doAdd() { throw new Exception("Not yet implemented") }
  def doDelete() { throw new Exception("Not yet implemented") }
}