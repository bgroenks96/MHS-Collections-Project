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

package org.madeirahs.shared.v3d;

import java.io.Serializable;
import java.util.Arrays;

public class V3DBundle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3457537570471342699L;

	/*
	 * Type values are arbitrary. Position values are relevant to accurate
	 * usage.
	 */
	public static final int BUNDLE_TYPE_360 = 360, BUNDLE_TYPE_LEVEL = 270,
			BUNDLE_TYPE_TOP = 101, BUNDLE_TYPE_BOTTOM = 102;

	public static final int TOP = 5, BOTTOM = 4, FRONT = 0, RIGHT = 1,
			BACK = 2, LEFT = 3;

	private String[] imgNames = new String[4];
	private String top, bottom;
	private int bundleType;
	private volatile boolean locked;

	/**
	 * Creates a V3DBundle with front, back, left and right view points. The
	 * type is BUNDLE_TYPE_LEVEL by default. Top and bottom view points can be
	 * added via the <code>setTopBottom(..)</code>, <code>setTopOnly</code> and
	 * <code>setBottomOnly</code> methods.
	 * 
	 * @param front
	 *            location of the front view image relative to whatever file
	 *            system is being used.
	 * @param right
	 *            location of the right view image relative to whatever file
	 *            system is being used.
	 * @param back
	 *            location of the back view image relative to whatever file
	 *            system is being used.
	 * @param left
	 *            location of the left view image relative to whatever file
	 *            system is being used.
	 */
	public V3DBundle(String front, String right, String back, String left) {
		imgNames[0] = front;
		imgNames[1] = right;
		imgNames[2] = back;
		imgNames[3] = left;
		// Make sure that none of the arguments are null
		for (int i = 0; i < imgNames.length; i++) {
			if (imgNames[i] == null) {
				throw (new IllegalArgumentException("null argument(s)"));
			}
		}
		bundleType = BUNDLE_TYPE_LEVEL;
	}

	/**
	 * Sets the location of both the top and bottom view points in this
	 * V3DBundle. This method corresponds to BUNDLE_TYPE_360.
	 * 
	 * @param top
	 *            the location of the top image relative to whatever file system
	 *            is being used.
	 * @param bottom
	 *            the location of the bottom image relative to whatever file
	 *            system is being used.
	 * @throws IllegalArgumentException
	 *             if this V3DBundle has been locked in a final state and can no
	 *             longer be modified.
	 */
	public void setTopBottom(String top, String bottom)
			throws IllegalStateException {
		if (locked) {
			throw (new IllegalStateException(
					"V3DBundle cannot be modified once locked"));
		}
		this.top = top;
		this.bottom = bottom;
		bundleType = BUNDLE_TYPE_360;
	}

	/**
	 * Sets the location of the top view point in this V3DBundle. This method
	 * corresponds to BUNDLE_TYPE_TOP. This method will ensure that the bottom
	 * image value is reset to null.
	 * 
	 * @param top
	 *            the location of the image relative to whatever file system is
	 *            being used.
	 * @throws IllegalArgumentException
	 *             if this V3DBundle has been locked in a final state and can no
	 *             longer be modified.
	 */
	public void setTopOnly(String top) throws IllegalStateException {
		if (locked) {
			throw (new IllegalStateException(
					"V3DBundle cannot be modified once locked"));
		}
		this.top = top;
		bottom = null;
		bundleType = BUNDLE_TYPE_TOP;
	}

	/**
	 * Sets the location of the bottom view point in this V3DBundle. This method
	 * corresponds to BUNDLE_TYPE_BOTTOM. This method will ensure that the top
	 * image value is reset to null.
	 * 
	 * @param bottom
	 *            the location of the image relative to whatever file system is
	 *            being used.
	 * @throws IllegalArgumentException
	 *             if this V3DBundle has been locked in a final state and can no
	 *             longer be modified.
	 */
	public void setBottomOnly(String bottom) throws IllegalStateException {
		if (locked) {
			throw (new IllegalStateException(
					"V3DBundle cannot be modified once locked"));
		}
		this.bottom = bottom;
		top = null;
		bundleType = BUNDLE_TYPE_BOTTOM;
	}

	/**
	 * Discards any values set for the bottom and top image locations and resets
	 * the type to BUNDLE_TYPE_LEVEL.
	 * 
	 * @throws IllegalArgumentException
	 *             if this V3DBundle has been locked in a final state and can no
	 *             longer be modified.
	 */
	public void resetToLevel() throws IllegalStateException {
		if (locked) {
			throw (new IllegalStateException(
					"V3DBundle cannot be modified once locked"));
		}
		this.bottom = null;
		this.top = null;
		bundleType = BUNDLE_TYPE_LEVEL;
	}

	/**
	 * Returns the image-name data of this V3DBundle in a String[]. This method
	 * calls <code>lock()</code> if this V3DBundle has not yet been locked. The
	 * returned array is sorted in correspondence to the TOP, BOTTOM, RIGHT,
	 * LEFT, BACK, FRONT fields.
	 * 
	 * @return an ordered array of String image locations sorted by 3D view
	 *         position.
	 * @see #lock()
	 */
	public String[] getFinalImageArray() {
		if (!locked) {
			lock();
		}
		switch (bundleType) {
		case BUNDLE_TYPE_360:
			String[] imgs360 = Arrays.copyOf(imgNames, 6);
			imgs360[4] = bottom;
			imgs360[5] = top;
			return imgs360;
		case BUNDLE_TYPE_BOTTOM:
			String[] imgslb = Arrays.copyOf(imgNames, 5);
			imgslb[4] = bottom;
			return imgslb;
		case BUNDLE_TYPE_TOP:
			String[] imgslt = Arrays.copyOf(imgNames, 6);
			imgslt[5] = top;
			return imgslt;
		default:
			return imgNames;
		}
	}

	/**
	 * Returns this V3DBundle's type (as specified in class). This method calls
	 * <code>lock()</code> if this V3DBundle has not yet been locked.
	 * 
	 * @return the type specification of this V3DBundle.
	 * @see #lock()
	 */
	public int getBundleType() {
		if (!locked) {
			lock();
		}
		return bundleType;
	}

	/**
	 * Manually lock this V3DBundle so it will no longer be modifiable and can
	 * forever hold its state.
	 */
	public void lock() {
		locked = true;
	}

	/**
	 * Returns the current modification state of the V3DBundle.
	 * 
	 * @return true if this V3DBundle has been locked in a final state, false
	 *         otherwise.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Returns a direct, unlocked copy of this V3DBundle. This method ignores
	 * modification state, so this bundle will be copied regardless of whether
	 * or not lock() has been called and will not affect the current state of
	 * this V3DBundle.
	 */
	public V3DBundle copy() {
		String[] imgcopy = imgNames.clone();
		V3DBundle bundlecopy = new V3DBundle(imgcopy[0], imgcopy[1],
				imgcopy[2], imgcopy[3]);
		if (bundleType == BUNDLE_TYPE_TOP) {
			bundlecopy.setTopOnly(top);
		} else if (bundleType == BUNDLE_TYPE_BOTTOM) {
			bundlecopy.setBottomOnly(bottom);
		} else if (bundleType == BUNDLE_TYPE_360) {
			bundlecopy.setTopBottom(top, bottom);
		}
		return bundlecopy;
	}
}
