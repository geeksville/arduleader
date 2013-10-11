package com.geeksville.andropilot.gui

import android.os.Bundle
import scala.collection.JavaConverters._
import com.geeksville.util.ThreadTools._
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ridemission.scandroid.AndroidUtil._
import com.geeksville.andropilot.TypedResource._
import com.geeksville.andropilot.TR
import com.geeksville.flight._
import org.mavlink.messages.ardupilotmega.msg_attitude
import com.geeksville.andropilot.R
import com.bvcode.ncopter.widgets.HUD
import com.geeksville.akka.InstrumentedActor

class HudFragment extends Fragment with AndroServicePage {

  private def hud = Option(getView).map(_.findView(TR.hud_view))

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    // Inflate the layout for this fragment
    val v = inflater.inflate(R.layout.hud_fragment, container, false)

    initView(v.findView(TR.hud_view))
    v
  }

  /**
   * Provide current vehicle state - so the view is correct until it receives new msgs
   */
  private def initView(h: HUD) {
    for {
      v <- myVehicle
      att <- v.attitude
    } yield {
      h.newFlightData(att.roll, att.pitch, att.yaw)
    }
  }

  override def onResume() = {
    super.onResume()

    hud.foreach(initView _)
  }

  override def onVehicleReceive: InstrumentedActor.Receiver = {
    case msg: msg_attitude =>
      //debug(msg.toString)
      handler.post { () =>
        hud.foreach(_.newFlightData(msg.roll, msg.pitch, msg.yaw))
      }

    case l: Location =>
      //debug("Handling location: " + l) 
      handler.post { () =>
        myVehicle.foreach { v =>
          val degSymbol = "\u00B0"
          //latView.setText(l.lat.toString + degSymbol)
          //lonView.setText(l.lon.toString + degSymbol)
          //altView.setText(l.alt + "m")
          hud.foreach(_.setAltitude(v.bestAltitude + "m"))
          // myVehicle.foreach { v =>v.numSats.foreach { n => numSatView.setText(n.toString) } }
        }
      }

    case MsgSysStatusChanged =>
      handler.post { () =>
        if (getView != null) {
          for {
            v <- myVehicle
            h <- hud
          } yield {
            v.radio.foreach { n =>
              //rssiLocalView.setText(n.rssi.toString + "/" + n.remrssi.toString)
            }
            v.batteryVoltage.foreach { n =>
              val socStr = v.batteryPercent.map { pct => "%d%%".format((pct * 100).toInt) }.getOrElse("")
              //batteryView.setText(n.toString + "V " + socStr)

              h.setBatteryRemaining(socStr)
              h.setBatteryMVolt(n.toString + "V")
            }
          }
        }
      }
  }

}
