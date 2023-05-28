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

/**
 * Dijkstra's algorithm for Single Source Shortest Path (SSSP).
 * <p>
 * Compute the shortest paths from a single source to all other vertices in \(O(m + n \log n)\) time, using a heap with
 * \(O(1)\) time for {@code decreaseKey()} operations.
 * <p>
 * Only positive edge weights are supported. This implementation should be the first choice for {@link ShortestPathSingleSource} with
 * positive weights. For negative weights use {@link ShortestPathSingleSourceBellmanFord} for floating points or {@link ShortestPathSingleSourceGoldberg} for
 * integers.
 * <p>
 * Based on 'A note on two problems in connexion with graphs' by E. W. Dijkstra (1959). A 'note'??!! this guy changed
 * the world, and he publish it as a 'note'.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class ShortestPathSingleSourceDijkstra implements ShortestPathSingleSource {

	private HeapReferenceable.Builder<?, ?> heapBuilder;

	/**
	 * Construct a new SSSP algorithm.
	 */
	ShortestPathSingleSourceDijkstra() {
		heapBuilder = HeapReferenceable.newBuilder();
	}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(HeapReferenceable.Builder<?, ?> heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if one of the edge weights is negative
	 */
	@Override
	public ShortestPathSingleSource.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		if (w == null)
			w = EdgeWeightFunc.CardinalityEdgeWeightFunction;
		ArgumentCheck.onlyPositiveWeights(g, w);
		if (w instanceof EdgeWeightFunc.Int) {
			return computeSsspInts(g, (EdgeWeightFunc.Int) w, source);
		} else {
			return computeSsspDoubles(g, w, source);
		}
	}

	private ShortestPathSingleSource.Result computeSsspDoubles(Graph g, EdgeWeightFunc w, int source) {
		final int n = g.vertices().size();
		HeapReferenceable<Double, Integer> heap =
				heapBuilder.keysTypePrimitive(double.class).valuesTypePrimitive(int.class).build();
		@SuppressWarnings("unchecked")
		HeapReference<Double, Integer>[] verticesPtrs = new HeapReference[n];

		ShortestPathSingleSourceUtils.ResultImpl res = new ShortestPathSingleSourceUtils.ResultImpl(g, source);
		res.distances[source] = 0;

		for (int u = source;;) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (res.distances[v] != Double.POSITIVE_INFINITY)
					continue;
				double distance = res.distances[u] + w.weight(e);

				HeapReference<Double, Integer> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					verticesPtrs[v] = heap.insert(Double.valueOf(distance), Integer.valueOf(v));
					res.backtrack[v] = e;
				} else {
					if (distance < vPtr.key().doubleValue()) {
						res.backtrack[v] = e;
						heap.decreaseKey(vPtr, Double.valueOf(distance));
					}
				}
			}

			if (heap.isEmpty())
				break;
			HeapReference<Double, Integer> next = heap.extractMin();
			res.distances[u = next.value().intValue()] = next.key().doubleValue();
		}

		return res;
	}

	private ShortestPathSingleSource.Result computeSsspInts(Graph g, EdgeWeightFunc.Int w, int source) {
		final int n = g.vertices().size();
		HeapReferenceable<Integer, Integer> heap =
				heapBuilder.keysTypePrimitive(int.class).valuesTypePrimitive(int.class).build();
		@SuppressWarnings("unchecked")
		HeapReference<Integer, Integer>[] verticesPtrs = new HeapReference[n];

		ShortestPathSingleSourceUtils.ResultImpl.Int res = new ShortestPathSingleSourceUtils.ResultImpl.Int(g, source);
		res.distances[source] = 0;

		for (int u = source;;) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (res.distances[v] != Integer.MAX_VALUE)
					continue;
				int distance = res.distances[u] + w.weightInt(e);

				HeapReference<Integer, Integer> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					verticesPtrs[v] = heap.insert(Integer.valueOf(distance), Integer.valueOf(v));
					res.backtrack[v] = e;
				} else {
					if (distance < vPtr.key().intValue()) {
						res.backtrack[v] = e;
						heap.decreaseKey(vPtr, Integer.valueOf(distance));
					}
				}
			}

			if (heap.isEmpty())
				break;
			HeapReference<Integer, Integer> next = heap.extractMin();
			res.distances[u = next.value().intValue()] = next.key().intValue();
		}

		return res;
	}

}
