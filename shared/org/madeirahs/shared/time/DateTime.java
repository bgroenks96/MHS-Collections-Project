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

package org.madeirahs.shared.time;

import java.text.*;
import java.util.*;

public class DateTime extends TimeSpec {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2478646815892786055L;

	public Date date;

	public DateTime(Date date, DateFormat format) {
		super(format);
		this.date = date;
	}

	@Override
	public Date getStartDate() {
		return date;
	}

	@Override
	public Date getEndDate() {
		return date;
	}

	@Override
	public String toString() {
		if (forcedValue != null) {
			return forcedValue;
		} else {
			return format.format(date);
		}
	}

	@Override
	public boolean contains(Date date) {
		return this.date.compareTo(date) == 0;
	}

	/**
	 * Checks to see if this DateTime is equivalent to the Date returned by
	 * <code>ts.getStartDate()</code>.
	 */
	@Override
	public boolean contains(TimeSpec ts) {
		return this.date.compareTo(ts.getStartDate()) == 0;
	}
}
