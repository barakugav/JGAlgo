package com.jgalgo;

/**
 * Range Minimum Query (RMQ) algorithm.
 * <p>
 * Given a sequence of
 *
 * @author Barak Ugav
 */
public interface RMQStatic {

	/**
	 * Perform a static pre processing of a sequence of elements for future RMQ
	 * (Range minimum query) queries
	 *
	 * @param c comparator used to compare between two elements, see the Comparator
	 *          definition below
	 * @param n the number of elements in the sequence
	 */
	public RMQStatic.DataStructure preProcessSequence(RMQComparator c, int n);

	interface DataStructure {

		/**
		 * Calculate the minimum element in range [i, j)
		 *
		 * Can be called only after pre processing of an array
		 *
		 * @param i index of range start (including)
		 * @param j index of the range end (excluding)
		 * @return index of the minimum element in the range
		 */
		public int findMinimumInRange(int i, int j);

	}

}
