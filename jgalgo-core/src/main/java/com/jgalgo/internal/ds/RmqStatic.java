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

import com.jgalgo.alg.AlgorithmBuilderBase;

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
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
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
	 * This is the recommended way to instantiate a new {@link RmqStatic} object. The {@link RmqStatic.Builder} might
	 * support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link RmqStatic}
	 */
	static RmqStatic newInstance() {
		return builder().build();
	}

	/**
	 * Create a new static range minimum queries algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link RmqStatic} objects
	 */
	static RmqStatic.Builder builder() {
		return new RmqStatic.Builder() {
			String impl;

			@Override
			public RmqStatic build() {
				if (impl != null) {
					switch (impl) {
						case "simple-lookup-table":
							return new RmqStaticSimpleLookupTable();
						case "power-of-2-table":
							return new RmqStaticPowerOf2Table();
						case "cartesian-trees":
							return new RmqStaticCartesianTrees();
						case "plus-minus-one":
							return new RmqStaticPlusMinusOne();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new RmqStaticPowerOf2Table();
			}

			@Override
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						RmqStatic.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link RmqStatic} objects.
	 *
	 * @see    RmqStatic#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new static range minimum queries algorithm.
		 *
		 * @return a new static range minimum queries algorithm
		 */
		RmqStatic build();
	}

}
