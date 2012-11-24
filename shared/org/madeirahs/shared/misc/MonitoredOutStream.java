/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright © 2012-  Madeira Historical Society (developed by Brian Groenke)
 *
 *  This library is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.madeirahs.shared.misc;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.ProgressMonitor;

public class MonitoredOutStream extends OutputStream {

	OutputStream stream;
	ProgressMonitor prog;
	long total, max;

	/**
	 * Updates the given ProgressMonitor on the progress of the given OutputStream.
	 * It will not call <code>setValue(int)</code> with a value > 100.
	 * @param out
	 * @param prog
	 * @param max
	 */
	public MonitoredOutStream(OutputStream out, ProgressMonitor prog, long max) {
		if (out == null || prog == null) {
			throw (new IllegalArgumentException("one or more null arguments"));
		}
		this.prog = prog;
		this.stream = out;
		this.max = max;
	}

	@Override
	public void write(int b) throws IOException {
		stream.write(b);
		total++;
		int p = (int) Math.round(((double) total / max) * 100);
		prog.setProgress(p);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}
}
