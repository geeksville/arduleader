package com.geeksville.andropilot.service

import java.io.File
import java.io.FilenameFilter
import android.content.Context
import com.ridemission.scandroid._
import android.support.v4.app.NotificationCompat
import android.app.NotificationManager
import com.geeksville.andropilot.R
import android.content.Intent
import android.net.Uri
import android.app.PendingIntent
import com.geeksville.andropilot.AndropilotPrefs
import android.net.ConnectivityManager
import android.app.IntentService

/**
 * Scan for tlogs in the specified directory.  If found, upload them to droneshare and then either delete or
 * move to destdir
 */
class AndroidDirUpload extends IntentService("Uploader") 
  with AndroidLogger with AndropilotPrefs with UsesResources {

  val srcDirOpt = AndropilotService.logDirectory
  val destDirOpt = AndropilotService.uploadedDirectory

  private var curUpload: Option[AndroidUpload] = None

  def context = this
  def isUploading = curUpload.isDefined

  // FIXME - check for data connection and permission for background data
  def canUpload = !dshareUsername.isEmpty && !dsharePassword.isEmpty && dshareUpload && isNetworkAvailable && srcDirOpt.isDefined && destDirOpt.isDefined

  /// Anytime anyone sends us an intent, we just scan the spool directory to
  /// see if we have outbound files and send em all (if we have data connectivity)
  override def onHandleIntent(intent: Intent) {
    send()
  }

  private def send() {
    val toSend = srcDirOpt.flatMap { dir =>
      val files = dir.listFiles(new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(".tlog") })

      Option(files).flatMap(_.headOption)
    }

    toSend match {
      case Some(n) =>
        NetworkStateReceiver.register(this) // We now want to find out about network connectivity changes

        // We have a candidate for uploading, is the network good and user prefs entered?
        if (!isUploading && canUpload) { // If an upload is in progress wait for it to finish
          toast(R.string.starting_upload, false)
          curUpload = Some(new AndroidUpload(n))
        }
      case None =>
        NetworkStateReceiver.unregister(this)
    }

  }

  /**
   * Mark current file as complete and start the next file
   */
  private def handleSuccess() {
    val src = curUpload.get.srcFile

    if (!dshareDeleteSent) {
      destDirOpt.foreach { d =>
        d.mkdirs() // Make sure the dir exists

        val newName = new File(d, src.getName)
        warn("Moving to " + newName)
        src.renameTo(newName)
      }
    } else {
      warn("Deleting " + src)
      src.delete()
    }

    curUpload = None

    // Send next file
    // error("FIXME, suppressing next send")
    send()
  }

  private def handleFailure() {
    curUpload = None
  }

  def isNetworkAvailable = {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val activeNetworkInfo = connectivityManager.getActiveNetworkInfo()
    activeNetworkInfo != null && activeNetworkInfo.isConnected()
  }

  /**
   * Add android specific upload behavior
   */
  class AndroidUpload(srcFile: File) extends DroneShareUpload(srcFile, dshareUsername, dsharePassword) {

    private val fileSize = srcFile.length.toInt

    private val notifyId = AndroidUpload.makeId

    private val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    private val nBuilder = new NotificationCompat.Builder(context)
    nBuilder.setContentTitle(S(R.string.droneshare_upload))
      .setContentText(S(R.string.uploading_tlog))
      .setSmallIcon(R.drawable.icon)
      .setProgress(fileSize, 0, false)
      .setPriority(NotificationCompat.PRIORITY_LOW)

    // Generate initial notification
    updateNotification(true)

    debug("Started upload " + srcFile)

    private def updateNotification(isForeground: Boolean) {
      val n = nBuilder.build

      notifyManager.notify(notifyId, n)
      if (isForeground)
        startForeground(notifyId, n)
      else {
        stopForeground(false)
      }
    }

    private def removeProgress() { nBuilder.setProgress(0, 0, false) }

    override protected def handleProgress(bytesTransferred: Int) {
      debug("Upload progress " + bytesTransferred)
      nBuilder.setProgress(fileSize, bytesTransferred, false)
      updateNotification(true)

      super.handleProgress(bytesTransferred)
    }

    override protected def handleUploadFailed(ex: Option[Exception]) {
      error("Upload failed: " + ex)
      removeProgress()
      nBuilder.setContentText(S(R.string.failed) + ex.map(": " + _.getMessage).getOrElse(""))
      updateNotification(false)
      handleFailure()

      super.handleUploadFailed(ex)
    }

    override protected def handleUploadCompleted() {
      debug("Upload completed")
      removeProgress()
      updateNotification(true)

      super.handleUploadCompleted
    }

    override protected def handleWebAppCompleted() {
      debug("Webapp upload completed")
      nBuilder.setContentText(S(R.string.completed_select))

      // Attach the view URL
      val pintent = PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(viewURL)), 0)
      nBuilder.setContentIntent(pintent)

      // Attach the google earth link
      val geintent = PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(kmzURL)), 0)
      nBuilder.addAction(android.R.drawable.ic_menu_mapmode, S(R.string.google_earth), geintent)

      // Attach a web link
      nBuilder.addAction(android.R.drawable.ic_menu_set_as, S(R.string.web), pintent)

      // Add a share link
      val sendIntent = new Intent(Intent.ACTION_SEND)
      sendIntent.putExtra(Intent.EXTRA_TEXT, viewURL)
      sendIntent.setType("text/plain")
      //val chooser = Intent.createChooser(sendIntent, "Share log to...")
      nBuilder.addAction(android.R.drawable.ic_menu_share, S(R.string.share),
        PendingIntent.getActivity(context, 0, sendIntent, 0))
      nBuilder.setPriority(NotificationCompat.PRIORITY_HIGH) // The user probably wants to choose us now

      // FIXME, include action buttons for sharing

      updateNotification(false)

      // Send the next file
      handleSuccess()

      super.handleWebAppCompleted()
    }
  }

  object AndroidUpload {
    private var nextId = 2

    def makeId = {
      val r = nextId
      nextId += 1
      r
    }
  }
}

object AndroidDirUpload {

  /// Create an Intent that will start this service
  def createIntent(context: Context) = new Intent(context, classOf[AndroidDirUpload])
}