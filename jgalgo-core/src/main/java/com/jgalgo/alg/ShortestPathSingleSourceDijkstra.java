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

import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.IndexHeapDouble;
import com.jgalgo.internal.util.Assertions;

/**
 * Dijkstra's algorithm for Single Source Shortest Path (SSSP).
 *
 * <p>
 * Compute the shortest paths from a single source to all other vertices in \(O(m + n \log n)\) time, using a heap with
 * \(O(1)\) time for {@code decreaseKey()} operations.
 *
 * <p>
 * Only positive edge weights are supported. This implementation should be the first choice for
 * {@link ShortestPathSingleSource} with positive weights. For negative weights use
 * {@link ShortestPathSingleSourceBellmanFord} for floating points or {@link ShortestPathSingleSourceGoldberg} for
 * integers.
 *
 * <p>
 * Based on 'A note on two problems in connexion with graphs' by E. W. Dijkstra (1959). A 'note'??!! this guy changed
 * the world, and he publish it as a 'note'.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class ShortestPathSingleSourceDijkstra extends ShortestPathSingleSourceUtils.AbstractImpl {

	/**
	 * Construct a new SSSP algorithm.
	 */
	ShortestPathSingleSourceDijkstra() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if one of the edge weights is negative
	 */
	@Override
	ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g, IWeightFunction w, int source) {
		w = IWeightFunction.replaceNullWeightFunc(w);
		ShortestPathSingleSourceUtils.IndexResult res = new ShortestPathSingleSourceUtils.IndexResult(g, source);
		res.distances[source] = 0;
		IndexHeapDouble heap = IndexHeapDouble.newInstance(res.distances);

		for (int u = source;;) {
			final double uDistance = heap.key(u);
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (heap.key(v) != Double.POSITIVE_INFINITY)
					continue;
				double ew = w.weight(e);
				Assertions.onlyPositiveWeight(ew);
				double distance = uDistance + ew;

				if (!heap.isInserted(v)) {
					heap.insert(v, distance);
					res.backtrack[v] = e;
				} else if (distance < heap.key(v)) {
					heap.decreaseKey(v, distance);
					res.backtrack[v] = e;
				}
			}

			if (heap.isEmpty())
				break;
			u = heap.extractMin();
		}

		return res;
	}

}
