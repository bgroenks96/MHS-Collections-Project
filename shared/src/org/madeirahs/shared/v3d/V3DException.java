/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright (c) 2012-2016 Madeira Historical Society (developed by Brian Groenke)
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

package org.madeirahs.shared.v3d;

public class V3DException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5135933403853506698L;

	private String[] urls;

	public V3DException(String msg, String url) {
		super(msg);
		urls = new String[] { url };
	}

	public V3DException(String msg, String[] urls) {
		super(msg);
		this.urls = urls;
	}

	public String[] getCausedByImageURLs() {
		return urls;
	}

}
