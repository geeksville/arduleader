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
import com.ridemission.scandroid._

class ServoOutputFragment extends SimpleListFragment with UsesResources {

  override def onVehicleReceive = {
    case MsgServoOutputChanged(_) =>
      if (isVisible) {
        //debug("Received Rc channels")
        handler.post(updateList _)
      }
  }

  private def rcToSeq(m: msg_servo_output_raw) =
    Seq(m.servo1_raw, m.servo2_raw, m.servo3_raw, m.servo4_raw,
      m.servo5_raw, m.servo6_raw, m.servo7_raw, m.servo8_raw).zipWithIndex.map { case (a, i) =>
         S(R.string.servo).format(i) -> a
         }
      
  protected def makeAdapter(): Option[SimpleAdapter] = {
    for { v <- myVehicle; rc <- v.servoOutputRaw } yield {
      seqToAdapter(rcToSeq(rc))
    }
  }
}
