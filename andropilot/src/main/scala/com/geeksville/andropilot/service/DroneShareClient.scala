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
import scala.util.Random

class DroneShareUpload(srcFile: File, val userId: String, val userPass: String)
  extends S3Upload("s3-droneshare", DroneShareUpload.createKey(), srcFile) {

  private val baseUrl = "http://www.droneshare.com"
  // private val baseUrl = "http://192.168.0.93:8080"
  private val webAppUploadUrl = baseUrl + "/api/upload/froms3.json"

  var tlogId = "FIXME" // Need to use the server response

  /**
   * URL to see the webpage for this tlog
   */
  def viewURL = baseUrl + "/view/" + tlogId

  def kmzURL = baseUrl + "/api/tlog/" + tlogId + ".kmz"

  private def jsonToWebApp = """
	|{
    |  "key": "%s",
    |  "userId": "%s",
    |  "userPass": "%s"
	|}
  	""".stripMargin.format(keyName, userId, userPass)

  /**
   * Now tell our webapp
   */
  override protected def handleUploadCompleted() {
    try {
      tlogId = tellWebApp()
      println("WebApp responds: " + tlogId)

      handleWebAppCompleted()
    } catch {
      case ex: Exception =>
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

    println("Sending JSON: " + jsonToWebApp)

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
    val resp = DroneShareUpload.httpclient.execute(httpost, responseHandler)

    // Skanky way to decode a json string
    if (resp.startsWith("\"") && resp.endsWith("\""))
      resp.substring(1, resp.length - 1)
    else
      throw new Exception("Malformed response")
  }
}

object DroneShareUpload {
  val httpclient = new DefaultHttpClient()

  private val rand = new Random(System.currentTimeMillis)

  def createKey() = {
    "uploads/" + math.abs(rand.nextLong).toString + ".tlog"
  }
}
