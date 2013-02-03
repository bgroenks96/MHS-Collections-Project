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

import java.text.DateFormat;
import java.util.Date;

public class TimeFrame extends TimeSpec {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8548139564319412099L;

	public Date start, end;

	/**
	 * Creates a new TimeFrame with given start and end times. Null values
	 * indicate infinity.
	 * 
	 * @param start
	 *            The time that should be used as the start date.
	 * @param end
	 *            The time that should be used as the end date.
	 */
	public TimeFrame(Date start, Date end, DateFormat format) {
		super(format);
		if (end != null && end.compareTo(start) <= 0) {
			throw (new IllegalArgumentException(
					"End date cannot precede or match start date"));
		}
		this.start = start;
		this.end = end;
	}

	/**
	 * Tests to see if the ceiling (end date) of this TimeFrame is infinite
	 * (indicated by null value).
	 * 
	 * @return true if the ceiling is infinite, false otherwise.
	 */
	public boolean hasInfiniteCeiling() {
		if (end == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tests to see if the floor (start date) of this TimeFrame is infinite
	 * (indicated by null value).
	 * 
	 * @return true if the floor is infinite, false otherwise.
	 */
	public boolean hasInfiniteFloor() {
		if (start == null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Date getStartDate() {
		return start;
	}

	@Override
	public Date getEndDate() {
		return end;
	}

	@Override
	public String toString() {
		if (forcedValue != null) {
			return forcedValue;
		} else {
			String form = format.format(start) + "-" + format.format(end);
			return form;
		}
	}

	@Override
	public boolean contains(Date d) {
		return (((start != null) ? start.compareTo(d) : Long.MIN_VALUE) <= 0)
				&& (((end != null) ? end.compareTo(d) : Long.MAX_VALUE) >= 0);
	}

	@Override
	public boolean contains(TimeSpec ts) {
		return (((start != null) ? start.compareTo(ts.getStartDate())
				: Long.MIN_VALUE) <= 0)
				&& (((end != null) ? end.compareTo(ts.getEndDate())
						: Long.MAX_VALUE) >= 0);
	}
}
