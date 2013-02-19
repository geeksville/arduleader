package com.geeksville.andropilot.gui

import com.geeksville.andropilot.R
import android.view.View
import com.ridemission.scandroid.AndroidLogger

class ParameterPane extends LayoutFragment(R.layout.parameter_pane) with AndroidLogger {
  override def onViewCreated(v: View) {
    super.onViewCreated(v)

    debug("Creating child fragments")

    recreateFragment(R.id.parameter_list_fragment, "paramlist", new ParameterListFragment)
    recreateFragment(R.id.parameter_info_fragment, "paraminfo", new ParameterInfoFragment)
  }
}