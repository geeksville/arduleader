package com.geeksville.andropilot.gui

import android.os.Bundle
import android.view.View
import scala.collection.JavaConverters._
import android.widget.SimpleAdapter
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw
import com.geeksville.flight.MsgRcChannelsChanged
import com.geeksville.util.ThreadTools._
import android.support.v4.app.ListFragment
import com.geeksville.andropilot.R
import com.geeksville.andropilot.service._
import com.ridemission.scandroid._

class RcChannelsFragment extends SimpleListFragment with UsesResources {

  override def onVehicleReceive = {
    case MsgRcChannelsChanged(_) =>
      if (isVisible) {
        //debug("Received Rc channels")
        handler.post(updateList _)
      }
  }

  private def rcToSeq(m: msg_rc_channels_raw) =
    Seq(m.chan1_raw, m.chan2_raw, m.chan3_raw, m.chan4_raw,
      m.chan5_raw, m.chan6_raw, m.chan7_raw, m.chan8_raw).zipWithIndex.map {
        case (a, i) =>
          S(R.string.channel).format(i + 1) -> a
      }

  protected def makeAdapter(): Option[SimpleAdapter] = {
    for { v <- myVehicle; rc <- v.rcChannels } yield {
      seqToAdapter(rcToSeq(rc))
    }
  }
}
