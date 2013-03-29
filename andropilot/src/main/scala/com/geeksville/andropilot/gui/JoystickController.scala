package com.geeksville.andropilot.gui

import android.view.MotionEvent
import android.view.InputDevice
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import android.app.Activity
import android.view.KeyEvent
import com.geeksville.andropilot.service.AndroServiceClient
import com.geeksville.flight.DoSetMode

/**
 * Provides joystick control either through hardware or a virtual screen joystick (not yet implemented)
 *
 * Implicitly set FBW when either of the sticks are moved (FIXME - not implemented)
 * Button Y = RTL
 * Button L1 = toggle fence on/off
 * Button R1 = cycle through favorite modes
 */
trait JoystickController extends Activity with AndroidLogger with AndroServiceClient {

  private val debugOutput = false

  override def onGenericMotionEvent(ev: MotionEvent) = {
    val isJoystick = ((ev.getSource & InputDevice.SOURCE_JOYSTICK) != 0) || ((ev.getSource & InputDevice.SOURCE_GAMEPAD) != 0)
    warn("Received %s from %s, action %s".format(ev, ev.getSource, ev.getAction))

    val devId = ev.getDeviceId
    if (isJoystick && devId != 0) {

      val dev = InputDevice.getDevice(devId)

      if (debugOutput) {
        val vibrator = dev.getVibrator
        debug("Name: " + dev.getName)
        debug("Has vibe: " + vibrator)
        dev.getMotionRanges.asScala.foreach { r =>
          debug("Axis %s(%s), %s, flat %s, fuzz %s, min %s, max %s".format(r.getAxis, MotionEvent.axisToString(r.getAxis), ev.getAxisValue(r.getAxis), r.getFlat, r.getFuzz, r.getMin, r.getMax))
        }

        debug("History size: " + ev.getHistorySize)
        debug("Buttons: " + ev.getButtonState)
      }

      // -1 is up and to the left
      val throttle = ev.getAxisValue(MotionEvent.AXIS_Y)
      val rudder = ev.getAxisValue(MotionEvent.AXIS_X)
      val elevator = ev.getAxisValue(MotionEvent.AXIS_RZ)
      val aileron = ev.getAxisValue(MotionEvent.AXIS_Z)

      /// Is the specified joystick axis moved away from center?
      def isMoved(axisNum: Int) = math.abs(ev.getAxisValue(axisNum)) <= dev.getMotionRange(axisNum).getFlat

    }

    isJoystick
  }

  override def onKeyDown(code: Int, ev: KeyEvent) = {
    debug("keydown " + code)
    if (KeyEvent.isGamepadButton(code)) {
      debug("press " + code)
      code match {
        case KeyEvent.KEYCODE_BUTTON_Y =>
          doRTL()
        case KeyEvent.KEYCODE_BUTTON_L1 =>
          doToggleFence()
        case KeyEvent.KEYCODE_BUTTON_R1 =>
          doNextMode()
      }
      true
    } else
      super.onKeyDown(code, ev)
  }

  private def doRTL() {
    myVehicle.foreach(_ ! DoSetMode("RTL"))
  }

  private def doToggleFence() {
  }

  private def doNextMode() {
  }

  /**
   * -1 for a channel means leave unchanged
   * 0 for a channel means do not override
   */
  private def setOverrides() {
    for {
      v <- myVehicle;
      curRc <- v.rcChannels
    } yield {}
  }
}