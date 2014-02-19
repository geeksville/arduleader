package com.geeksville.andropilot.gui

import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Activity
import com.geeksville.andropilot.FlurryActivity
import com.geeksville.andropilot.R
import android.preference.PreferenceActivity.Header
import android.preference.PreferenceActivity
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import android.content.Context
import android.content.Intent
import scala.collection.mutable.Buffer

object SettingsActivity {
  /**
   * Create an intent that will bring up network sharing settings
   */
  def sharingSettingsIntent(context: Context) = {
    val i = new Intent(context, classOf[SettingsActivity])
    i.setAction(Intent.ACTION_MANAGE_NETWORK_USAGE)
    i
  }
}

class SettingsActivity extends PreferenceActivity with FlurryActivity with AndroidLogger {

  implicit def acontext: Context = this

  // Bug in android - we have to track pref headers on our own
  // https://code.google.com/p/android/issues/detail?id=22430
  private var headers: Seq[Header] = Seq()

  override def onResume() {
    super.onResume()

    for {
      i <- Option(getIntent);
      act <- Option(i.getAction)
    } yield {
      warn("Action: " + act)

      act match {
        case Intent.ACTION_MANAGE_NETWORK_USAGE =>
          startPanel(R.id.pref_share)

        case x @ _ =>
          error("Unknown action: " + x)
      }
    }
  }

  private def startPanel(id: Int) {
    /*
val args = new Bundle
    args.putString("settings", name)
    startPreferencePanel("com.geeksville.andropilot.gui.SettingsFragment", args, 0, null, null, 0)
    
    */
    val found = headers.find { h =>
      warn("considering " + h + " " + h.id)
      id == h.id
    }

    found.foreach { h =>
      switchToHeader(h)
    }
  }

  /**
   * Populate the activity with the top-level headers.
   */
  override def onBuildHeaders(target: java.util.List[Header]) {
    // warn("Building prefs headers")
    headers = target.asScala
    this.loadHeadersFromResource(R.xml.preferences, target);
  }

}

class SettingsFragment extends PreferenceFragment {
  val mapping = Map(
    "flight" -> R.xml.preferences_flight,
    "mavlink" -> R.xml.preferences_mavlink,
    "network" -> R.xml.preferences_network,
    "serial" -> R.xml.preferences_serial,
    "share" -> R.xml.preferences_share)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    if (getArguments != null) {
      val n = getArguments.getString("settings")
      if (n != null) {
        val r = mapping(n)
        addPreferencesFromResource(r) // If there is a particular screen to show, then show it
      }
    }
  }
}