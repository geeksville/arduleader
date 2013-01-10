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
import com.sun.jna.Callback
import com.sun.jna.Platform

trait SignalType extends Callback {
  def invoke(signal: Int) {
    printf("invoke (%d) called on %s\n", signal, this.toString)
  }
}

object Signal extends SNA {

  snaLibrary = if (Platform.isWindows) "msvcrt" else "c"
  val signal = SNA[Int, SignalType, SignalType]
  val raise = SNA[Int, Unit]

  def main(args: Array[String]) {
    val hdlr = new Object with SignalType
    for (i <- 0 to 32) {
      val old_sig = signal(i, hdlr)
      printf("%d -> old_sig: %s\n", i, old_sig)
      raise(i)
    }
  }
}
