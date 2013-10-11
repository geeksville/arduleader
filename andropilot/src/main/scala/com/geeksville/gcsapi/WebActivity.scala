package com.geeksville.gcsapi

import com.geeksville.andropilot._
import com.ridemission.scandroid._

import android.app._
import android.webkit._
import android.view._
import android.os._
import android.preference._
import android.content._
import android.net._

class WebActivity extends Activity with TypedActivity with AndroidLogger {
  override def onCreate(saved: Bundle) {
    //Remove title bar
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.web_fragment)
    super.onCreate(saved)

    //Remove notification bar
    getWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

    val view = findView(TR.web_main)
    view.setWebChromeClient(new CustomChromeClient)
    view.getSettings.setJavaScriptEnabled(true)
    view.getSettings.setBlockNetworkLoads(false)
    view.getSettings.setBlockNetworkImage(false)
    view.getSettings.setAllowFileAccess(false)
    view.getSettings.setLightTouchEnabled(true)
    view.getSettings.setBuiltInZoomControls(true)
    //view.getSettings.setDisplayZoomControls(true)
    view.getSettings.setJavaScriptCanOpenWindowsAutomatically(true)
    view.getSettings.setLoadsImagesAutomatically(true)
    view.getSettings.setAppCacheEnabled(true)
    //view.getSettings.setAllowContentAccess(true)

    // Allow links to work
    view.setWebViewClient(new WebViewClient)

    val intent = getIntent
    val url = intent.getStringExtra("url")
    warn("Loading " + url)
    view.loadUrl(url)

    println("Done making activity")
  }

  override def onResume() {
    super.onResume()
  }

  override def onPause() {
    super.onPause()
  }

  override def onDestroy() {
    // work around for http://stackoverflow.com/questions/7156420/how-can-i-destroy-the-webview-activity-and-the-video-in-webview
    val view = findView(TR.web_main)
    println("Destroying webkit")
    view.destroy()
    super.onDestroy()
  }

  /// Make back button stay in web browser
  override def onKeyDown(keyCode: Int, event: KeyEvent) = {
    def callSuper() = { super.onKeyDown(keyCode, event); true }

    if (event.getAction == KeyEvent.ACTION_DOWN)
      keyCode match {
        case KeyEvent.KEYCODE_BACK => {
          val view = findView(TR.web_main)
          if (view.canGoBack)
            view.goBack()
          else
            finish()

          true
        }

        case _ =>
          callSuper()
      }
    else
      callSuper()
  }
}

object WebActivity {
  /// Show a URL either in our internal or external browser
  def showURL(context: Context, url: String, external: Boolean = false) {
    val intent = if (external)
      new Intent(Intent.ACTION_VIEW, Uri.parse(url))
    else {
      val intent = new Intent(context, classOf[WebActivity])
      intent.putExtra("url", url)
      intent
    }

    // Needed when launching activities from services
    if (context.isInstanceOf[Service])
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    println("created intent")
    context.startActivity(intent)
  }
}