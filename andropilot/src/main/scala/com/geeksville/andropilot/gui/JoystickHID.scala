package com.geeksville.andropilot.gui

import android.view.MotionEvent
import android.view.InputDevice
import com.ridemission.scandroid._
import scala.collection.JavaConverters._
import com.geeksville.util.ThreadTools._
import android.app.Activity
import android.view.KeyEvent
import com.geeksville.andropilot.service.AndroServiceClient
import com.geeksville.flight.DoSetMode
import com.geeksville.andropilot.service.AndropilotService
import com.geeksville.mavlink.SendYoungest
import com.geeksville.akka.MockAkka
import scala.concurrent.duration._
import org.mavlink.messages.ardupilotmega.msg_rc_channels_override
import com.geeksville.andropilot.R
import android.content.ActivityNotFoundException
import android.os.Bundle
import akka.actor.Cancellable
import scala.concurrent.ExecutionContext

/**
 * Provides joystick control through a bluetooth joystick
 *
 * Implicitly set FBW when either of the sticks are moved (FIXME - not implemented)
 * Button R2 = RTL
 * Button L1 = toggle fence on/off
 * Button R1 = cycle through favorite modes
 * Button START = return all axis controls to regular RC transmitter
 */
trait JoystickHID extends JoystickController {

  /// We integrate this value over time
  private var throttleStickPos = 0f

  /// Is the throttle currently pressed by the user?
  private var throttleMoved = false

  private var throttleTimer: Option[Cancellable] = None

  private var oldHatX = 0.0f
  private var oldHatY = 0.0f

  override def onCreate(b: Bundle) {
    super.onCreate(b)

    // I think Nvidia shield might have hooked this to find apps that are joystick aware...
    InputDevice.getDeviceIds.foreach(InputDevice.getDevice)
  }

  override def onGenericMotionEvent(ev: MotionEvent) = {
    val isJoystick = ((ev.getSource & InputDevice.SOURCE_JOYSTICK) != 0) || ((ev.getSource & InputDevice.SOURCE_GAMEPAD) != 0)
    if (debugOutput)
      debug("Received %s from %s, action %s".format(ev, ev.getSource, ev.getAction))

    val devId = ev.getDeviceId
    if (isJoystick && devId != 0 && joystickAvailable) {
      val dev = InputDevice.getDevice(devId)
      if (dev != null) {
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

        /// Process a historical (or current) joystick record
        def processJoystick(historyPos: Int) {
          def getAxisValue(axis: Int) = if (historyPos < 0) ev.getAxisValue(axis) else ev.getHistoricalAxisValue(axis, historyPos)

          rudder = getAxisValue(MotionEvent.AXIS_X)
          elevator = getAxisValue(MotionEvent.AXIS_RZ)
          aileron = getAxisValue(MotionEvent.AXIS_Z)
          throttleStickPos = getAxisValue(MotionEvent.AXIS_Y)

          val hatx = getAxisValue(MotionEvent.AXIS_HAT_X)
          val haty = getAxisValue(MotionEvent.AXIS_HAT_Y)

          if (hatx != oldHatX)
            if (hatx < -0.5f)
              selectNextPage(false) // left
            else if (hatx > 0.5f)
              selectNextPage(true)

          if (haty != oldHatY)
            if (haty < -0.5f)
              doNextMode(false) // up
            else if (haty > 0.5f)
              doNextMode(true) // down

          oldHatX = hatx
          oldHatY = haty

          /// Is the specified joystick axis moved away from center?
          def isMoved(axisNum: Int) = math.abs(getAxisValue(axisNum)) > dev.getMotionRange(axisNum, ev.getSource).getFlat

          // Possibly turn on overrides
          throttleMoved = isMoved(MotionEvent.AXIS_Y)
          //debug("set throttle moved %s, because %s is outside %s".format(throttleMoved, getAxisValue(MotionEvent.AXIS_Y), dev.getMotionRange(MotionEvent.AXIS_Y, ev.getSource).getFlat))

          if (throttleMoved || isMoved(MotionEvent.AXIS_X) || isMoved(MotionEvent.AXIS_RZ) || isMoved(MotionEvent.AXIS_Z))
            startOverride()
        }

        (0 until ev.getHistorySize).foreach { i =>
          // Handle any prior state
          processJoystick(i)
        }

        // Handle the current state
        processJoystick(-1)

        if (throttleMoved && !throttleTimer.isDefined) // Prime the pump on the throttle timer if necessary
          applyThrottle()

        if (isOverriding)
          sendOverride()
      }
    }

    isJoystick
  }

  private def applyThrottle() {
    val scale = 0.05f // FIXME, make adjustable and/or exponential

    if (throttleMoved && isOverriding) {
      // We invert stick because the joystick uses -1 to mean top of travel, we want it to mean increase throttle
      val newval = throttle + -throttleStickPos * scale

      //debug("apply " + throttleStickPos + " to throttle " + newval)
      val newt = math.min(1.0f, math.max(0.0f, newval))

      if (newt != throttle) {
        throttle = newt
        sendOverride()
      }

      val system = MockAkka.system
      import system._

      // Schedule us to be invoked again in a little while
      throttleTimer = Some(system.scheduler.scheduleOnce(200 milliseconds)(applyThrottle))
    } else
      throttleTimer = None
  }

  protected def selectNextPage(toRight: Boolean)

  /**
   * d-pad keys seem to only be available here
   */
  override def dispatchKeyEvent(ev: KeyEvent) = {
    if (ev.getAction == KeyEvent.ACTION_DOWN) {
      // debug("dispatch " + ev.getKeyCode)

      ev.getKeyCode match {
        case KeyEvent.KEYCODE_DPAD_LEFT =>
          selectNextPage(false)
          true
        case KeyEvent.KEYCODE_DPAD_RIGHT =>
          selectNextPage(true)
          true
        case KeyEvent.KEYCODE_DPAD_UP =>
          doNextMode(false)
          true
        case KeyEvent.KEYCODE_DPAD_DOWN =>
          doNextMode(true)
          true
        case _ =>
          super.dispatchKeyEvent(ev)
      }
    } else
      try {
        super.dispatchKeyEvent(ev)
      } catch {
        case ex: ActivityNotFoundException =>
          toast("Google play services missing - maps won't work!")
          false
      }
  }

  override def onKeyDown(code: Int, ev: KeyEvent) = {
    // debug("keydown " + code)
    if (KeyEvent.isGamepadButton(code)) {
      // debug("press " + code)
      code match {
        case KeyEvent.KEYCODE_BUTTON_R2 =>
          doRTL()
          true
        case KeyEvent.KEYCODE_BUTTON_L1 =>
          doToggleFence()
          true
        case KeyEvent.KEYCODE_BUTTON_START =>
          service.foreach(_.speak(S(R.string.spk_joystick_off), true))
          toast("Joystick off")
          stopOverrides()
          true
        case x @ _ =>
          warn("Unknown key: " + x)
          super.onKeyDown(code, ev)
      }
    } else
      super.onKeyDown(code, ev)
  }

  private def doRTL() {
    myVehicle.foreach(_.self ! DoSetMode("RTL"))
  }

  protected def showSidebar(shown: Boolean)

  private def doNextMode(isNext: Boolean) {
    showSidebar(true)

    try {
      myVehicle.foreach { v =>
        val favoriteModes = (1 to 6).map { i =>
          val modeInt = v.getFlightMode(i).getOrElse(0)
          v.modeToString(modeInt)
        }

        if (!favoriteModes.isEmpty) {
          // If the current mode is not a 'favorite' we'll return -1 and just pick the first mode option
          val curPos = favoriteModes.indexOf(v.currentMode)

          // Wrap around if we fall off the table
          val newMode = if (isNext) {
            if (curPos < favoriteModes.size - 1)
              favoriteModes(curPos + 1)
            else
              favoriteModes.head
          } else {
            if (curPos > 0)
              favoriteModes(curPos - 1)
            else
              favoriteModes.last
          }

          v.self ! DoSetMode(newMode)
        }
      }
    } catch {
      case ex: NoSuchElementException =>
        error("Vehicle doesn't support FLTMODE")
    }
  }
}