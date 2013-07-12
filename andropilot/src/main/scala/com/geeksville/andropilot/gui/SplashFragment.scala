package com.geeksville.andropilot.gui

import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.geeksville.andropilot.R

class SplashFragment extends DialogFragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    val view = inflater.inflate(R.layout.splash_fragment, container)
    getDialog().setTitle("Andropilot")

    view
  }

  // this.dismiss();
}