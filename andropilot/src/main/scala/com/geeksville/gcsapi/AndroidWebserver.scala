package com.geeksville.gcsapi

import com.geeksville.gcsapi._
import com.ridemission.rest.FileHandler
import android.content.Context
import com.geeksville.rest.AndroidFilesystem

/**
 * A GCSApi webserver that knows to pull static content from the local filesystem
 */
class AndroidWebserver(context: Context, root: SmallAPI, localonly: Boolean = true) extends Webserver(root, localonly) {
  // FIXME - we currently assume the cwd is the default of 'posixpilot'
  server.addHandler(new FileHandler("/static", new AndroidFilesystem(context.getAssets, "webapp/")))

  // server.addHandler(new FileHandler("/static/checklist.html", new AndroidFilesystem(context.getAssets, "webapp/checklist/plane.html")))
}