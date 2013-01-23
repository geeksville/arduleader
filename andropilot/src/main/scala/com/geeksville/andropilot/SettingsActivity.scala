package com.geeksville.andropilot

import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Activity

class SettingsActivity extends Activity with FlurryActivity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Display the fragment as the main content.
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, new SettingsFragment())
      .commit()
  }
}

class SettingsFragment extends PreferenceFragment {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences)
  }
}