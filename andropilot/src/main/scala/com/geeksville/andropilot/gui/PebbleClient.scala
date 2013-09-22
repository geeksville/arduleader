package com.geeksville.andropilot.gui

import android.content.Context
import com.getpebble.android.kit.PebbleKit
import com.getpebble.android.kit.Constants
import com.getpebble.android.kit.util.PebbleDictionary
import android.graphics.BitmapFactory
import com.geeksville.andropilot.R
import android.support.v4.app.FragmentActivity
import android.content.Intent

/**
 *
 */
object PebbleClient {

  private def createMusicIntent = new Intent("com.getpebble.action.NOW_PLAYING")

  def sendMusicToPebble(context: Context, artist: String, track: String, album: String) {
    val i = createMusicIntent
    i.putExtra("artist", artist)
    i.putExtra("album", track)
    i.putExtra("track", album)

    try {
      context.sendBroadcast(i)
    } catch {
      case ex: Throwable =>
        println("ignoring pebble: " + ex)
    }
  }

  /**
   * Does this user seem to have the pebble software
   */
  def hasPebble(context: Context) = context.getPackageManager.queryBroadcastReceivers(createMusicIntent, 0).size != 0
}

/**
 *  A crude mixin to demo using the crummy pebble sport watch api
 */
trait PebbleSportsAppClient {

  def context: Context

  // Send a broadcast to launch the specified application on the connected Pebble
  def startWatchApp() {
    PebbleKit.startAppOnPebble(context, Constants.SPORTS_UUID)

    customizeWatchApp("Andropilot")
    setUnits()
  }

  // Send a broadcast to close the specified application on the connected Pebble
  def stopWatchApp() {
    PebbleKit.closeAppOnPebble(context, Constants.SPORTS_UUID)
  }

  private def setUnits() {
    val data = new PebbleDictionary()
    data.addUint8(Constants.SPORTS_UNITS_KEY, Constants.SPORTS_UNITS_METRIC.toByte)

    PebbleKit.sendDataToPebble(context, Constants.SPORTS_UUID, data);
  }

  // This is pretty disappointing - only true numbers are supported for these fields
  def updateWatchApp() {
    val time = "STAB" //"%02d:%02d".format(1, 2)
    val distance = "15" // "%02.02f".format(32 * 0.0)
    val addl_data = "CAT" // "%02d:%02d".format(3, 4)

    val data = new PebbleDictionary()
    data.addString(Constants.SPORTS_TIME_KEY, time)
    data.addString(Constants.SPORTS_DISTANCE_KEY, distance)
    data.addString(Constants.SPORTS_DATA_KEY, addl_data)
    data.addUint8(Constants.SPORTS_LABEL_KEY, Constants.SPORTS_DATA_PACE)

    PebbleKit.sendDataToPebble(context, Constants.SPORTS_UUID, data)
  }

  private def customizeWatchApp(name: String) {
    val customIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.watch);

    PebbleKit.customizeWatchApp(context, Constants.PebbleAppType.SPORTS, name, customIcon);
  }
}