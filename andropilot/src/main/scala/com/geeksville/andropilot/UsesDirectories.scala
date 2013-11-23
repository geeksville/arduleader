package com.geeksville.andropilot
import android.os.Environment
import java.io.File
import com.ridemission.scandroid.UsesResources

trait UsesDirectories extends UsesResources {
  /**
   * Where we should spool our output files (if allowed)
   */
  def sdDirectory = {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
      None
    else {
      val sdcard = Environment.getExternalStorageDirectory()
      if (!sdcard.exists())
        None
      else
        Some(new File(sdcard, S(R.string.app_name).toLowerCase))
    }
  }

  def logDirectory = sdDirectory.map { sd =>
    val f = new File(sd, "newlogs")
    f.mkdirs()
    f
  }
  def checklistDirectory = sdDirectory.map { sd =>
    val f = new File(sd, "checklists")
    f.mkdirs()
    f
  }
  def uploadedDirectory = sdDirectory.map { sd =>
    val f = new File(sd, "uploaded")
    f.mkdirs()
    f
  }
  def paramDirectory = sdDirectory.map { sd =>
    val f = new File(sd, "param-files")
    f.mkdirs()
    f
  }
  def waypointDirectory = sdDirectory.map { sd =>
    val f = new File(sd, "waypoints")
    f.mkdirs()
    f
  }
}