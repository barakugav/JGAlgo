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

import java.util.function.IntToDoubleFunction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;

/**
 * Shortest path algorithm that uses a distance heuristic function.
 * <p>
 * Given a source and target vertices, and a heuristic function that maps each vertex to distance approximation of its
 * distance to the target, the algorithm attempt to find the shortest path from the source to target. An advantage of
 * such algorithm over other {@link ShortestPathSingleSource} algorithms, is that it can terminate much faster for the
 * specific source and target, especially if the heuristic is good.
 * <p>
 * Differing from the regular {@link ShortestPathSingleSource}, algorithms implementing this interface attempt to find
 * the shortest path between a single source and a single target, rather than a single source and all other vertices as
 * targets. Therefore, the algorithm can terminate after performing and using less than linear (in the graph size)
 * operations and space.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    ShortestPathSingleSource
 * @author Barak Ugav
 */
public interface ShortestPathHeuristicST {

	/**
	 * Compute the shortest path between two vertices in a graph.
	 *
	 * @param  g          a graph
	 * @param  w          an edge weight function
	 * @param  source     a source vertex
	 * @param  target     a target vertex
	 * @param  vHeuristic a heuristic function that map each vertex to {@code double}. The heuristic should be close to
	 *                        the real distance of each vertex to the target.
	 * @return            the short path found from {@code source} to {@code target}
	 */
	Path computeShortestPath(Graph g, WeightFunction w, int source, int target, IntToDoubleFunction vHeuristic);

	/**
	 * Create a new shortest path algorithm with heuristic.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathHeuristicST} object. The
	 * {@link ShortestPathHeuristicST.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ShortestPathHeuristicST}
	 */
	static ShortestPathHeuristicST newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new heuristic shortest path algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ShortestPathHeuristicST} objects
	 */
	static ShortestPathHeuristicST.Builder newBuilder() {
		return new ShortestPathHeuristicST.Builder() {

			private String impl;

			@Override
			public ShortestPathHeuristicST build() {
				if (impl != null) {
					switch (impl) {
						case "a-star":
							return new ShortestPathAStar();
						default:
							break;
					}
				}
				return new ShortestPathAStar();
			}

			@Override
			public ShortestPathHeuristicST.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link ShortestPathHeuristicST} objects.
	 *
	 * @see    ShortestPathHeuristicST#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for shortest path with heuristic computation.
		 *
		 * @return a new heuristic shortest path algorithm
		 */
		ShortestPathHeuristicST build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default ShortestPathHeuristicST.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
