package com.geeksville.andropilot.gui

import android.os.Bundle
import android.widget.ArrayAdapter
import scala.collection.JavaConverters._
import com.geeksville.util.ThreadTools._
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ridemission.scandroid.AndroidUtil._
import com.geeksville.andropilot.TypedResource._
import com.geeksville.andropilot.TR
import android.widget.ArrayAdapter
import com.geeksville.flight._
import java.util.LinkedList
import com.geeksville.andropilot.R
import android.view.View
import com.ridemission.scandroid.AndroidLogger

/**
 * A simple fragment that just pulls its layout from a resource
 */
class LayoutFragment(layoutId: Int) extends Fragment with AndroidLogger {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    // Inflate the layout for this fragment
    val v = inflater.inflate(layoutId, container, false)
    onViewCreated(v)
    v
  }

  def recreateFragment(idToReplace: Int, tag: String, generator: => Fragment) {
    val fragMgr = getFragmentManager();
    val xact = fragMgr.beginTransaction();

    Option(fragMgr.findFragmentByTag(tag)).foreach { f =>
      debug("removing old" + f)
      xact.remove(f)
    }

    debug("Creating " + tag)
    xact.add(idToReplace, generator, tag)

    xact.commit()
  }

  protected def onViewCreated(v: View) {}

  override def onSaveInstanceState(outState: Bundle) {
    //first saving my state, so the bundle wont be empty.
    //http://code.google.com/p/android/issues/detail?id=19917
    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE")
    super.onSaveInstanceState(outState)
  }
}
