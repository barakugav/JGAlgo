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
package com.jgalgo.alg.path;

import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

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
 * A variant with a heuristic distance function is also available, see {@link ShortestPathHeuristicSt}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    ShortestPathSingleSource
 * @see    ShortestPathAllPairs
 * @see    ShortestPathHeuristicSt
 * @author Barak Ugav
 */
public interface ShortestPathSt {

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
	default <V, E> Path<V, E> computeShortestPath(Graph<V, E> g, WeightFunction<E> w, V source, V target) {
		ObjectDoublePair<Path<V, E>> sp = computeShortestPathAndWeight(g, w, source, target);
		return sp != null ? sp.first() : null;
	}

	/**
	 * Compute the shortest path from a source vertex to a target vertex, and its weight.
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
	 * @return        a pair of the shortest path from the source to the target, and its weight, or {@code null} if
	 *                there is no path
	 */
	<V, E> ObjectDoublePair<Path<V, E>> computeShortestPathAndWeight(Graph<V, E> g, WeightFunction<E> w, V source,
			V target);

	/**
	 * Create a new S-T shortest path algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathSt} object.
	 *
	 * @return a default implementation of {@link ShortestPathSt}
	 */
	static ShortestPathSt newInstance() {
		return new ShortestPathSt() {
			ShortestPathSt cardinalityStSp = new ShortestPathStBidirectionalBfs();
			ShortestPathSt weightedStSp = new ShortestPathStBidirectionalDijkstra();

			@Override
			public <V, E> ObjectDoublePair<Path<V, E>> computeShortestPathAndWeight(Graph<V, E> g, WeightFunction<E> w,
					V source, V target) {
				if (WeightFunction.isCardinality(w)) {
					return cardinalityStSp.computeShortestPathAndWeight(g, null, source, target);
				} else {
					return weightedStSp.computeShortestPathAndWeight(g, w, source, target);
				}
			}
		};
	}

}
