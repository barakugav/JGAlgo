/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.internal.ds;

/**
 * Static Range Minimum Query (RMQ) algorithm.
 *
 * <p>
 * Given a sequence of \(n\) comparable objects we would like to perform pre-processing and than be able to answer
 * queries of the type: "what is the minimum element in the range \([i, j]\)?" for any indices \(0 \leq i \leq j \leq
 * n\). Algorithm implementing this interface usually require linear or close to linear processing time and try to
 * achieve constant or logarithmic query time.
 *
 * <p>
 * The sequence itself is never passed to the algorithm, rather a {@link RmqStaticComparator} which support comparing
 * two elements given their <i>indices</i> only.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @author Barak Ugav
 */
public interface RmqStatic {

	/**
	 * Perform a static pre processing of a sequence of elements for future RMQ (Range minimum query) queries.
	 *
	 * @param  comparator comparator used to compare between two elements, see the Comparator definition below
	 * @param  n          the number of elements in the sequence
	 * @return            a data structure built from the preprocessing, that can answer RMQ queries efficiently
	 */
	RmqStatic.DataStructure preProcessSequence(RmqStaticComparator comparator, int n);

	/**
	 * Data structure result created from a static RMQ pre-processing.
	 *
	 * @author Barak Ugav
	 */
	interface DataStructure {

		/**
		 * Find the minimum element in range [i, j].
		 *
		 * @param  i                        index of range start (including)
		 * @param  j                        index of the range end (including)
		 * @return                          the index of the minimum element in the range
		 * @throws IllegalArgumentException if either {@code i} or {@code j} are not in range {@code [0, n)} or if
		 *                                      {@code i > j}.
		 */
		int findMinimumInRange(int i, int j);

		/**
		 * Get the size of the data structure in bytes.
		 *
		 * @return the size of the data structure in bytes
		 */
		long sizeInBytes();

	}

	/**
	 * Create a new RMQ algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link RmqStatic} object.
	 *
	 * @return a default implementation of {@link RmqStatic}
	 */
	static RmqStatic newInstance() {
		return new RmqStaticPowerOf2Table();
	}

}
