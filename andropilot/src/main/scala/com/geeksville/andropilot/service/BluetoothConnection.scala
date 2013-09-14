package com.geeksville.andropilot.service

import android.bluetooth._
import android.content._
import scala.collection.JavaConverters._
import java.util.UUID
import com.ridemission.scandroid.AndroidLogger
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.FilterInputStream

trait BluetoothConnection extends Context with AndroidLogger {
  /// Well known ID for bt serial adapters from deal extreme
  // val serialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
  // Supposedly all serial devices start with this UUID
  val serialUUIDprefix = "00001101-"

  private val adapter = BluetoothAdapter.getDefaultAdapter

  def hasBluetooth = adapter != null

  def isEnabled = hasBluetooth && adapter.isEnabled
  //val enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
  //context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

  private def getUUID(device: BluetoothDevice) = {
    val uuids = Option(device.getUuids).getOrElse(Array()).map(_.getUuid)
    uuids.find { uuid => uuid.toString.startsWith(serialUUIDprefix) }
  }

  /// Do we see a controllable bluetooth device out there?
  def bluetoothAdapterPresent = isEnabled && !foundDevices.isEmpty

  def foundDevices = {
    val pairedDevices = adapter.getBondedDevices.asScala
    // If there are paired devices
    val r = pairedDevices.find { device =>

      // Fix an auto bug, some devices out there seem able to return null for getUuids or getUUid
      val uuids = Option(device.getUuids).getOrElse(Array()).map(_.getUuid.toString)
      debug("BT dev: %s, addr=%s, class=%s, uuids=%s".format(device.getName, device.getAddress, device.getBluetoothClass, uuids.mkString(",")))

      // Add the name and address to an array adapter to show in a ListView
      //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
      getUUID(device).isDefined
    }

    debug("Found BT: " + r)
    r
  }

  /**
   *  The called routine is now responsible for closing the input stream (which will implicitly close the device)
   */
  protected def onBluetoothConnect(in: InputStream, out: OutputStream)

  protected def onBluetoothDisconnect()

  private class BTInputWrapper(val s: BluetoothSocket) extends FilterInputStream(s.getInputStream) {
    override def close() {
      warn("Closing bluetooth device")
      try {
        super.close()
        s.close()
        onBluetoothDisconnect()
      } catch {
        case ex: IOException =>
          error("Ignoring BT error on close: " + ex)
      }
    }
  }

  /**
   * We will call onConnect when a suitable device is found.
   */
  def connectToDevices() {
    if (isEnabled)
      foundDevices.foreach { device =>
        val remoteDev = adapter.getRemoteDevice(device.getAddress)
        adapter.cancelDiscovery()

        try {
          val btSocket = remoteDev.createRfcommSocketToServiceRecord(getUUID(device).get)

          // Block and connect in this thread
          try {
            btSocket.connect()
            onBluetoothConnect(new BTInputWrapper(btSocket), btSocket.getOutputStream)
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

