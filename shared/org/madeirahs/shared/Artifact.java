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

package org.madeirahs.shared;

import java.io.*;
import java.text.*;
import java.util.*;

import org.madeirahs.shared.time.*;
import org.madeirahs.shared.v3d.*;

public class Artifact implements Serializable, Comparable<Artifact> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5308353995976856642L;

	public TimeSpec objDate;
	public TimeSpec subDate;
	public String donor;
	public String title;
	public String desc;
	public String medium;
	public String accNum;
	/**
	 * This field denotes resource names on a remote or local file system. Not
	 * searchable.
	 */
	public String[] filenames;
	/**
	 * This field denotes a V3DBundle representitive of resources on a remtoe or
	 * local file system. Not searchable.
	 */
	public V3DBundle bundle;
	/**
	 * This field represents the literal values typed into each field in the
	 * editor program (for loading recovery). Content Specification: 0 = title 1
	 * = donor 2 = sub date 3 = obj date 4 = medium 5 = acc number 6 = desc
	 */
	public String[] fieldValues = new String[7];

	private boolean v3d;

	/**
	 * 
	 * @param objDate
	 *            the date of the object itself.
	 * @param subDate
	 *            the date of submission to the MHS.
	 * @param donor
	 *            name of donor
	 * @param title
	 *            artifact's title
	 * @param medium
	 *            the medium of the artifact
	 * @param accNum
	 *            the accession number
	 * @param desc
	 *            a description
	 */
	public Artifact(TimeSpec objDate, TimeSpec subDate, String donor,
			String title, String medium, String accNum, String desc) {
		this.objDate = objDate;
		this.subDate = subDate;
		this.donor = donor;
		this.title = title;
		this.desc = desc;
		this.accNum = accNum;
		this.medium = medium;
		this.filenames = new String[0];
		Arrays.fill(this.fieldValues, "");
	}

	/**
	 * Creates a new Artifact object that is a copy of the given one.
	 * 
	 * @param model
	 *            the Artifact whose data will be copied into this new one.
	 */
	public Artifact(Artifact model) {
		this.objDate = model.objDate;
		this.subDate = model.subDate;
		this.donor = model.donor;
		this.title = model.title;
		this.desc = model.desc;
		this.accNum = model.accNum;
		this.medium = model.medium;
		this.filenames = model.filenames;
		this.fieldValues = model.fieldValues;
		this.bundle = model.bundle;
		this.v3d = model.v3d;
	}

	/**
	 * 
	 * @param bundle
	 * @throws V3DException
	 */
	public void configure3D(V3DBundle bundle) throws V3DException {
		if (bundle.getFinalImageArray() == null) {
			throw (new V3DException(
					"internal error occurred with V3DBundle - String array was null",
					bundle.getFinalImageArray()));
		}
		this.bundle = bundle;
		v3d = true;
	}

	/**
	 * 
	 * @param imgname
	 */
	public void configureImage(String imgname) {
		filenames = new String[] { imgname };
		v3d = false;
	}

	/**
	 * 
	 * @param files
	 */
	public void configureMultiImage(String[] files) {
		filenames = files;
		v3d = false;
	}

	/**
	 * Checks to see if this Artifact supports the V3D image view.
	 * 
	 * @return true if supported, false otherwise.
	 */
	public boolean is3DSupported() {
		return v3d;
	}
	
	@Override
	public String toString() {
		return accNum;
	}

	public static Artifact createGenericArtifact() {
		return new Artifact(new DateTime(Calendar.getInstance().getTime(),
				DateFormat.getDateInstance(DateFormat.MEDIUM)), new DateTime(
				Calendar.getInstance().getTime(),
				DateFormat.getDateInstance(DateFormat.MEDIUM)), "", "", "", "",
				"");
	}

	/*
	 * \\***----------------------------------------***\\ 
	 * Make sure to update the constructors for each field in the following two enums if Artifact's
	 * field names are changed. Failure to do this will cause catastrophic
	 * problems with the Database search algorithms.
	 * \\***----------------------------------------***\\
	 */

	public static enum TimeField {
		OBJECT_DATE("objDate"), SUBMISSION_DATE("subDate");

		private String fieldName;

		TimeField(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getFieldName() {
			return fieldName;
		}
	}

	public static enum StringField {
		DONOR("donor"), TITLE("title"), DESCRIPTION("desc"), MEDIUM("medium"), ACCESSION_NUMBER(
				"accNum");

		private String fieldName;

		StringField(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getFieldName() {
			return fieldName;
		}
	}

	/**
	 * Calls compareTo on the accession number String values.
	 */
	@Override
	public int compareTo(Artifact o) {
		return accNum.compareTo(o.accNum);
	}

	// --------------------------------------------------
}
