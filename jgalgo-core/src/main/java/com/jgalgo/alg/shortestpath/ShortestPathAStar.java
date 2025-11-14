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

import java.util.Objects;
import java.util.function.IntToDoubleFunction;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.DoubleIntReferenceableHeap;
import com.jgalgo.internal.ds.ReferenceableHeap;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * A* shortest path algorithm.
 *
 * <p>
 * The A star (\(A^*\)) algorithm try to find the shortest path from a source to target vertex. It uses a heuristic that
 * map a vertex to an estimation of its distance from the target position.
 *
 * <p>
 * An advantage of the \(A^*\) algorithm over other {@link ShortestPathSingleSource} algorithm, is that it can terminate
 * much faster for the specific source and target, especially if the heuristic is good.
 *
 * <p>
 * The algorithm runs in \(O(m + n \log n)\) and uses linear space in the worse case. If the heuristic is good, smaller
 * running time and space (!) will be used.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/A*_search_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class ShortestPathAStar extends ShortestPathHeuristicStAbstract {

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.builder();

	/**
	 * Construct a new AStart algorithm.
	 *
	 * <p>
	 * Please prefer using {@link ShortestPathHeuristicSt#newInstance()} to get a default implementation for the
	 * {@link ShortestPathHeuristicSt} interface.
	 */
	public ShortestPathAStar() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(ReferenceableHeap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@Override
	protected IPath computeShortestPath(IndexGraph g, IWeightFunction w, int source, int target,
			IntToDoubleFunction vHeuristic) {
		if (source == target)
			return IPath.valueOf(g, source, target, IntLists.emptyList());
		DoubleIntReferenceableHeap heap = (DoubleIntReferenceableHeap) heapBuilder.build(double.class, int.class);

		Int2ObjectMap<Info> info = new Int2ObjectOpenHashMap<>();
		Info sourceInfo = new Info();
		sourceInfo.distance = 0;
		info.put(source, sourceInfo);
		heap.insert(.0, source);

		while (heap.isNotEmpty()) {
			DoubleIntReferenceableHeap.Ref min = heap.extractMin();
			int u = min.value();
			if (u == target)
				return computePath(g, source, target, info);
			Info uInfo = info.get(u);
			final double uDistance = uInfo.distance;
			uInfo.heapPtr = null;

			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				Info vInfo = info.computeIfAbsent(v, k -> new Info());

				double ew = w.weight(e);
				Assertions.onlyPositiveWeight(ew);
				double distance = uDistance + ew;

				if (distance >= vInfo.distance)
					continue;
				vInfo.distance = distance;
				vInfo.backtrack = e;
				double distanceEstimate = distance + vHeuristic.applyAsDouble(v);

				if (vInfo.heapPtr == null) {
					vInfo.heapPtr = heap.insert(distanceEstimate, v);
				} else {
					assert distanceEstimate <= vInfo.heapPtr.key();
					heap.decreaseKey(vInfo.heapPtr, distanceEstimate);
				}
			}
		}
		return null;
	}

	private static IPath computePath(IndexGraph g, int source, int target, Int2ObjectMap<Info> info) {
		IntArrayList path = new IntArrayList();
		if (g.isDirected()) {
			for (int v = target;;) {
				int e = info.get(v).backtrack;
				if (e < 0) {
					assert v == source;
					break;
				}
				path.add(e);
				v = g.edgeSource(e);
			}
		} else {
			for (int v = target;;) {
				int e = info.get(v).backtrack;
				if (e < 0) {
					assert v == source;
					break;
				}
				path.add(e);
				v = g.edgeEndpoint(e, v);
			}
		}
		IntArrays.reverse(path.elements(), 0, path.size());
		return IPath.valueOf(g, source, target, path);
	}

	static class Info {
		int backtrack = -1;
		double distance = Double.POSITIVE_INFINITY;
		DoubleIntReferenceableHeap.Ref heapPtr;
	}

}
