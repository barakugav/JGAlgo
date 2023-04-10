package com.jgalgo;

import java.util.Comparator;

@FunctionalInterface
public interface RMQComparator {

	/**
	 * Compare the i'th and j'th elements in the sequence
	 *
	 * @param i index of first element
	 * @param j index of second element
	 * @return value less than zero if the i'th element is smaller than the j'th
	 *         element, value greater than zero if the j'th is smaller than the i'th
	 *         and zero if they are equal
	 */
	public int compare(int i, int j);

	public static <E> RMQComparator ofObjArray(E[] arr) {
		return new RMQComparatorArray.ObjDefaultCmp<>(arr);
	}

	public static <E> RMQComparator ofObjArray(E[] arr, Comparator<? super E> c) {
		return c == null ? new RMQComparatorArray.ObjDefaultCmp<>(arr) : new RMQComparatorArray.ObjCustomCmp<>(arr, c);
	}

	public static RMQComparator ofIntArray(int[] arr) {
		return new RMQComparatorArray.Int(arr);
	}

}
