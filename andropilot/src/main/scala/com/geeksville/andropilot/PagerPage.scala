package com.geeksville.andropilot

import com.ridemission.scandroid.AndroidLogger

/**
 * A fragment that is a page on a paged view
 * FIXME - move to scandroid utilities
 */
trait PagerPage extends AndroidLogger {
  var isShown = false

  def onPageShown() {
    isShown = true
    debug("Page shown: " + this)
  }
  def onPageHidden() {
    isShown = false
    debug("Page hidden: " + this)
  }
}