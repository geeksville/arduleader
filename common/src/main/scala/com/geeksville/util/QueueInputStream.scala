package com.geeksville.util

import scala.collection.mutable.Queue
import java.io.InputStream

/**
 * This is a fairly specialized input stream that pulls data from a scala queue.
 * If the queue ever becomes empty, available will start returning 0.
 */
class QueueInputStream(val queue: Queue[java.lang.Byte]) extends InputStream {
  override def read(): Int = {
    if (queue.isEmpty)
      -1
    else
      (queue.dequeue().toInt & 0xff)
  }

  override def available() = queue.size
}