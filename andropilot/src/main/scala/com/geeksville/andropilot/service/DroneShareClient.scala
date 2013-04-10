package com.geeksville.andropilot.service

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.model.ProgressListener
import java.io.File
import com.amazonaws.services.s3.model.ProgressEvent
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.client.HttpResponseException

class DroneShareUpload(srcFile: File, val userId: String, val userPass: String, val key: String = DroneShareUpload.createKey()) extends S3Upload("s3-droneshare", "uploads/" + key, srcFile) {

  private val baseUrl = "http://www.droneshare.com"
  private val webAppUploadUrl = baseUrl + "/api/upload/froms3.json"

  /**
   * URL to see the webpage for this tlog
   */
  def viewURL = baseUrl + "/view/" + key

  def kmzURL = baseUrl + "/api/tlog/" + key + ".kmz"

  private def jsonToWebApp = """
	|{
    |  key: '%s',
    |  userId: '%s',
    |  userPass: '%s'
	|}
  	""".stripMargin.format(key, userId, userPass)

  /**
   * Now tell our webapp
   */
  override protected def handleUploadCompleted() {
    try {
      tellWebApp()

      handleWebAppCompleted()
    } catch {
      case ex: HttpResponseException =>
        handleUploadFailed(Some(ex))
    }
  }

  /**
   * Show the user the view/download URL
   */
  protected def handleWebAppCompleted() {}

  def tellWebApp() = {
    //instantiates httpclient to make request

    //url with the post data
    val httpost = new HttpPost(webAppUploadUrl)

    //passes the results to a string builder/entity
    val se = new StringEntity(jsonToWebApp)

    //sets the post request as the resulting string
    httpost.setEntity(se)
    //sets a request header so the page receving the request
    //will know what to do with it
    httpost.setHeader("Accept", "application/json")
    httpost.setHeader("Content-type", "application/json")

    //Handles what is returned from the page 
    val responseHandler = new BasicResponseHandler()
    DroneShareUpload.httpclient.execute(httpost, responseHandler)
  }
}

object DroneShareUpload {
  val httpclient = new DefaultHttpClient()

  def createKey() = {
    "FIXME"
  }
}
