package com.jgalgo;

/**
 * Static Range Minimum Query (RMQ) algorithm.
 * <p>
 * Given a sequence of {@code n} comparable objects we would like to perform
 * pre-processing and than be able to answer queries of the type: "what is the
 * minimum element in the range [i, j]?" for any indices {@code 0 < i,j <= n}.
 * Algorithm implementing this interface usually require linear or close to
 * linear processing time and try to achieve constant or logarithmic query time.
 * <p>
 * The sequence itself is never passed to the algorithm, rather a
 * {@link RMQStaticComparator} which support comparing two elements given their
 * <i>indices</i> only.
 *
 * @author Barak Ugav
 */
public interface RMQStatic {

	/**
	 * Perform a static pre processing of a sequence of elements for future RMQ
	 * (Range minimum query) queries.
	 *
	 * @param comparator comparator used to compare between two elements, see the
	 *                   Comparator
	 *                   definition below
	 * @param n          the number of elements in the sequence
	 * @return a data structure built from the preprocessing, that can answer RMQ
	 *         queries efficiently
	 */
	RMQStatic.DataStructure preProcessSequence(RMQStaticComparator comparator, int n);

	/**
	 * Data structure result created from a static RMQ pre-processing.
	 *
	 * @author Barak Ugav
	 */
	interface DataStructure {

		/**
		 * Find the minimum element in range [i, j].
		 *
		 * @param i index of range start (including)
		 * @param j index of the range end (including)
		 * @return the index of the minimum element in the range
		 * @throws IllegalArgumentException if either {@code i} or {@code j} are not in
		 *                                  range {@code [0, n)} or if {@code i > j}.
		 */
		int findMinimumInRange(int i, int j);

	}

	/**
	 * Create a new static range minimum queries algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link RMQStatic}
	 * object.
	 *
	 * @return a new builder that can build {@link RMQStatic} objects
	 */
	static RMQStatic.Builder newBuilder() {
		return RMQStaticCartesianTrees::new;
	}

	/**
	 * A builder for {@link RMQStatic} objects.
	 *
	 * @see RMQStatic#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new static range minimum queries algorithm.
		 *
		 * @return a new static range minimum queries algorithm
		 */
		RMQStatic build();
	}

}
