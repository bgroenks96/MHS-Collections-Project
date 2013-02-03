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

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 * Specifies a type for all time representations both of individual Dates and
 * ranges of time.
 * 
 * @author Brian Groenke
 * 
 */
public abstract class TimeSpec implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4416326614685654122L;

	/**
	 * The String representation to be returned by <code>toString</code> if a
	 * form other than a formatted Date is desired. Setting this value to
	 * anything other than null (the initialized value) will override the date
	 * formatting and instead return this String.
	 */
	public String forcedValue = null;

	/**
	 * If this TimeSpec was created by parsing a syntax String, this is the
	 * unparsed value. This is provided purely as a convenience and is not
	 * required to be set or used.
	 */
	public String syntaxString = null;
	/**
	 * The DateFormat object to be used to format dates.
	 */
	public DateFormat format;

	/**
	 * Default TimeSpec constructor called by subclasses.
	 * 
	 * @param format
	 *            the DateFormat object this TimeSpec should be initialized
	 *            with. Null can be passed as long as <code>forcedValue</code>
	 *            is set to a value other than null before calling
	 *            <code>toString()</code>. Failure to do this could result in
	 *            unpredictable behavior; most likely a NPE.
	 */
	public TimeSpec(DateFormat format) {
		this.format = format;
	}

	/**
	 * Start date for time ranges or the date for a single time.
	 * 
	 * @return the corresponding Date object or null if not specified by this
	 *         TimeSpec
	 */
	public abstract Date getStartDate();

	/**
	 * End date for time ranges or the date for a single time.
	 * 
	 * @return the corresponding Date object or null if not specified by this
	 *         TimeSpec
	 */
	public abstract Date getEndDate();

	/**
	 * Tests to see if this TimeSpec contains or is equivalent to the specified
	 * Date.
	 * 
	 * @return true if the Date lies within or is equivalent to this TimeSpec.
	 */
	public abstract boolean contains(Date date);

	/**
	 * Tests to see if this TimeSpec contains or is equivalent to the specified
	 * Date.
	 * 
	 * @param ts
	 * @return true if the TimeSpec lies within or is equivalent to this
	 *         TimeSpec.o
	 */
	public abstract boolean contains(TimeSpec ts);

	/**
	 * Return a string representation of this TimeSpec for human reading.
	 * The default implementation by TimeSpec simply returns <code>forcedValue</code>.
	 * 
	 * @return a human-comprehensible representation of the time.
	 */
	@Override
	public String toString() {
		return forcedValue;
	}
}
