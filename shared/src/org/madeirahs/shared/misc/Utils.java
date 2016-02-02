package org.madeirahs.shared.misc;

import java.io.*;
import java.util.*;

public class Utils {

	/**
	 * Reverses the array so that all the values currently set from front to
	 * back are reset to being back to front.
	 * 
	 * @param array
	 * @return the reversed array.
	 */
	public static <T> T[] flip(T[] array) {
		if (array == null) {
			throw (new NullPointerException("passed array is of null value"));
		}
		T[] copy = Arrays.copyOf(array, array.length);
		int inv = 0;
		for (int i = array.length - 1; i >= 0; i--) {
			array[i] = copy[inv];
			inv++;
		}
		return array;
	}

	/**
	 * Reassigns all of the existing values in the given array to different
	 * (pseudo-random) slots.
	 * 
	 * @param array
	 * @return the randomized array.
	 */
	public static <T> T[] randomize(T[] array) {
		if (array == null) {
			throw (new NullPointerException("passed array is of null value"));
		}
		T[] copy = Arrays.copyOf(array, array.length);
		List<Integer> track = new ArrayList<Integer>();
		for (int i = 0; i < array.length; i++) {
			int rand = -1;
			do {
				rand = (int) (Math.random() * array.length);
			} while (track.contains(rand));

			array[i] = copy[rand];
			track.add(rand);
		}
		return array;
	}

	public static void main(String[] args) {
		System.out
		.println(toString(insert(new Integer[] { 5, 4, 6 }, 1, 10, 12)));
	}

	/**
	 * Finds the "position" of the current array. The position is considered the
	 * next open slot in the array. <br>
	 * If the array has been filled to its capacity, the method returns -1.
	 * 
	 * @param array
	 * @return the next open position in the array or -1 if nothing is
	 *         available.
	 */
	public static <T> int position(T[] array) {
		if (array == null) {
			throw (new NullPointerException("passed array is of null value"));
		}
		boolean filled = false;
		int pos = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				pos = i;
				break;
			} else if (i == array.length - 1) {
				filled = true;
			}
		}
		if (filled) {
			return -1;
		} else {
			return pos;
		}
	}

	public static <T> T[] insert(T[] array, int pos, T... elems) {
		int space = position(array);
		T[] narr = null;
		if (space >= 0 && space + elems.length <= array.length) {
			narr = array;
		} else if (space >= 0) {
			narr = Arrays.copyOf(array, space + elems.length);
		} else {
			narr = Arrays.copyOf(array, array.length + elems.length);
		}
		for (int i = pos; i < pos + elems.length; i++) {
			T prev = narr[i];
			narr[i] = elems[i - pos];
			int ii = i + elems.length;
			if (ii < narr.length) {
				narr[ii] = prev;
			}
		}

		return narr;
	}

	public static <T> String toString(T[] array) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < array.length; i++) {
			String val = null;
			if (array[i] != null) {
				val = array[i].toString();
			}

			if (i == array.length - 1) {
				sb.append(val);
			} else {
				sb.append(val + ", ");
			}
		}
		sb.append(']');
		return sb.toString();
	}
	
	/**
	 * Flips the given map so that the current values are keys and vice versa.
	 * @param map
	 * @return the flipped Map, or null if a new instance of the given Map failed to be created.
	 */
	public static <T, K> Map<K, T> flipMap(Map<T, K> map) {
		try {
			Map<K, T> flipped = map.getClass().newInstance();
			for(T key:map.keySet()) {
				flipped.put(map.get(key), key);
			}
			
			return flipped;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Computes the size of an Object by serializing it to memory and checking
	 * the buffer size.
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static int sizeof(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.close();
		return bos.size();
	}
}
