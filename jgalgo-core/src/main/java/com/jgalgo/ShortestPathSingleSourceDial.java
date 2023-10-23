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
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.LinkedListFixedSize;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;

/**
 * Dial's algorithm for Single Source Shortest Path for positive integer weights.
 * <p>
 * The algorithm runs in \(O(n + m + D)\) where \(D\) is the maximum distance, or the sum of heaviest n-1 edges if the
 * maximum distance is not known. It takes advantage of the fact that a heap for integers can be implemented using
 * buckets, one for each weight. Such a heap require \(D\) buckets, and therefore the algorithm running time and space
 * depends on \(D\).
 * <p>
 * This algorithm should be used in case the maximal distance is known in advance, and its small. For example, its used
 * by {@link ShortestPathSingleSourceGoldberg} as a subroutine, where the maximum distance is bounded by the number of
 * layers.
 * <p>
 * Based on 'Algorithm 360: Shortest-Path Forest with Topological Ordering' by Dial, Robert B. (1969).
 *
 * @author Barak Ugav
 */
class ShortestPathSingleSourceDial extends ShortestPathSingleSourceUtils.AbstractImpl {

	/**
	 * Construct a new SSSP algorithm.
	 */
	ShortestPathSingleSourceDial() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if one of the edge weights is negative or the weight function is not of type
	 *                                      {@link WeightFunction.Int}
	 */
	@Override
	ShortestPathSingleSource.Result computeShortestPaths(IndexGraph g, WeightFunction w, int source) {
		if (w == null)
			w = WeightFunction.CardinalityWeightFunction;
		if (!(w instanceof WeightFunction.Int))
			throw new IllegalArgumentException("only int weights are supported");
		WeightFunction.Int w0 = (WeightFunction.Int) w;

		return computeShortestPaths(g, w0, source, -1);
	}

	/**
	 * Compute the shortest paths from a source to any other vertex in a graph, given a maximal distance bound.
	 *
	 * @param  g           a graph
	 * @param  w           an integer edge weight function with non negative values
	 * @param  source      a source vertex
	 * @param  maxDistance a bound on the maximal distance to any vertex in the graph, any negative number is treated as
	 *                         'unknown'
	 * @return             a result object containing the distances and shortest paths from the source to any other
	 *                     vertex
	 * @see                #computeShortestPaths(Graph, WeightFunction, int)
	 */
	ShortestPathSingleSource.Result computeShortestPaths(IndexGraph g, WeightFunction.Int w, int source,
			int maxDistance) {
		ShortestPathSingleSourceUtils.ResultImpl.Int res = new ShortestPathSingleSourceUtils.ResultImpl.Int(g, source);
		res.distances[source] = 0;

		DialHeap heap = new DialHeap(g.vertices().size(), maxDistance);

		for (int u = source;;) {
			final int uDistance = res.distances[u];
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (res.distances[v] != Integer.MAX_VALUE)
					continue;

				int ew = w.weightInt(e);
				Assertions.Graphs.onlyPositiveWeight(ew);
				int distance = uDistance + ew;

				if (!heap.containsVertex(v)) {
					heap.insert(v, distance);
					res.backtrack[v] = e;
				} else {
					if (distance < heap.getCurrentDistance(v)) {
						res.backtrack[v] = e;
						heap.decreaseKey(v, distance);
					}
				}
			}

			IntIntPair next = heap.extractMin();
			if (next == null)
				break;
			u = next.firstInt();
			assert res.distances[u] == Integer.MAX_VALUE;
			res.distances[u] = next.secondInt();
		}

		return res;
	}

	private static class DialHeap {

		private final LinkedListFixedSize.Doubly heapBucketsNodes;
		private int[] heapBucketsHead;
		private final int[] heapDistances;
		private int heapScanIdx;
		private int maxDistance;

		DialHeap(int n, int initialSize) {
			heapBucketsHead = initialSize <= 0 ? IntArrays.DEFAULT_EMPTY_ARRAY : new int[initialSize];
			Arrays.fill(heapBucketsHead, LinkedListFixedSize.None);
			heapBucketsNodes = new LinkedListFixedSize.Doubly(n);
			heapDistances = new int[n];
			Arrays.fill(heapDistances, 0, n, -1);

			maxDistance = -1;
			heapScanIdx = 0;
		}

		void insert(int v, int distance) {
			heapDistances[v] = distance;
			if (distance >= heapBucketsHead.length) {
				int oldLength = heapBucketsHead.length;
				int newLength = Math.max(distance + 1, oldLength * 2);
				heapBucketsHead = Arrays.copyOf(heapBucketsHead, newLength);
				Arrays.fill(heapBucketsHead, oldLength, newLength, LinkedListFixedSize.None);
				maxDistance = distance;
			} else if (distance > maxDistance) {
				maxDistance = distance;
			}

			int h = heapBucketsHead[distance];
			heapBucketsHead[distance] = v;
			if (h != LinkedListFixedSize.None)
				heapBucketsNodes.connect(v, h);
		}

		void decreaseKey(int v, int distance) {
			int oldDistance = heapDistances[v];
			assert distance < oldDistance;
			if (v == heapBucketsHead[oldDistance])
				heapBucketsHead[oldDistance] = heapBucketsNodes.next(v);
			heapBucketsNodes.disconnect(v);

			insert(v, distance);
		}

		IntIntPair extractMin() {
			for (int distance = heapScanIdx; distance <= maxDistance; distance++) {
				int v = heapBucketsHead[distance];
				if (v != LinkedListFixedSize.None) {
					heapBucketsHead[distance] = heapBucketsNodes.next(v);
					heapBucketsNodes.disconnect(v);
					heapScanIdx = distance;
					return IntIntPair.of(v, distance);
				}
			}
			heapScanIdx = maxDistance;
			return null;
		}

		boolean containsVertex(int v) {
			return heapDistances[v] != -1;
		}

		int getCurrentDistance(int v) {
			return heapDistances[v];
		}

	}

}
