package com.geeksville.andropilot.gui

import android.os.Bundle
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import android.os.Handler
import com.geeksville.util.ThreadTools._
import android.support.v4.app.Fragment
import com.geeksville.andropilot.service.AndroServiceClient
import android.view.ActionMode
import com.ridemission.scandroid.PagerPage
import com.geeksville.andropilot.FlurryClient

/**
 * Mixin for common behavior for all our fragments that depend on data from the andropilot service.
 */
trait AndroServiceFragment extends Fragment with AndroidLogger with AndroServiceClient with PagerPage with FlurryClient {

  implicit def context = getActivity

  /**
   * Does work in the GUIs thread
   */
  protected final var handler: Handler = null

  private var actionMode: Option[ActionMode] = None

  override def onCreate(saved: Bundle) {
    super.onCreate(saved)

    debug("androFragment onCreate")
    handler = new Handler
  }

  override def onResume() {
    debug("androFragment onResume")
    super.onResume()

    serviceOnResume()
  }

  override def onPause() {
    debug("androFragment onPause")

    serviceOnPause()

    super.onPause()
  }

  override def onPageShown() {
    beginTimedEvent("show_" + getClass.getSimpleName)
    super.onPageShown()
  }

  override def onPageHidden() {
    endTimedEvent("show_" + getClass.getSimpleName)
    stopActionMode() // Don't show our menu on other pages
    super.onPageHidden()
  }

  //
  // Operations for action modes
  //

  trait ActionModeCallback extends ActionMode.Callback {
    // Called when the user exits the action mode
    override def onDestroyActionMode(mode: ActionMode) {
      actionMode = None
    }
  }

  /// menu choices might have changed)
  protected def invalidateContextMenu() {
    actionMode.foreach(_.invalidate())
  }

  protected def startActionMode(cb: ActionModeCallback) {
    // Reuse existing action mode if possible
    actionMode match {
      case Some(am) =>
        invalidateContextMenu() // menu choices might have changed
      case None =>
        actionMode = Some(getActivity.startActionMode(cb))
    }
  }

  protected def stopActionMode() {
    actionMode.foreach { a =>
      debug("Stopping action mode")
      a.finish()
    }
    actionMode = None
  }

  // protected def isVisible = (getActivity != null) && (getView != null)
}
