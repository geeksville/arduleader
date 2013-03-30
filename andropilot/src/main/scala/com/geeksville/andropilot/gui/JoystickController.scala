package com.geeksville.andropilot.gui

import android.view.MotionEvent
import android.view.InputDevice
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import android.app.Activity
import android.view.KeyEvent
import com.geeksville.andropilot.service.AndroServiceClient
import com.geeksville.flight.DoSetMode
import com.geeksville.andropilot.service.AndropilotService
import com.geeksville.mavlink.SendYoungest

/**
 * Provides joystick control either through hardware or a virtual screen joystick (not yet implemented)
 *
 * Implicitly set FBW when either of the sticks are moved (FIXME - not implemented)
 * Button Y = RTL
 * Button L1 = toggle fence on/off
 * Button R1 = cycle through favorite modes
 * Button START = return all axis controls to regular RC transmitter
 */
trait JoystickController extends Activity with AndroidLogger with AndroServiceClient {

  private val debugOutput = false

  private var fenceChannel = 0

  /// Don't enable till we've read our params
  var sticksEnabled = false

  // Raw values from the stick
  // -1 is up and to the left for the gamepad
  private var throttle = 0f
  private var rudder = 0f
  private var aileron = 0f
  private var elevator = 0f

  /// Are we driving the four primary axes?
  private var isOverriding = false

  /**
   * @param reverse -1 or 1 depending on how vehicle is configured
   * @param stickBackwards if true then the android controller direction is swapped from how airplanes should fly
   */
  case class AxisInfo(reverse: Int = 1, min: Int = 1000, max: Int = 2000, trim: Int = 1500, stickBackwards: Boolean = false) {
    /// reverse joystick directions based on param reversal values (so we work like the main controller)
    /// @return a correct RC channel value for the specified joystick input
    def scale(raw: Float) = {
      // Convert to a range from -1 to 1 with all proper reverals done
      val stick = raw * reverse * (if (stickBackwards) -1 else 1)

      // Scale linearly on either side but be careful to leave the trim position in the middle
      if (stick >= 0)
        (stick * (max - trim) + trim).toInt
      else
        (-stick * (min - trim) + trim).toInt
    }
  }

  private var axis = Array(AxisInfo(), AxisInfo(), AxisInfo(), AxisInfo())

  /**
   * Super skanky - this hook is called from the activity when our parameters have arrived
   */
  protected def handleParameters() {
    myVehicle.foreach { v =>
      fenceChannel = v.fenceChannel

      def makeInfo(ch: Int, backwards: Boolean, trimDefault: Int = 1500) = {
        val r = AxisInfo(
          v.parametersById("RC" + ch + "_REV").getInt.getOrElse(1),
          v.parametersById("RC" + ch + "_MIN").getInt.getOrElse(1000),
          v.parametersById("RC" + ch + "_MAX").getInt.getOrElse(2000),
          v.parametersById("RC" + ch + "_TRIM").getInt.getOrElse(trimDefault),
          backwards)

        debug("Got axis: " + r)
        r
      }

      // For throttle ardupilot expects trim to be at the bottom of the range
      axis = Array(makeInfo(1, false), makeInfo(2, false), makeInfo(3, true, trimDefault = 1000), makeInfo(4, false))
      sticksEnabled = true
    }
  }

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

      throttle = ev.getAxisValue(MotionEvent.AXIS_Y)
      rudder = ev.getAxisValue(MotionEvent.AXIS_X)
      elevator = ev.getAxisValue(MotionEvent.AXIS_RZ)
      aileron = ev.getAxisValue(MotionEvent.AXIS_Z)

      /// Is the specified joystick axis moved away from center?
      def isMoved(axisNum: Int) = math.abs(ev.getAxisValue(axisNum)) > dev.getMotionRange(axisNum).getFlat

      // Possibly turn on overrides
      if (!isOverriding)
        isOverriding = isMoved(MotionEvent.AXIS_Y) || isMoved(MotionEvent.AXIS_X) || isMoved(MotionEvent.AXIS_RZ) || isMoved(MotionEvent.AXIS_Z)

      if (isOverriding && sticksEnabled)
        sendOverride()
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
        case KeyEvent.KEYCODE_BUTTON_START =>
          stopOverrides()
      }
      true
    } else
      super.onKeyDown(code, ev)
  }

  /**
   * When we get a service, make sure we turn off any stale overrides
   */
  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    stopOverrides()
  }

  private def doRTL() {
    myVehicle.foreach(_ ! DoSetMode("RTL"))
  }

  private def doToggleFence() {
  }

  private def doNextMode() {
  }

  private def stopOverrides() {
    warn("Stopping overrides")
    isOverriding = false
    myVehicle.foreach { v =>
      val p = v.rcChannelsOverride()
      v.sendMavlink(p)
      v.sendMavlink(p)
      v.sendMavlink(p)
    }
  }

  /**
   * -1 for a channel means leave unchanged
   * 0 for a channel means do not override
   */
  private def sendOverride() {
    //debug("sendOverride")
    for {
      v <- myVehicle;
      curRc <- v.rcChannels
    } yield {
      // FIXME - if min/max has not already been set by someone else, they may not have a regular controller - so just pick something
      // FIXME - use throttle as a virtual throttle - bump the desired throttle up or down as user presses it

      val p = v.rcChannelsOverride()
      p.chan1_raw = axis(0).scale(aileron)
      p.chan2_raw = axis(1).scale(elevator)
      p.chan3_raw = axis(2).scale(throttle)
      p.chan4_raw = axis(3).scale(rudder)

      // don't let override messages queue up in the output
      debug("Sending: " + p)
      v.sendMavlink(SendYoungest(p))
    }
  }
}