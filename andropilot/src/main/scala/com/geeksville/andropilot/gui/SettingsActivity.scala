package com.geeksville.andropilot.gui

import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Activity
import com.geeksville.andropilot.FlurryActivity
import com.geeksville.andropilot.R
import android.preference.PreferenceActivity.Header
import android.preference.PreferenceActivity

class SettingsActivity extends PreferenceActivity with FlurryActivity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    /*
    // Display the fragment as the main content.
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, new SettingsFragment())
      .commit()
     
      */
  }

  /**
   * Populate the activity with the top-level headers.
   */
  override def onBuildHeaders(target: java.util.List[Header]) {
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