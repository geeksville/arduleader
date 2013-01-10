/* 
 * Copyright 2010 Sanjay Dasgupta, sanjay.dasgupta@gmail.com
 * 
 * This file is part of SNA.
 *
 * SNA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * SNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SNA.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.java.dev.sna.examples

import net.java.dev.sna.SNA

object GetSystemTimeDemo extends SNA {

  snaLibrary = "Kernel32"
  val GetSystemTime = SNA[GETSYSTEMTIME, Unit]

  def main(args: Array[String]) {
    val gst = new GETSYSTEMTIME
    GetSystemTime(gst)
    printf("Date: %d-%02d-%02d Time: %d:%02d:%02d.%03d GMT%n", gst.wYear, gst.wMonth,
      gst.wDay, gst.wHour, gst.wMinute, gst.wSecond, gst.wMilliseconds)
  }
}
