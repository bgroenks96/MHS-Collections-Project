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

package org.madeirahs.shared.database;

import org.madeirahs.shared.Artifact;

/**
 * * {@deprecated This class is no longer needed, as LocalArtifact was
 * redesigned to be SavedArtifact, which can function with or without local file
 * data.} Allows the editor to regain exact field entry data from an artifact
 * saved on the remote server. This is essentially a stripped down version of
 * the LocalArtifact class, excluding the file Vector/Map, as they are
 * irrelevant for files loaded from the server (not on the local file system).
 * 
 * @author Brian Groenke
 * 
 */
@Deprecated
public class RemoteArtifact {

	public Artifact element;
	public String[] fieldValues;

	/**
	 * Create a RemoteArtifact that can be attached to an Artifact in the remote
	 * database. The purpose of this constructor is to allow remote usage of
	 * this class so that it does not depend on the MainUI class instance
	 * variables. None of the values may be null.
	 * 
	 * @param element
	 *            Artifact object to save.
	 */
	public RemoteArtifact(Artifact element, String... strings) {
		if (element == null || strings == null) {
			throw (new IllegalArgumentException(
					"cannot instantiate SaveData with null data"));
		}
		this.element = element;
		this.fieldValues = strings;
	}
}
