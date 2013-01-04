// vim: expandtab shiftwidth=4 softtabstop=4
/*
 * Copyright (c) 2012, Yang Bo. All rights reserved.
 * Copyright (c) 2002-2007, Marc Prud'hommeaux. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package com.geeksville.shell;

import static com.geeksville.shell.ANSITerminal.UnixKey.ARROW_DOWN;
import static com.geeksville.shell.ANSITerminal.UnixKey.ARROW_LEFT;
import static com.geeksville.shell.ANSITerminal.UnixKey.ARROW_PREFIX;
import static com.geeksville.shell.ANSITerminal.UnixKey.ARROW_RIGHT;
import static com.geeksville.shell.ANSITerminal.UnixKey.ARROW_START;
import static com.geeksville.shell.ANSITerminal.UnixKey.ARROW_UP;
import static com.geeksville.shell.ANSITerminal.UnixKey.DEL;
import static com.geeksville.shell.ANSITerminal.UnixKey.DEL_THIRD;
import static com.geeksville.shell.ANSITerminal.UnixKey.END_CODE;
import static com.geeksville.shell.ANSITerminal.UnixKey.HOME_CODE;
import static com.geeksville.shell.ANSITerminal.UnixKey.O_PREFIX;
import static scala.tools.jline.console.Key.BACKSPACE;
import static scala.tools.jline.console.Key.CTRL_A;
import static scala.tools.jline.console.Key.CTRL_B;
import static scala.tools.jline.console.Key.CTRL_E;
import static scala.tools.jline.console.Key.CTRL_F;
import static scala.tools.jline.console.Key.CTRL_N;
import static scala.tools.jline.console.Key.CTRL_O;
import static scala.tools.jline.console.Key.CTRL_P;
import static scala.tools.jline.console.Key.CTRL_T;
import static scala.tools.jline.console.Key.CTRL_W;
import static scala.tools.jline.console.Key.CTRL_X;
import static scala.tools.jline.console.Key.DELETE;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import scala.tools.jline.TerminalSupport;
import scala.tools.jline.console.Key;
import scala.tools.jline.internal.Configuration;
import scala.tools.jline.internal.ReplayPrefixOneCharInputStream;

/**
 * SshTerminal is like UnixTerminal, but it does not execute stty.
 * 
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:dwkemp@gmail.com">Dale Kemp</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:jbonofre@apache.org">Jean-Baptiste Onofrè</a>
 * @author <a href="mailto:pop.atry@gmail.com">杨博</a>
 * @since 2.0
 */
public class ANSITerminal extends TerminalSupport {
	private final ReplayPrefixOneCharInputStream replayStream;

	private final InputStreamReader replayReader;

	public ANSITerminal() throws Exception {
		super(true);

		this.replayStream = new ReplayPrefixOneCharInputStream(
				Configuration.getInputEncoding());
		this.replayReader = new InputStreamReader(replayStream,
				replayStream.getEncoding());
	}

	/**
	 * Remove line-buffered input by invoking "stty -icanon min 1" against the
	 * current terminal.
	 */
	@Override
	public void init() throws Exception {
		super.init();

		setAnsiSupported(true);

		setEchoEnabled(false);
	}

	@Override
	public int readVirtualKey(final InputStream in) throws IOException {
		int c = readCharacter(in);

		if (Key.valueOf(c) == DELETE) {
			c = BACKSPACE.code;
		}

		UnixKey key = UnixKey.valueOf(c);

		// in Unix terminals, arrow keys are represented by a sequence of 3
		// characters. E.g., the up arrow key yields 27, 91, 68
		if (key == ARROW_START) {
			// also the escape key is 27 thats why we read until we have
			// something different than 27
			// this is a bugfix, because otherwise pressing escape and than an
			// arrow key was an undefined state
			while (key == ARROW_START) {
				c = readCharacter(in);
				key = UnixKey.valueOf(c);
			}

			if (key == ARROW_PREFIX || key == O_PREFIX) {
				c = readCharacter(in);
				key = UnixKey.valueOf(c);

				if (key == ARROW_UP) {
					return CTRL_P.code;
				} else if (key == ARROW_DOWN) {
					return CTRL_N.code;
				} else if (key == ARROW_LEFT) {
					return CTRL_B.code;
				} else if (key == ARROW_RIGHT) {
					return CTRL_F.code;
				} else if (key == HOME_CODE) {
					return CTRL_A.code;
				} else if (key == END_CODE) {
					return CTRL_E.code;
				} else if (key == DEL_THIRD) {
					readCharacter(in); // read 4th & ignore
					return DELETE.code;
				}
			} else if (c == 'b') { // alt-b: go back a word
				return CTRL_O.code; // PREV_WORD
			} else if (c == 'f') { // alt-f: go forward a word
				return CTRL_T.code; // NEXT_WORD
			} else if (key == DEL) { // alt-backspace: delete previous word
				return CTRL_W.code; // DELETE_PREV_WORD
			} else if (c == 'd') { // alt-d: delete next word
				return CTRL_X.code; // DELETE_NEXT_WORD
			}

		}

		// handle unicode characters, thanks for a patch from amyi@inf.ed.ac.uk
		if (c > 128) {
			// handle unicode characters longer than 2 bytes,
			// thanks to Marc.Herbert@continuent.com
			replayStream.setInput(c, in);
			// replayReader = new InputStreamReader(replayStream, encoding);
			c = replayReader.read();
		}

		return c;
	}

	/**
	 * Unix keys.
	 */
	public static enum UnixKey {
		ARROW_START(27),

		ARROW_PREFIX(91),

		ARROW_LEFT(68),

		ARROW_RIGHT(67),

		ARROW_UP(65),

		ARROW_DOWN(66),

		O_PREFIX(79),

		HOME_CODE(72),

		END_CODE(70),

		DEL_THIRD(51),

		DEL_SECOND(126),

		DEL(127);

		public final short code;

		UnixKey(final int code) {
			this.code = (short) code;
		}

		private static final Map<Short, UnixKey> codes;

		static {
			Map<Short, UnixKey> map = new HashMap<Short, UnixKey>();

			for (UnixKey key : UnixKey.values()) {
				map.put(key.code, key);
			}

			codes = map;
		}

		public static UnixKey valueOf(final int code) {
			return codes.get((short) code);
		}
	}
}