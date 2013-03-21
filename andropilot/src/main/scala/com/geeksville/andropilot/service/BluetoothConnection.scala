package com.geeksville.andropilot.service

import android.bluetooth._
import android.content._
import scala.collection.JavaConverters._
import java.util.UUID
import com.ridemission.scandroid.AndroidLogger
import java.io.IOException

class BluetoothConnection(implicit val context: Context) extends AndroidLogger {
  /// Well known ID for bt serial adapters from deal extreme
  val serialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

  private val adapter = BluetoothAdapter.getDefaultAdapter

  def hasBluetooth = adapter != null

  def isEnabled = hasBluetooth && adapter.isEnabled
  //val enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
  //context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

  def foundDevices = {
    val pairedDevices = adapter.getBondedDevices.asScala
    // If there are paired devices
    val r = pairedDevices.find { device =>
      val uuids = device.getUuids.map(_.getUuid)
      debug("BT dev: %s, addr=%s, class=%s, uuids=%s".format(device.getName, device.getAddress, device.getBluetoothClass, uuids.mkString(",")))
      // Add the name and address to an array adapter to show in a ListView
      //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
      uuids.contains(serialUUID)
    }

    debug("Found BT: " + r)
    r
  }

  /**
   * We will call onConnect when a suitable device is found.  The called routine is now responsible for closing the device.
   */
  def connectToDevices(onConnect: BluetoothSocket => Unit) {
    foundDevices.foreach { device =>
      val remoteDev = adapter.getRemoteDevice(device.getAddress)
      try {
        val btSocket = remoteDev.createRfcommSocketToServiceRecord(serialUUID)

        // Block and connect in this thread
        try {
          btSocket.connect()
          onConnect(btSocket)
        } catch {
          case ex: IOException =>
            error("Socket connect failed: " + ex)
            btSocket.close()
        }
      } catch {
        case ex: IOException =>
          error("Socket creation failed")
      }
    }
  }
}