package com.geeksville.andropilot.gui

import android.os.Bundle
import android.view.View
import scala.collection.JavaConverters._
import android.widget.SimpleAdapter
import com.geeksville.util.ThreadTools._
import android.support.v4.app.ListFragment
import com.geeksville.andropilot.R
import com.geeksville.andropilot.service._
import com.geeksville.flight.MsgServoOutputChanged
import org.mavlink.messages.ardupilotmega.msg_servo_output_raw

class ServoOutputFragment extends SimpleListFragment {

  override def onVehicleReceive = {
    case MsgServoOutputChanged(_) =>
      if (isVisible) {
        //debug("Received Rc channels")
        handler.post(updateList _)
      }
  }

  private def rcToSeq(m: msg_servo_output_raw) =
    Seq("Servo 1" -> m.servo1_raw, "Servo 2" -> m.servo2_raw, "Servo 3" -> m.servo3_raw, "Servo 4" -> m.servo4_raw,
      "Servo 5" -> m.servo5_raw, "Servo 6" -> m.servo6_raw, "Servo 7" -> m.servo7_raw, "Servo 8" -> m.servo8_raw)

  protected def makeAdapter(): Option[SimpleAdapter] = {
    for { v <- myVehicle; rc <- v.servoOutputRaw } yield {
      seqToAdapter(rcToSeq(rc))
    }
  }
}
