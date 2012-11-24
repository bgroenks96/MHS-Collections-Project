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

/**
 * Represents an implementation of Comparator used to crawl the Collections
 * Database. Subclasses should override the <code>compare</code> method
 * specified by {@link Comparator} and implement it according to the following
 * specifications: <br>
 * <ul>
 * <li> <code>T a</code> and <code>T b</code> should both be compared to the
 * initialized <code>query</code> variable in this superclass.
 * <li>The return value of <code>compare</code> should be -1 if a is less
 * similar to query than b, 0 if equally similar and 1 if more similar.
 * <li>If either a or b are null, an IllegalArgumentException should be thrown,
 * and comparison should NOT take place.
 * <li>The process by which a and b are compared to query should be similar to
 * the relevance determination in <code>isRelevant</code>. The difference is
 * that <code>isRelevant</code> should return false for completely irrelevant
 * values, while <code>compare</code> should just determine relative similarity.
 * </ul>
 * <br>
 * <br>
 * Note: Due to the third party comparison technique specified by this class, it
 * is not required nor expected that all subclasses meet all {@link Comparator}
 * criteria. Instead, it is left up to the implementor to determine how this
 * criteria should apply to calculating relative similarities between each
 * variable and the query.
 * 
 * @author Brian Groenke
 * 
 * @param <T>
 *            the type of object in the Database this Crawler is looking for.
 */
abstract class DatabaseCrawler<T> implements Comparator<T> {

	protected T query;

	/**
	 * @param a
	 *            the query that this DatabaseCrawler will compare against.
	 */
	protected DatabaseCrawler(T a) {
		if (a == null) {
			throw (new IllegalArgumentException(
					"initialized query value cannot be null"));
		}
		this.query = a;
	}

	@Override
	public abstract int compare(T a, T b);

	public abstract boolean isRelevant(T a);
}
