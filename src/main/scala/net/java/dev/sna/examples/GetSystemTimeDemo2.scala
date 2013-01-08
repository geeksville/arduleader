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

object GetSystemTimeDemo2 extends SNA {

  snaLibrary = "Kernel32"
  val GetSystemTime = SNA[GETSYSTEMTIME, Unit]

  def main(args: Array[String]) {
    val gst = new GETSYSTEMTIME
    GetSystemTime(gst)
    val shorts = gst.getPointer.getShortArray(0, 8)
    for (sh <- shorts)
      printf("%d ", sh)
    println
  }
}
