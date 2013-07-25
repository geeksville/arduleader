package com.geeksville.andropilot

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper

class MyBackupAgent extends BackupAgentHelper {

  // A key to uniquely identify the set of backup data
  val PREFS_BACKUP_KEY = "default"

  // Allocate a helper and add it to the backup agent
  override def onCreate() {
    // The name of the SharedPreferences file
    val PREFS = getPackageName() + "_preferences"

    val helper = new SharedPreferencesBackupHelper(this, PREFS)
    addHelper(PREFS_BACKUP_KEY, helper)
  }
}