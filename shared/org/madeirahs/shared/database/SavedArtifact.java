/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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

import java.io.Serializable;
import java.util.Map;

import org.madeirahs.shared.Artifact;

/**
 * Object that is serialized when saving Artifact data to the local system. This
 * is necessary to keep track of resources on the local system that the
 * ArtifactView uses and/or store field values for editor documents. This class
 * is NOT nor was it intended to be a subclass of (or even related to)
 * org.madeirahs.shared.Artifact
 * 
 * @author Brian Groenke
 * 
 */
public class SavedArtifact implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3234380396591455457L;

	public Artifact element;
	public Map<String, String> nameMap;
	public boolean loadedRemote;

	/**
	 * Create a SaveData artifact within the MainUI class to represent saved
	 * Artifact data as well as resources on the local file system it uses. None
	 * of the values may be null; <br>
	 * <br>
	 * Note: This constructor instantiates this SavedArtifact as being local.
	 * This local setting is immutable and cannot be altered later. However, the
	 * rest of the instance variables are public and may be freely edited.
	 * 
	 * @param element
	 *            Artifact object to save.
	 * @param nameMap
	 *            HashMap that maps file names to their respective fully
	 *            qualified File on the system.
	 * @param strings
	 *            all field values in a varargs
	 */
	public SavedArtifact(Artifact element, Map<String, String> nameMap,
			boolean loadedRemote, String... strings) {
		if (element == null || nameMap == null || strings == null) {
			throw (new IllegalArgumentException(
					"cannot instantiate SaveData with null data"));
		}
		this.element = element;
		this.nameMap = nameMap;
		this.element.fieldValues = strings;
		this.loadedRemote = loadedRemote;
	}
}
