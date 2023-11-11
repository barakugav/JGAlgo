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

package com.jgalgo.alg;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Algorithm that find the cycle with the minimum mean weight.
 *
 * <p>
 * Given a graph \(G\), a cycle in \(G\) is a sequence of edges that form a path, and its first edge source is also its
 * last edge target. Given an edge weight function, we can define for each such cycle its mean weight, by summing its
 * edges weights and dividing by its length (the number of edges in the cycle). Algorithms implementing this interface
 * find the cycle with the minimum mean weight among all the cycles in the given graph.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface MinimumMeanCycle {

	/**
	 * Compute the minimum mean cycle in a graph.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IPath} object will be returned. In that case, its better to pass a
	 * {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @param  w   an edge weight function
	 * @return     the cycle with the minimum mean weight in the graph, or {@code null} if no cycles were found
	 */
	<V, E> Path<V, E> computeMinimumMeanCycle(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Create a new min mean cycle algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumMeanCycle} object. The
	 * {@link MinimumMeanCycle.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MinimumMeanCycle}
	 */
	static MinimumMeanCycle newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new minimum mean cycle algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumMeanCycle} objects
	 */
	static MinimumMeanCycle.Builder newBuilder() {
		return new MinimumMeanCycle.Builder() {
			String impl;

			@Override
			public MinimumMeanCycle build() {
				if (impl != null) {
					switch (impl) {
						case "dasdan-gupta":
							return new MinimumMeanCycleDasdanGupta();
						case "howard":
							return new MinimumMeanCycleHoward();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new MinimumMeanCycleHoward();
			}

			@Override
			public MinimumMeanCycle.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						MinimumMeanCycle.Builder.super.setOption(key, value);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MinimumMeanCycle} objects.
	 *
	 * @see    MinimumMeanCycle#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum mean cycle computation.
		 *
		 * @return a new minimum mean cycle algorithm
		 */
		MinimumMeanCycle build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 *
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 *
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default MinimumMeanCycle.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
