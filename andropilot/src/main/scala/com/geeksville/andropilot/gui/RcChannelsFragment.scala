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
import com.geeksville.akka.InstrumentedActor

class RcChannelsFragment extends SimpleListFragment with UsesResources {

  override def onVehicleReceive: InstrumentedActor.Receiver = {
    case MsgRcChannelsChanged =>
      if (isVisible) {
        //debug("Received Rc channels")
        handler.post(updateList _)
      }
  }

  private def rcToSeq(m: Seq[Int]) =
    m.zipWithIndex.map {
      case (a, i) =>
        S(R.string.channel).format(i + 1) -> a
    }

  protected def makeAdapter(): Option[SimpleAdapter] = {
    for { v <- myVehicle } yield {
      seqToAdapter(rcToSeq(v.rcChannels))
    }
  }
}
