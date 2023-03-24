package com.ugav.jgalgo;

import java.util.Objects;

public interface RMQ {

	/**
	 * Perform a static preprocessing of a sequence of elements for future RMQ
	 * (Range minimum query) queries
	 *
	 * @param c comparator used to compare between two elements, see the Comparator
	 *          definition below
	 * @param n the number of elements in the sequence
	 */
	public void preprocessRMQ(Comparator c, int n);

	/**
	 * Calculate the minimum element in range [i, j)
	 *
	 * Can be called only after preprocessing of an array
	 *
	 * @param i index of range start (including)
	 * @param j index of the range end (excluding)
	 * @return index of the minimum element in the range
	 */
	public int calcRMQ(int i, int j);

	@FunctionalInterface
	public static interface Comparator {

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

	}

	public static class ArrayComparator<E> implements Comparator {

		private final E[] arr;
		private final java.util.Comparator<? super E> c;

		public ArrayComparator(E[] arr, java.util.Comparator<? super E> c) {
			this.arr = Objects.requireNonNull(arr);
			this.c = Objects.requireNonNull(c);
		}

		@Override
		public int compare(int i, int j) {
			return c.compare(arr[i], arr[j]);
		}

	}

	public static class ArrayIntComparator implements Comparator {

		private final int[] arr;

		public ArrayIntComparator(int[] arr) {
			this.arr = Objects.requireNonNull(arr);
		}

		@Override
		public int compare(int i, int j) {
			return Integer.compare(arr[i], arr[j]);
		}

	}

}
