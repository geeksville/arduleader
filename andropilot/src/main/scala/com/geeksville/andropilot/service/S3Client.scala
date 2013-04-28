package com.geeksville.andropilot.service

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.model.ProgressListener
import java.io.File
import com.amazonaws.services.s3.model.ProgressEvent
import java.io.IOException

class S3Upload(val bucketName: String, val keyName: String, val srcFile: File) extends ProgressListener {

  // Transfer a file to an S3 bucket.
  val upload = S3Client.transfer.upload(bucketName, keyName, srcFile)

  private var totalTransferred = 0

  upload.addProgressListener(this)

  private def onCompletion() {
    upload.removeProgressListener(this)
  }

  /**
   * The uploader has made some progress
   */
  protected def handleProgress(bytesTransferred: Int) {}

  protected def handleUploadFailed(ex: Option[Exception]) {}

  protected def handleUploadCompleted() {}

  override def progressChanged(ev: ProgressEvent) {
    println("S3 progress: " + ev.getEventCode)

    totalTransferred += ev.getBytesTransfered

    ev.getEventCode match {
      case 0 =>
        handleProgress(totalTransferred)
      case ProgressEvent.FAILED_EVENT_CODE =>
        onCompletion()
        // val ex = Option(upload.waitForException()) // can't call this inside a handler
        handleUploadFailed(Some(new IOException("File upload interrupted")))
      case ProgressEvent.COMPLETED_EVENT_CODE =>
        onCompletion()
        handleUploadCompleted()
      case ProgressEvent.CANCELED_EVENT_CODE =>
        onCompletion()
        handleUploadFailed(None)
      case x @ _ =>
        println("Ignoring S3 progress: " + x)
    }
  }
}

object S3Client {
  private val accessKey = "AKIAIALOFNWOTXDMVF3Q"
  private val secretAccessKey = "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"

  private val credential = new BasicAWSCredentials(accessKey, secretAccessKey)

  // TransferManager manages its own thread pool, so
  // please share it when possible.
  val transfer = new TransferManager(credential)
}