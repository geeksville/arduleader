package com.geeksville.andropilot.gui

import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.geeksville.andropilot.R
import android.widget.ProgressBar

/**
 * A mixin that adds a progress bar while loading
 */
trait ProgressList extends ListFragment {
  protected def progressBar = getView.findViewById(android.R.id.empty).asInstanceOf[ProgressBar]

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    val view = inflater.inflate(R.layout.progress_list, container, false);
    view
  }
}