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

package org.madeirahs.shared.database;

public class DuplicateArtifactException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7685558499331523850L;

	public DuplicateArtifactException() {
	}

	public DuplicateArtifactException(String message) {
		super(message);
	}

	public DuplicateArtifactException(Throwable cause) {
		super(cause);
	}

	public DuplicateArtifactException(String message, Throwable cause) {
		super(message, cause);
	}

}
