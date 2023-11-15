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
 * An algorithm for computing the shortest path between two vertices in a graph.
 *
 * <p>
 * Given a graph \(G=(V,E)\), and a weight function \(w:E \rightarrow R\), one might ask what is the shortest path from
 * a <i>source</i> vertex to a <i>target</i> vertex, where the 'shortest' is defined by comparing the sum of edges
 * weights of each path. This interface computes such a path. It differ from the more known
 * {@link ShortestPathSingleSource}, as it does not compute the paths from a source to all vertices, only to a specific
 * target. This might be more efficient in some cases, as less than linear time and space can be used.
 *
 * <p>
 * A variant with a heuristic distance function is also available, see {@link ShortestPathHeuristicST}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    ShortestPathSingleSource
 * @see    ShortestPathAllPairs
 * @see    ShortestPathHeuristicST
 * @author Barak Ugav
 */
public interface ShortestPathST {

	/**
	 * Compute the shortest path from a source vertex to a target vertex.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IPath} object will be returned. In that case, its better to pass a
	 * {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      the graph
	 * @param  w      an edge weight function
	 * @param  source the source vertex
	 * @param  target the target vertex
	 * @return        the shortest path from the source to the target, or {@code null} if there is no path
	 */
	<V, E> Path<V, E> computeShortestPath(Graph<V, E> g, WeightFunction<E> w, V source, V target);

	/**
	 * Create a new S-T shortest path algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathST} object. The
	 * {@link ShortestPathST.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ShortestPathST}
	 */
	static ShortestPathST newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new S-T shortest path algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ShortestPathST} objects
	 */
	static ShortestPathST.Builder newBuilder() {
		return () -> {
			return new ShortestPathST() {
				ShortestPathST cardinalityStSp = new ShortestPathSTBidirectionalBfs();
				ShortestPathST weightedStSp = new ShortestPathSTBidirectionalDijkstra();

				@Override
				public <V, E> Path<V, E> computeShortestPath(Graph<V, E> g, WeightFunction<E> w, V source, V target) {
					if (WeightFunction.isCardinality(w)) {
						return cardinalityStSp.computeShortestPath(g, null, source, target);
					} else {
						return weightedStSp.computeShortestPath(g, w, source, target);
					}
				}
			};
		};
	}

	/**
	 * A builder for {@link ShortestPathST} objects.
	 *
	 * @see    ShortestPathST#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for S-T shortest path computation.
		 *
		 * @return a new S-T shortest path algorithm
		 */
		ShortestPathST build();
	}

}
