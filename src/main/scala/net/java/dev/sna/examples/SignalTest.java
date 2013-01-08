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

package net.java.dev.sna.examples;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Callback;
//import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

interface sig_type extends Callback {
	public void callback(int s);
}

interface CLib extends Library {
	void raise(int s);
	sig_type signal(int s, sig_type f);
}

public class SignalTest {
	public static void main(String[] args) {
		sig_type st = new sig_type() {
			public void callback(int s) {
				System.out.printf("callback(%d) called\n", s);
			}
		};
		CLib lib = (CLib)Native.loadLibrary("msvcrt", CLib.class);
		lib.signal(8, st);
		lib.raise(8);
	}
}
