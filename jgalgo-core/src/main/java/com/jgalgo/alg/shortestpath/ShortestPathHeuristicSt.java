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
package com.jgalgo.alg.shortestpath;

import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleFunction;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Shortest path algorithm that uses a distance heuristic function.
 *
 * <p>
 * Given a source and target vertices, and a heuristic function that maps each vertex to distance approximation of its
 * distance to the target, the algorithm attempt to find the shortest path from the source to target. An advantage of
 * such algorithm over other {@link ShortestPathSingleSource} algorithms, is that it can terminate much faster for the
 * specific source and target, especially if the heuristic is good.
 *
 * <p>
 * Differing from the regular {@link ShortestPathSingleSource}, algorithms implementing this interface attempt to find
 * the shortest path between a single source and a single target, rather than a single source and all other vertices as
 * targets. Therefore, the algorithm can terminate after performing and using less than linear (in the graph size)
 * operations and space.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    ShortestPathSingleSource
 * @author Barak Ugav
 */
public interface ShortestPathHeuristicSt {

	/**
	 * Compute the shortest path between two vertices in a graph.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IPath} object will be returned. In that case, its better to pass a
	 * {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>        the vertices type
	 * @param  <E>        the edges type
	 * @param  g          a graph
	 * @param  w          an edge weight function
	 * @param  source     a source vertex
	 * @param  target     a target vertex
	 * @param  vHeuristic a heuristic function that map each vertex to {@code double}. The heuristic should be close to
	 *                        the real distance of each vertex to the target.
	 * @return            the short path found from {@code source} to {@code target}
	 */
	<V, E> Path<V, E> computeShortestPath(Graph<V, E> g, WeightFunction<E> w, V source, V target,
			ToDoubleFunction<V> vHeuristic);

	/**
	 * Compute the shortest path between two vertices in an int graph.
	 *
	 * @param  g          a graph
	 * @param  w          an edge weight function
	 * @param  source     a source vertex
	 * @param  target     a target vertex
	 * @param  vHeuristic a heuristic function that map each vertex to {@code double}. The heuristic should be close to
	 *                        the real distance of each vertex to the target.
	 * @return            the short path found from {@code source} to {@code target}
	 */
	IPath computeShortestPath(IntGraph g, IWeightFunction w, int source, int target, IntToDoubleFunction vHeuristic);

	/**
	 * Create a new shortest path algorithm with heuristic.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathHeuristicSt} object.
	 *
	 * @return a default implementation of {@link ShortestPathHeuristicSt}
	 */
	static ShortestPathHeuristicSt newInstance() {
		return new ShortestPathAStar();
	}

}
