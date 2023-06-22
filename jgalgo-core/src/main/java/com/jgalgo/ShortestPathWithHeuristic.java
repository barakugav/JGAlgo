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
package com.jgalgo;

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
 *
 * @see    ShortestPathSingleSource
 * @author Barak Ugav
 */
public interface ShortestPathWithHeuristic {

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
	 * Create a new heuristic shortest path algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathWithHeuristic} object.
	 *
	 * @return a new builder that can build {@link ShortestPathWithHeuristic} objects
	 */
	static ShortestPathWithHeuristic.Builder newBuilder() {
		return AStar::new;
	}

	/**
	 * A builder for {@link ShortestPathWithHeuristic} objects.
	 *
	 * @see    ShortestPathWithHeuristic#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<ShortestPathWithHeuristic.Builder> {

		/**
		 * Create a new algorithm object for shortest path with heuristic computation.
		 *
		 * @return a new heuristic shortest path algorithm
		 */
		ShortestPathWithHeuristic build();
	}

}
