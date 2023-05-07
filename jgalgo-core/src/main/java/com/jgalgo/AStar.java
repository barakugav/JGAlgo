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

import java.util.Objects;
import java.util.function.IntToDoubleFunction;

import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * A* shortest path algorithm.
 * <p>
 * The A star (\(A^*\)) algorithm try to find the shortest path from a source to target vertex. It uses a heuristic that
 * map a vertex to an estimation of its distance from the target position.
 * <p>
 * An advantage of the \(A^*\) algorithm over other {@link SSSP} algorithm, is that it can terminate much faster for the
 * specific source and target, especially if the heuristic is good.
 * <p>
 * The running time of this algorithm is \(O(m + n \log n)\) in the worse case, and it uses linear space.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/A*_search_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class AStar {

	private HeapReferenceable.Builder heapBuilder = HeapPairing::new;

	/**
	 * Construct a new AStart algorithm.
	 */
	public AStar() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	public void setHeapBuilder(HeapReferenceable.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

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
	public Path computeShortestPath(Graph g, EdgeWeightFunc w, int source, int target, IntToDoubleFunction vHeuristic) {
		if (source == target)
			return new Path(g, source, target, IntLists.emptyList());
		int n = g.vertices().size();
		HeapReferenceable<Double, Integer> heap = heapBuilder.build();
		@SuppressWarnings("unchecked")
		HeapReference<Double, Integer>[] verticesPtrs = new HeapReference[n];

		SSSPResultImpl res = new SSSPResultImpl(g, source);
		res.distances[source] = 0;

		for (int u = source;;) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				double ws = w.weight(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				double distance = res.distances[u] + ws;
				if (distance >= res.distances[v])
					continue;
				res.distances[v] = distance;
				res.backtrack[v] = e;
				double distanceEstimate = distance + vHeuristic.applyAsDouble(v);

				HeapReference<Double, Integer> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					verticesPtrs[v] = heap.insert(distanceEstimate, v);
				} else {
					if (distanceEstimate < vPtr.key())
						heap.decreaseKey(vPtr, distanceEstimate);
				}
			}

			if (heap.isEmpty())
				break;
			HeapReference<Double, Integer> next = heap.extractMin();
			verticesPtrs[u = next.value()] = null;
			if (u == target)
				return res.getPath(target);
		}
		return null;
	}

}
