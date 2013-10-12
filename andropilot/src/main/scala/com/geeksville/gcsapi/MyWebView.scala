package com.geeksville.gcsapi

import android.webkit._
import android.util._
import android.content._

// work around for android bug http://stackoverflow.com/questions/7156420/how-can-i-destroy-the-webview-activity-and-the-video-in-webview
class MyWebView(context: Context, attrs: AttributeSet) extends WebView(context.getApplicationContext(), attrs) {
}