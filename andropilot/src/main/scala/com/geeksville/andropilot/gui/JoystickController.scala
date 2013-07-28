package com.geeksville.andropilot.gui

import android.view.MotionEvent
import android.view.InputDevice
import com.ridemission.scandroid._
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
import com.geeksville.andropilot.R
import android.content.ActivityNotFoundException
import com.geeksville.flight.MsgRcChannelsChanged
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw

/**
 * Provides joystick control either through hardware or a virtual screen joystick (not yet implemented)
 *
 * Implicitly set FBW when either of the sticks are moved (FIXME - not implemented)
 * Button R2 = RTL
 * Button L1 = toggle fence on/off
 * Button R1 = cycle through favorite modes
 * Button START = return all axis controls to regular RC transmitter
 */
trait JoystickController extends Activity
  with AndroidLogger with AndroServiceClient with TTSClient with UsesResources {

  protected val debugOutput = false

  private var fenceChannel = 0

  /// Don't enable till we've read our params
  var sticksEnabled = false
  var hasParameters = false

  // Raw values from the stick
  // -1 is up and to the left for the gamepad
  var rudder = 0f
  var aileron = 0f
  var elevator = 0f

  val aileronAxisNum = 0
  val elevatorAxisNum = 1
  val throttleAxisNum = 2
  val rudderAxisNum = 3

  private val aileronScale = 1.0f
  private val elevatorScale = 1.0f
  private val rudderScale = 1.0f

  /// Are we driving the four primary axes?
  var isOverriding = false

  /// 0 is 0 throttle 1.0f is full throttle
  var throttle = 0f

  private var fenceEnabled = false

  // We handle overrides of fence separately (so as to not change fence enable just because we switched to game pad)
  private var fenceOverridden = false

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
    /// Not used? FIXME
    def scaleThrottle(raw: Float) = {
      // Convert to a range from 0 to 1 with all proper reversals done
      val stick = raw * reverse * (if (stickBackwards) -1 else 1) * scaleVal

      (stick * (max - min) + min).toInt
    }

    /**
     * Given a servo usec value return a value between 0 and 1 (scaling and reversing)
     */
    def unscale(raw: Int) = {

      // scale linearly between the trimpos and the correct min/max
      val r = (reverse / scaleVal) * (if (raw > trim)
        (raw.toFloat - trim) / (max - trim)
      else
        (raw.toFloat - trim) / (trim - min))

      //debug(this + ": Unscale " + raw + " to " + r)

      // Clamp to -1.0 to 1.0
      math.min(1.0f, math.max(-1.0f, r))
    }
  }

  protected var axis = Array(AxisInfo(), AxisInfo(), AxisInfo(), AxisInfo())

  /**
   * Super skanky - this hook is called from the activity when our parameters have arrived
   */
  protected def handleParameters() {
    hasParameters = true

    // On first we need to read some calibration from the device
    getParameters()
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

  /**
   * When we get a service, make sure we turn off any stale overrides
   */
  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    stopOverrides()
  }

  def stopOverrides() {
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

  def startOverride() {
    if (!isOverriding) {
      speak(S(R.string.spk_joystick_on))
      isOverriding = true

      // Pull over current throttle setting
      for (v <- myVehicle; rc <- v.rcChannels) yield {
        val oldthrottle = rc.chan3_raw
        throttle = axis(throttleAxisNum).unscale(oldthrottle)
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

  def doToggleFence() {
    debug("In toggle fence")
    if (fenceChannel != 0) {
      fenceOverridden = true
      fenceEnabled = !fenceEnabled
      debug("new fence state: " + fenceEnabled)
      startOverride()
      sendOverride()
    }
  }

  /**
   * -1 for a channel means leave unchanged
   * 0 for a channel means do not override
   */
  def sendOverride() {
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