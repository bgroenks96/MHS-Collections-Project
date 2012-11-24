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

import java.util.Comparator;

import org.madeirahs.shared.Artifact;
import org.madeirahs.shared.time.TimeSpec;

public class ArtifactSorter implements Comparator<Artifact> {

	Mode mode = Mode.ALPHABETICAL;
	Variable var = Variable.TITLE;

	@Override
	public int compare(Artifact o1, Artifact o2) {
		int res = 0;
		switch (var) {
		case TITLE:
			res = compare(o1.title, o2.title);
		case DONOR:
			res = compare(o1.donor, o2.donor);
		case MEDIUM:
			res = compare(o1.medium, o2.medium);
		case OBJ_DATE:
			res = compare(o1.objDate, o2.objDate);
		case SUB_DATE:
			res = compare(o1.subDate, o2.subDate);
		}
		return res;
	}

	public int compare(String s1, String s2) {
		int res = 0;
		switch (mode) {
		case ALPHABETICAL:

			break;
		case REVERSE_ALPHABETICAL:

			break;
		default:
			throw (new IllegalArgumentException(
					"illegal sorting mode for strings"));
		}

		return res;
	}

	public int compare(TimeSpec t1, TimeSpec t2) {
		int res = 0;

		return res;
	}

	public void setMode(Mode mode) {
		if (mode != null) {
			this.mode = mode;
		}
	}

	public void setVar(Variable field) {
		if (field != null) {
			this.var = field;
		}
	}

	public enum Mode {

		ALPHABETICAL, REVERSE_ALPHABETICAL, NEWEST, OLDEST;
	}

	public enum Variable {

		TITLE, DONOR, MEDIUM, OBJ_DATE, SUB_DATE;
	}

}
