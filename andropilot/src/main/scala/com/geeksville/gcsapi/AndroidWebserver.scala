package com.geeksville.gcsapi

import com.geeksville.gcsapi._
import com.ridemission.rest.FileHandler
import android.content.Context
import com.geeksville.rest.AndroidFilesystem
import com.geeksville.andropilot.UsesDirectories
import com.ridemission.rest.JavaFileSystem

/**
 * A GCSApi webserver that knows to pull static content from the local filesystem
 */
class AndroidWebserver(val context: Context, root: SmallAPI, localonly: Boolean = true) extends Webserver(root, localonly) with UsesDirectories {
  // FIXME - we currently assume the cwd is the default of 'posixpilot'
  server.addHandler(new FileHandler("/static", new AndroidFilesystem(context.getAssets, "webapp/")))

  // Allow users to place custom checklists in /sdcard/andropilot/checklist/plane.html or copter.html.
  checklistDirectory.foreach { dir =>
    server.addHandler(new FileHandler("/static/checklist", new JavaFileSystem(dir)))
  }
}