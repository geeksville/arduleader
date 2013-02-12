package com.geeksville.util

import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

/**
 * Listens on a particular TCP port number, firing up connection handers when connections arrive
 */
class TCPListener(val portNum: Int, val handleConnection: Socket => Unit) {
  val queueLen = 10
  private val serverSocket = new ServerSocket(portNum, queueLen)
  private val listenerThread = ThreadTools.createDaemon("RESTServe")(readerFunct)

  listenerThread.start()

  /// Shut down this server
  def close() {
    serverSocket.close() // Should cause the thread to die
  }

  private def readerFunct() {
    try {
      while (true) {
        val socket = serverSocket.accept()

        handleConnection(socket)
      }
    } catch {
      case x: SocketException =>
        println("REST socket has been closed, exiting receiver...")
    }
  }
}