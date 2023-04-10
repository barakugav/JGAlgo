package com.jgalgo;

public interface RMQ {

	/**
	 * Perform a static preprocessing of a sequence of elements for future RMQ
	 * (Range minimum query) queries
	 *
	 * @param c comparator used to compare between two elements, see the Comparator
	 *          definition below
	 * @param n the number of elements in the sequence
	 */
	public void preprocessRMQ(RMQComparator c, int n);

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

}
