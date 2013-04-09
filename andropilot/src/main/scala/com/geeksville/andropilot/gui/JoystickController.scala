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
import com.geeksville.akka.Cancellable
import com.geeksville.akka.MockAkka
import scala.concurrent.duration._
import org.mavlink.messages.ardupilotmega.msg_rc_channels_override
import com.geeksville.aspeech.TTSClient

/**
 * Provides joystick control either through hardware or a virtual screen joystick (not yet implemented)
 *
 * Implicitly set FBW when either of the sticks are moved (FIXME - not implemented)
 * Button R2 = RTL
 * Button L1 = toggle fence on/off
 * Button R1 = cycle through favorite modes
 * Button START = return all axis controls to regular RC transmitter
 */
trait JoystickController extends Activity with AndroidLogger with AndroServiceClient with TTSClient {

  private val debugOutput = false

  private var fenceChannel = 0

  /// Don't enable till we've read our params
  var sticksEnabled = false
  var hasParameters = false

  // Raw values from the stick
  // -1 is up and to the left for the gamepad
  private var rudder = 0f
  private var aileron = 0f
  private var elevator = 0f

  private val aileronScale = 0.8f
  private val elevatorScale = 0.8f
  private val rudderScale = 0.8f

  /// Are we driving the four primary axes?
  private var isOverriding = false

  /// 0 is 0 throttle 1.0f is full throttle
  private var throttle = 0f

  /// We integrate this value over time
  private var throttleStickPos = 0f

  /// Is the throttle currently pressed by the user?
  private var throttleMoved = false

  private var throttleTimer: Option[Cancellable] = None

  var fenceEnabled = false

  // We handle overrides of fence separately (so as to not change fence enable just because we switched to game pad)
  var fenceOverridden = false

  /// For cycling through modes with the up/down arrow
  private var favoriteModes = Seq[String]()

  /**
   * @param reverse -1 or 1 depending on how vehicle is configured
   * @param stickBackwards if true then the android controller direction is swapped from how airplanes should fly
   */
  case class AxisInfo(reverse: Int = 1, min: Int = 1000, max: Int = 2000, trim: Int = 1500, scaleVal: Float = 1.0f, stickBackwards: Boolean = false) {
    /// reverse joystick directions based on param reversal values (so we work like the main controller)
    /// @return a correct RC channel value for the specified joystick input
    def scale(raw: Float) = {
      // Convert to a range from -1 to 1 with all proper reversals done
      val stick = raw * reverse * (if (stickBackwards) -1 else 1) * scaleVal

      // Scale linearly on either side but be careful to leave the trim position in the middle
      if (stick >= 0)
        (stick * (max - trim) + trim).toInt
      else
        (-stick * (min - trim) + trim).toInt
    }

    /// throttle scales differently, it ignores trim, rather 0 maps to min and 1 maps to max
    def scaleThrottle(raw: Float) = {
      // Convert to a range from 0 to 1 with all proper reversals done
      val stick = raw * reverse * (if (stickBackwards) -1 else 1) * scaleVal

      (stick * (max - min) + min).toInt
    }

    /**
     * Given a servo usec value return a value between 0 and 1 (scaling and reversing)
     */
    def unscale(raw: Int) = {
      val r = (reverse * (raw.toFloat - min) / (max - min)) / scaleVal

      debug("Unscale " + raw + " to " + r)

      // Clamp to 0 to 1.0
      math.min(1.0f, math.max(0.0f, r))
    }
  }

  private var axis = Array(AxisInfo(), AxisInfo(), AxisInfo(), AxisInfo())

  /**
   * Super skanky - this hook is called from the activity when our parameters have arrived
   */
  protected def handleParameters() {
    hasParameters = true
  }

  /**
   * We wait to fetch our params until the first time the user moves the stick (so as to not change the behavior for non joystick devices)
   */
  private def getParameters() {
    myVehicle.foreach { v =>
      fenceChannel = v.fenceChannel

      def makeInfo(ch: Int, backwards: Boolean, scaleVal: Float = 1.0f, trimDefault: Int = 1500) = {
        val r = AxisInfo(
          v.parametersById("RC" + ch + "_REV").getInt.getOrElse(1),
          v.parametersById("RC" + ch + "_MIN").getInt.getOrElse(1000),
          v.parametersById("RC" + ch + "_MAX").getInt.getOrElse(2000),
          v.parametersById("RC" + ch + "_TRIM").getInt.getOrElse(trimDefault),
          scaleVal,
          backwards)

        debug("Got axis: " + r)
        r
      }

      favoriteModes = (1 to 6).map { i =>
        val modeInt = v.parametersById("FLTMODE" + i).getInt.getOrElse(0)
        v.modeToString(modeInt)
      }

      // Elevator is NOT reversed vs standard android gamepad (forward should get larger)
      axis = Array(makeInfo(1, false, scaleVal = aileronScale), makeInfo(2, false, scaleVal = elevatorScale), makeInfo(3, false, trimDefault = 1000),
        makeInfo(4, false, scaleVal = rudderScale))
      sticksEnabled = true

      // Tell the vehicle we are controlling it - FIXME - do this someplace better
      v.parametersById.get("SYSID_MYGCS").foreach(_.setValueNoAck(v.systemId))
      v.parametersById.get("SYSID_MYGCS").foreach(_.setValueNoAck(v.systemId))
      v.parametersById.get("SYSID_MYGCS").foreach(_.setValueNoAck(v.systemId))
    }
  }

  override def onGenericMotionEvent(ev: MotionEvent) = {
    val isJoystick = ((ev.getSource & InputDevice.SOURCE_JOYSTICK) != 0) || ((ev.getSource & InputDevice.SOURCE_GAMEPAD) != 0)
    warn("Received %s from %s, action %s".format(ev, ev.getSource, ev.getAction))

    val devId = ev.getDeviceId
    if (isJoystick && devId != 0 && hasParameters) {

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

      /// Process a historical (or current) joystick record
      def processJoystick(historyPos: Int) {
        def getAxisValue(axis: Int) = if (historyPos < 0) ev.getAxisValue(axis) else ev.getHistoricalAxisValue(axis, historyPos)

        rudder = getAxisValue(MotionEvent.AXIS_X)
        elevator = getAxisValue(MotionEvent.AXIS_RZ)
        aileron = getAxisValue(MotionEvent.AXIS_Z)
        throttleStickPos = getAxisValue(MotionEvent.AXIS_Y)

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

      if (isOverriding && sticksEnabled)
        sendOverride()
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

      // Schedule us to be invoked again in a little while
      throttleTimer = Some(MockAkka.scheduler.scheduleOnce(200 milliseconds)(applyThrottle _))
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
      super.dispatchKeyEvent(ev)
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
          speak("Joystick off")
          stopOverrides()
          true
        case x @ _ =>
          warn("Unknown key: " + x)
          super.onKeyDown(code, ev)
      }
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
    debug("In toggle fence")
    if (fenceChannel != 0) {
      fenceOverridden = true
      fenceEnabled = !fenceEnabled
      debug("new fence state: " + fenceEnabled)
      startOverride()
      sendOverride()
    }
  }

  private def doNextMode(isNext: Boolean) {
    if (!favoriteModes.isEmpty)
      myVehicle.foreach { v =>

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

        v ! DoSetMode(newMode)
      }
  }

  private def stopOverrides() {
    warn("Stopping overrides")
    isOverriding = false
    fenceOverridden = false
    myVehicle.foreach { v =>
      val p = v.rcChannelsOverride()
      v.sendMavlink(p)
      v.sendMavlink(p)
      v.sendMavlink(p)
    }
  }

  private def startOverride() {
    if (!isOverriding) {
      // On first we need to read some calibration from the device
      if (!sticksEnabled)
        getParameters()

      speak("Joystick on")
      isOverriding = true

      // Pull over current throttle setting
      for (v <- myVehicle; rc <- v.rcChannels) yield {
        val oldthrottle = rc.chan3_raw
        throttle = axis(2).unscale(oldthrottle)
      }
    }
  }

  private def setOverride(p: msg_rc_channels_override, chNum: Int, v: Int) {
    chNum match {
      case 0 => // Ignore
      case 1 => p.chan1_raw = v
      case 2 => p.chan2_raw = v
      case 3 => p.chan3_raw = v
      case 4 => p.chan4_raw = v
      case 5 => p.chan5_raw = v
      case 6 => p.chan6_raw = v
      case 7 => p.chan7_raw = v
      case 8 => p.chan8_raw = v
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

      val p = v.rcChannelsOverride()
      p.chan1_raw = axis(0).scale(aileron)
      p.chan2_raw = axis(1).scale(elevator)
      p.chan3_raw = axis(2).scale(throttle)
      p.chan4_raw = axis(3).scale(rudder)
      if (fenceOverridden)
        setOverride(p, fenceChannel, if (fenceEnabled) 2000 else 1000)

      // don't let override messages queue up in the output FIFO
      debug("Sending: " + p)
      v.sendMavlink(SendYoungest(p))
    }
  }
}