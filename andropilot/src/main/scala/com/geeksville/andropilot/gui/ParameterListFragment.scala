package com.geeksville.andropilot.gui
import scala.collection.JavaConverters._
import com.geeksville.util.ThreadTools._
import android.support.v4.app.ListFragment
import com.geeksville.andropilot.service.AndropilotService
import com.geeksville.flight.MsgParametersDownloaded
import android.widget.ListView
import android.view.View
import android.widget.SimpleAdapter
import com.geeksville.andropilot.R
import com.geeksville.andropilot.TypedResource._
import com.geeksville.andropilot.TR
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.AdapterView
import android.graphics.Color
import com.geeksville.flight.MsgParameterReceived

class ParameterListFragment extends ListFragment with AndroServiceFragment {
  private var selected = -1

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    // Don't expand the view until we have _something_ to display
    debug("parameter list service connected")
    updateParameters()
  }

  override def onViewCreated(v: View, savedInstanceState: Bundle) {
    debug("Creating parameter list view")
    // Inflate the layout for this fragment
    super.onViewCreated(v, savedInstanceState)

    // Fire up our editor dialog as needed
    getListView.setOnItemLongClickListener(new OnItemLongClickListener {

      def onItemLongClick(arg0: AdapterView[_], arg1: View, position: Int, id: Long) = {
        // TODO Auto-generated method stub

        debug("long clicked" + position)

        myVehicle.foreach { v =>
          if (position < v.parameters.size) {
            val param = v.parameters(position)
            val transaction = getFragmentManager.beginTransaction()

            var frag = new ParameterEditorFragment(param)
            frag.show(transaction, "paramedit_fragment")
          }
        }

        true
      }
    })
  }

  override def onVehicleReceive = {
    case MsgParametersDownloaded =>
      handler.post(updateParameters _)

    case MsgParameterReceived(index) =>
      handler.post(updateParameters _) // { () => updateParameter(index) }
  }

  private def updateParameters() {
    // Don't expand the view until we have _something_ to display
    //if (getActivity != null) {
    debug("updating parameters")
    makeAdapter.foreach(setListAdapter)
    Option(getListView).foreach(_.invalidate())
    //}
  }

  // FIXME - this can't work, because of the dumb/heavyweight way we are building the adapter
  private def updateParameter(i: Int) {
    Option(getListView).foreach { l =>
      debug("Invaliding due to update: " + i)
      l.invalidateViews() // FIXME, is there a less heavyweight way to redraw our changed item
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    info("Item clicked: " + id)

    myVehicle.foreach { v =>
      if (position < v.parameters.size) {
        val param = v.parameters(position)
        val manager = getFragmentManager

        selected = position
        l.invalidateViews()

        debug("Creating new edit pane")
        var frag = manager.findFragmentById(R.id.parameter_info_fragment).asInstanceOf[ParameterInfoFragment]
        assert(frag != null)
        frag.setParam(Some(param))
      }
    }
  }

  private def makeAdapter() =
    for (v <- myVehicle if !v.parameters.isEmpty) yield {
      debug("Setting parameter list to " + v.parameters.size + " items")

      val asMap = v.parameters.toSeq.map { p =>
        Map("n" -> p.getId.getOrElse("?"), "v" -> p.asString.getOrElse("?")).asJava
      }.asJava
      val fromKeys = Array("n", "v")
      val toFields = Array(R.id.param_name, R.id.param_value)
      //new SimpleAdapter(getActivity, asMap, R.layout.parameter_row, fromKeys, toFields)
      new SimpleAdapter(getActivity, asMap, R.layout.parameter_row, fromKeys, toFields) {
        // Show selected item in a color
        override def getView(position: Int, convertView: View, parent: ViewGroup) = {
          val itemView = super.getView(position, convertView, parent)
          debug("in getView " + position)
          if (selected == position) {
            // debug("Selecting " + itemView)
            itemView.setBackgroundColor(Color.LTGRAY)
          } else
            itemView.setBackgroundColor(Color.TRANSPARENT)
          itemView
        }
      }
    }
}
