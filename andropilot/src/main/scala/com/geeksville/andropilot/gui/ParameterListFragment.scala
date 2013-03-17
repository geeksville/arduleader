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
import android.support.v4.app.Fragment
import com.geeksville.flight.ParametersModel

class ParameterListFragment extends ListAdapterHelper[ParametersModel#ParamValue] with AndroServiceFragment {
  private var selected = -1
  private var haveInitialParams = false

  override def onServiceConnected(s: AndropilotService) {
    super.onServiceConnected(s)

    // Don't expand the view until we have _something_ to display
    debug("parameter list service connected")
    updateParameters()
  }

  override def onActivityCreated(savedInstanceState: Bundle) {
    debug("Creating parameter list view")
    // Inflate the layout for this fragment
    super.onActivityCreated(savedInstanceState)

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
      haveInitialParams = true
      handler.post(updateParameters _)

    case MsgParameterReceived(index) =>
      if (haveInitialParams) // Don't bother with every little update when we haven't received our big squirt
        handler.post { () => updateParameter(index) }
  }

  /**
   * If we call this too early the list view might not be ready
   */
  private def safeGetListView = {
    try {
      getListView
    } catch {
      case ex: IllegalStateException =>
        null
    }
  }

  private def updateParameters() {
    // Don't expand the view until we have _something_ to display
    //if (getActivity != null) {

    // If we already have a view then update it
    Option(safeGetListView).foreach { v =>
      makeAdapter()

      v.invalidate()
    }
  }

  override def onViewCreated(v: View, b: Bundle) {
    super.onViewCreated(v, b)

    makeAdapter()
  }

  private def updateParameter(i: Int) {
    myVehicle.foreach { v =>
      updateAdapter(v.parameters, i)
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
        var frag = manager.findFragmentByTag("paraminfo").asInstanceOf[ParameterInfoFragment]
        assert(frag != null)
        frag.setParam(Some(param))
      }
    }
  }

  protected def rowId = R.layout.parameter_row

  protected override def fromKeys = Array("n", "v")
  protected override val toFields = Array(R.id.param_name, R.id.param_value)

  override def isSelected(p: Int) = selected == p

  override protected def makeRow(i: Int, p: ParametersModel#ParamValue) = Map("n" -> p.getId.getOrElse("?"), "v" -> p.asString.getOrElse("?"))

  private def makeAdapter() =
    for (v <- myVehicle if !v.parameters.isEmpty) yield {
      debug("Setting parameter list to " + v.parameters.size + " items")

      setAdapter(v.parameters)
    }
}
