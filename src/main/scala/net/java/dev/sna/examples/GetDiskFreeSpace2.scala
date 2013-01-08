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
import com.sun.jna.ptr.IntByReference

object GetDiskFreeSpace2 extends SNA {

  snaLibrary = "Kernel32"
  val GetDiskFreeSpaceA = SNA[String, IntByReference, IntByReference, IntByReference, IntByReference, Boolean]

  def main(args: Array[String]) {
    var disk = "Z:\\"
    var spc = new IntByReference // Sectors per cluster
    var bps = new IntByReference // Bytes per sector
    var fc = new IntByReference // Free clusters
    var tc = new IntByReference // Total clusters
    val ok = GetDiskFreeSpaceA(disk, spc, bps, fc, tc)
    printf("'%s' (%s): sectors/cluster: %d,  bytes/sector: %d,  free-clusters: %d,  total/clusters: %d%n",
      disk, ok, spc.getValue, bps.getValue, fc.getValue, tc.getValue)
    var disk2 = "C:\\"
    var ok2 = GetDiskFreeSpaceA(disk2, spc, bps, fc, tc)
    printf("'%s' (%s): sectors/cluster: %d,  bytes/sector: %d,  free-clusters: %d,  total/clusters: %d%n",
      disk2, ok2, spc.getValue, bps.getValue, fc.getValue, tc.getValue)
  }
}
