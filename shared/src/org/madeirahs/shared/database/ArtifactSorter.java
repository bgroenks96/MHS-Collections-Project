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

import java.util.*;

import org.madeirahs.shared.*;
import org.madeirahs.shared.time.*;

/**
 * Implementation of Comparator (generic type Artifact) that sorts Artifacts by
 * set variable and mode.  Variable specifies field and mode specifies order (i.e 
 * Newest -> Oldest vs. Oldest -> Newest).
 * @author Brian Groenke
 *
 */
public class ArtifactSorter implements Comparator<Artifact> {

	Mode mode = Mode.FORWARD;
	Variable var = Variable.TITLE;

	@Override
	public int compare(Artifact o1, Artifact o2) {
		int res = 0;
		switch (var) {
		case TITLE:
			res = compare(o1.title, o2.title);
			break;
		case DONOR:
			res = compare(o1.donor, o2.donor);
			break;
		case MEDIUM:
			res = compare(o1.medium, o2.medium);
			break;
		case OBJ_DATE:
			res = compare(o1.objDate, o2.objDate);
			break;
		case SUB_DATE:
			res = compare(o1.subDate, o2.subDate);
		}
		return res;
	}

	/**
	 * Compares two String attributes of an Artifact.
	 * Accepts sorting modes ALPHABETICAL and REVERSE_ALPHABETICAL.
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int compare(String s1, String s2) {
		int res = 0;
		switch (mode) {
		case FORWARD:
			res = s1.compareToIgnoreCase(s2);
			break;
		case REVERSE:
			res = s2.compareToIgnoreCase(s1);
			break;
		default:
			throw (new IllegalArgumentException(
					"illegal sorting mode for strings"));
		}

		return res;
	}

	/**
	 * Compares two TimeSpec attributes of an Artifact.
	 * Accepts sorting modes NEWEST and OLDEST.
	 * @param t1
	 * @param t2
	 * @return
	 */
	private int compare(TimeSpec t1, TimeSpec t2) {
		int res = 0;
		switch (mode) {
		case FORWARD:
			res = t1.getStartDate().compareTo(t2.getStartDate());
			break;
		case REVERSE:
			res = t2.getStartDate().compareTo(t1.getStartDate());
			break;
		default:
			throw(new IllegalArgumentException("illegal sorting mode for TimeSpec"));
		}
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

		FORWARD, REVERSE;
	}

	public enum Variable {

		TITLE, DONOR, MEDIUM, OBJ_DATE, SUB_DATE;
	}

}
