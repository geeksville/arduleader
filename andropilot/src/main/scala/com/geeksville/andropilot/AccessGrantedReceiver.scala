package com.geeksville.andropilot

import android.content.BroadcastReceiver
import android.content._
import android.hardware.usb._
import android.app._

/**
 * Ask android for access to a USB device, call success when granted
 */
class AccessGrantedReceiver(
  device: UsbDevice,
  success: UsbDevice => Unit,
  failure: UsbDevice => Unit = { throw new Exception("Access denied") })(implicit context: Context) extends BroadcastReceiver {
  val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

  override def onReceive(context: Context, intent: Intent) {
    if (intent.getAction == ACTION_USB_PERMISSION)
      synchronized {
        val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE).asInstanceOf[UsbDevice]

        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
          success(device)
        else
          failure(device)
      }
  }

  /**
   * Start listening for permission
   */
  def requestPermission() {
    val manager = context.getSystemService(Context.USB_SERVICE).asInstanceOf[UsbManager]

    val intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0)
    val filter = new IntentFilter(ACTION_USB_PERMISSION)
    context.registerReceiver(this, filter)

    manager.requestPermission(device, intent)
  }
}

