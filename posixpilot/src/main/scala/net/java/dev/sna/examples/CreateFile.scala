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

object CreateFile extends SNA {

  snaLibrary = "Kernel32"
  val CreateFileA = SNA[String, Int, Int, Object, Int, Int, Int]

  def main(args: Array[String]) {
    val chdl = CreateFileA("\\\\.\\C:",
      0x10000000, // GENERIC_ALL
      3, // sharing - read, write 
      null,
      3, // OPEN_EXISTING
      0x80)
    println(chdl)
    val dhdl = CreateFileA("\\\\.\\D:",
      0x10000000, // GENERIC_ALL
      3, // sharing - read, write 
      null,
      3, // OPEN_EXISTING
      0x80)
    println(dhdl)
  }
}
