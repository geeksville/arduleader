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
import org.apache.http.HttpStatus
import org.apache.http.params.BasicHttpParams
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.params.HttpConnectionParams
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.entity.FileEntity
import com.geeksville.apiproxy.APIConstants
import org.json.JSONArray
import com.geeksville.util.ThreadTools
import org.apache.http.client.utils.URLEncodedUtils
import java.util.LinkedList
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

class DroneShareUpload(val srcFile: File, val userId: String, val userPass: String, val vehicleId: String) {

  private val baseUrl = APIConstants.URL_BASE
  private val apiKey = "1c5dda3a.cf407bcbd75bfb6d0f9103374f2b5bd4"
  // For droidplanner use 1f56d502994ded5f537394bcac8affe4

  private val params = new LinkedList[NameValuePair]()
  params.add(new BasicNameValuePair("api_key", apiKey))
  params.add(new BasicNameValuePair("login", userId))
  params.add(new BasicNameValuePair("password", userPass))
  params.add(new BasicNameValuePair("autoCreate", "true"))
  private val queryParams = URLEncodedUtils.format(params, "utf-8")
  private val webAppUploadUrl = s"$baseUrl/api/v1/mission/upload/$vehicleId?$queryParams"

  /**
   * URL to see the webpage for this tlog
   */
  var viewURL = "FIXME"

  // Do our upload in the background
  val thread = ThreadTools.createDaemon("upload")(doUpload)
  thread.start()

  /**
   * The webserver will send error code 406 if the file upload is considered unacceptably boring (flight too short)
   * Just tell the user something about that and do not treat it as an error
   */
  protected def handleUploadNotAccepted() {}

  /**
   * A transient failure occurred preventing the upload
   */
  protected def handleUploadFailed(ex: Option[Exception]) {}

  /**
   * Show the user the view/download URL
   */
  protected def handleUploadCompleted() {}

  private def doUpload() = {
    try {
      //instantiates httpclient to make request

      //url with the post data
      println(s"Starting upload to $webAppUploadUrl")
      val httpost = new HttpPost(webAppUploadUrl)

      val se = new FileEntity(srcFile, APIConstants.tlogMimeType)
      httpost.setEntity(se)

      //sets a request header so the page receving the request
      //will know what to do with it
      httpost.setHeader("Accept", "application/json")
      //httpost.setHeader("Content-type", APIConstants.tlogMimeType)

      //Handles what is returned from the page 
      val responseHandler = new BasicResponseHandler()
      val resp = DroneShareUpload.httpclient.execute(httpost, responseHandler)

      println(s"Received JSON response: $resp")
      val missions = new JSONArray(resp)
      if (missions.length != 1)
        throw new Exception("Non unity length array from server")

      val mission = missions.getJSONObject(0)
      viewURL = mission.getString("viewURL")

      println(s"View URL is $viewURL")
      handleUploadCompleted()
    } catch {
      case ex: HttpResponseException if ex.getStatusCode == HttpStatus.SC_NOT_ACCEPTABLE =>
        handleUploadNotAccepted()
      case ex: Exception =>
        handleUploadFailed(Some(ex))
    }
  }
}

object DroneShareUpload {
  val httpclient = {
    // new DefaultHttpClient()
    //use following code to solve Adapter is detached error
    //refer: http://stackoverflow.com/questions/5317882/android-handling-back-button-during-asynctask
    val params = new BasicHttpParams()

    val schemeRegistry = new SchemeRegistry()
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    val sslSocketFactory = SSLSocketFactory.getSocketFactory();
    schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));

    // Set the timeout in milliseconds until a connection is established.
    //HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
    // Set the default socket timeout (SO_TIMEOUT) 
    // in milliseconds which is the timeout for waiting for data.
    //HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);

    val cm = new ThreadSafeClientConnManager(params, schemeRegistry);
    new DefaultHttpClient(cm, params);
  }

  private val rand = new Random(System.currentTimeMillis)

  def createKey() = {
    "uploads/" + math.abs(rand.nextLong).toString + ".tlog"
  }
}
