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

import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.DoubleIntReferenceableHeap;
import com.jgalgo.internal.ds.IntIntReferenceableHeap;
import com.jgalgo.internal.ds.ReferenceableHeap;
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
class ShortestPathSingleSourceDijkstra implements ShortestPathSingleSourceBase {

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.builder();

	/**
	 * Construct a new SSSP algorithm.
	 */
	ShortestPathSingleSourceDijkstra() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(ReferenceableHeap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if one of the edge weights is negative
	 */
	@Override
	public ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g, IWeightFunction w, int source) {
		w = IWeightFunction.replaceNullWeightFunc(w);
		if (WeightFunction.isInteger(w)) {
			return computeSsspInts(g, (IWeightFunctionInt) w, source);
		} else {
			return computeSsspDoubles(g, w, source);
		}
	}

	private ShortestPathSingleSource.IResult computeSsspDoubles(IndexGraph g, IWeightFunction w, int source) {
		final int n = g.vertices().size();
		DoubleIntReferenceableHeap heap = (DoubleIntReferenceableHeap) heapBuilder.build(double.class, int.class);
		DoubleIntReferenceableHeap.Ref[] verticesPtrs = new DoubleIntReferenceableHeap.Ref[n];

		ShortestPathSingleSourceUtils.IndexResult res = new ShortestPathSingleSourceUtils.IndexResult(g, source);
		res.distances[source] = 0;

		for (int u = source;;) {
			final double uDistance = res.distances[u];
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (res.distances[v] != Double.POSITIVE_INFINITY)
					continue;
				double ew = w.weight(e);
				Assertions.onlyPositiveWeight(ew);
				double distance = uDistance + ew;

				DoubleIntReferenceableHeap.Ref vPtr = verticesPtrs[v];
				if (vPtr == null) {
					verticesPtrs[v] = heap.insert(distance, v);
					res.backtrack[v] = e;
				} else if (distance < vPtr.key()) {
					heap.decreaseKey(vPtr, distance);
					res.backtrack[v] = e;
				}
			}

			if (heap.isEmpty())
				break;
			DoubleIntReferenceableHeap.Ref next = heap.extractMin();
			res.distances[u = next.value()] = next.key();
		}

		return res;
	}

	private ShortestPathSingleSource.IResult computeSsspInts(IndexGraph g, IWeightFunctionInt w, int source) {
		final int n = g.vertices().size();
		IntIntReferenceableHeap heap = (IntIntReferenceableHeap) heapBuilder.build(int.class, int.class);
		IntIntReferenceableHeap.Ref[] verticesPtrs = new IntIntReferenceableHeap.Ref[n];

		ShortestPathSingleSourceUtils.IndexResult.Int res =
				new ShortestPathSingleSourceUtils.IndexResult.Int(g, source);
		res.distances[source] = 0;

		for (int u = source;;) {
			final int uDistance = res.distances[u];
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (res.distances[v] != Integer.MAX_VALUE)
					continue;
				int ew = w.weightInt(e);
				Assertions.onlyPositiveWeight(ew);
				int distance = uDistance + ew;

				IntIntReferenceableHeap.Ref vPtr = verticesPtrs[v];
				if (vPtr == null) {
					verticesPtrs[v] = heap.insert(distance, v);
					res.backtrack[v] = e;
				} else if (distance < vPtr.key()) {
					heap.decreaseKey(vPtr, distance);
					res.backtrack[v] = e;
				}
			}

			if (heap.isEmpty())
				break;
			IntIntReferenceableHeap.Ref next = heap.extractMin();
			res.distances[u = next.value()] = next.key();
		}

		return res;
	}

}
