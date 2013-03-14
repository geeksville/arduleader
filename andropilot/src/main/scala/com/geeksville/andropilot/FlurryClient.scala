package com.geeksville.andropilot

import android.content.Context
import android.app.Activity
import android.app.Service

import com.flurry.android._

import scala.collection.JavaConverters._

/// scala glue to make for easy use of the Flurry API
trait FlurryClient {

  def flurryUserId(userid: String) = FlurryAgent.setUserId(userid)
  def flurryAge(age: Int) = FlurryAgent.setAge(age)
  def flurryIsMale(isMale: Boolean) = FlurryAgent.setGender(if (isMale) Constants.MALE else Constants.FEMALE)

  /// Record a trackable event
  def usageEvent(label: String, params: Pair[String, String]*) =
    FlurryAgent.logEvent(label, Map(params: _*).asJava)

  def beginTimedEvent(label: String, params: Pair[String, String]*) =
    FlurryAgent.logEvent(label, Map(params: _*).asJava, true)

  def endTimedEvent(label: String) {
    FlurryAgent.endTimedEvent(label)
  }

  /// Record some sort of unexpected exception
  def usageException(label: String, ex: Throwable) =
    FlurryAgent.onError(label, ex.getMessage, ex.toString)
}

/// scala glue to make for easy use of the Flurry API
trait FlurryContext extends Context with FlurryClient {
  protected def startFlurry() = FlurryAgent.onStartSession(this, "V27VCXPK295XWGZCQG85")

  protected def endFlurry() = FlurryAgent.onEndSession(this)
}

/// A mixin to add Flurry support to Activities 
trait FlurryActivity extends Activity with FlurryContext {
  override def onStart() {
    super.onStart()
    startFlurry()
  }

  override def onStop() {
    endFlurry()
    super.onStop()
  }
}

/// A mixin to add Flurry support to Services
trait FlurryService extends Service with FlurryContext {

  override def onCreate() {
    super.onCreate()
    startFlurry()
  }

  override def onDestroy() {
    endFlurry()
    super.onCreate()
  }
}
