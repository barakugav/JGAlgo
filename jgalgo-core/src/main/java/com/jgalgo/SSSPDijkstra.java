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

import java.util.Arrays;

/**
 * Dijkstra's algorithm for Single Source Shortest Path (SSSP).
 * <p>
 * Compute the shortest paths from a single source to all other vertices in \(O(m + n \log n)\) time, using
 * {@link HeapReferenceable} with \(O(1)\) time for {@link HeapReferenceable#decreaseKey(HeapReference, Object)}
 * operations.
 * <p>
 * Only positive edge weights are supported. This implementation should be the first choice for {@link SSSP} with
 * positive weights. For negative weights use {@link SSSPBellmanFord} for floating points or {@link SSSPGoldberg} for
 * integers.
 * <p>
 * Based on 'A note on two problems in connexion with graphs' by E. W. Dijkstra (1959). A 'note'??!! this guy changed
 * the world, and he publish it as a 'note'.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class SSSPDijkstra implements SSSP {

	@SuppressWarnings("rawtypes")
	private HeapReferenceable heap;
	@SuppressWarnings("rawtypes")
	private HeapReference[] verticesPtrs;

	/**
	 * Construct a new SSSP algorithm object.
	 */
	public SSSPDijkstra() {
		heap = new HeapPairing<>();
	}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	public void setHeapBuilder(HeapReferenceable.Builder heapBuilder) {
		heap = heapBuilder.build();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if one of the edge weights is negative
	 */
	@Override
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		int n = g.vertices().size();
		if (verticesPtrs == null || verticesPtrs.length < n)
			verticesPtrs = new HeapReference[n];

		SSSP.Result res;
		if (w instanceof EdgeWeightFunc.Int) {
			res = new WorkerInt(heap, verticesPtrs).computeSSSP(g, (EdgeWeightFunc.Int) w, source);
		} else {
			res = new WorkerDouble(heap, verticesPtrs).computeSSSP(g, w, source);
			res = new WorkerDouble(new HeapPairing<>(), new HeapReference[n]).computeSSSP(g, w, source);
		}

		heap.clear();
		Arrays.fill(verticesPtrs, 0, n, null);
		return res;
	}

	private static class WorkerDouble {

		private final HeapReferenceable<Double, Integer> heap;
		private final HeapReference<Double, Integer>[] verticesPtrs;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		WorkerDouble(HeapReferenceable heap, HeapReference[] verticesPtrs) {
			this.heap = heap;
			this.verticesPtrs = verticesPtrs;
		}

		SSSP.Result computeSSSP(Graph g, EdgeWeightFunc w, int source) {
			SSSPResultImpl res = new SSSPResultImpl(g, source);
			res.distances[source] = 0;

			for (int u = source;;) {
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					if (res.distances[v] != Double.POSITIVE_INFINITY)
						continue;
					double ws = w.weight(e);
					if (ws < 0)
						throw new IllegalArgumentException("negative weights are not supported");
					double distance = res.distances[u] + ws;

					HeapReference<Double, Integer> vPtr = verticesPtrs[v];
					if (vPtr == null) {
						verticesPtrs[v] = heap.insert(distance, v);
						res.backtrack[v] = e;
					} else {
						if (distance < vPtr.key()) {
							res.backtrack[v] = e;
							heap.decreaseKey(vPtr, distance);
						}
					}
				}

				if (heap.isEmpty())
					break;
				HeapReference<Double, Integer> next = heap.extractMin();
				res.distances[u = next.value()] = next.key();
			}

			return res;
		}
	}

	private static class WorkerInt {

		private final HeapReferenceable<Integer, Integer> heap;
		private final HeapReference<Integer, Integer>[] verticesPtrs;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		WorkerInt(HeapReferenceable heap, HeapReference[] verticesPtrs) {
			this.heap = heap;
			this.verticesPtrs = verticesPtrs;
		}

		SSSP.Result computeSSSP(Graph g, EdgeWeightFunc.Int w, int source) {
			SSSPResultImpl.Int res = new SSSPResultImpl.Int(g, source);
			res.distances[source] = 0;

			for (int u = source;;) {
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					if (res.distances[v] != Integer.MAX_VALUE)
						continue;
					int ws = w.weightInt(e);
					if (ws < 0)
						throw new IllegalArgumentException("negative weights are not supported");
					int distance = res.distances[u] + ws;

					HeapReference<Integer, Integer> vPtr = verticesPtrs[v];
					if (vPtr == null) {
						verticesPtrs[v] = heap.insert(distance, v);
						res.backtrack[v] = e;
					} else {
						if (distance < vPtr.key()) {
							res.backtrack[v] = e;
							heap.decreaseKey(vPtr, distance);
						}
					}
				}

				if (heap.isEmpty())
					break;
				HeapReference<Integer, Integer> next = heap.extractMin();
				res.distances[u = next.value()] = next.key();
			}

			return res;
		}

	}

}
