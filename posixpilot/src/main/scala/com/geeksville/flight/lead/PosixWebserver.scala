package com.geeksville.flight.lead

import com.geeksville.gcsapi._
import com.ridemission.rest.FileHandler
import java.io.File

/**
 * A GCSApi webserver that knows to pull static content from the local filesystem
 */
class PosixWebserver(root: SmallAPI, localonly: Boolean = true) extends Webserver(root, localonly) {
  // FIXME - we currently assume the cwd is the default of 'posixpilot'
  server.addHandler(new FileHandler("/static", new File("../common/src/main/webapp")))
}