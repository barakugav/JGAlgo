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
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * A* shortest path algorithm.
 * <p>
 * The A star (\(A^*\)) algorithm try to find the shortest path from a source to target vertex. It uses a heuristic that
 * map a vertex to an estimation of its distance from the target position.
 * <p>
 * An advantage of the \(A^*\) algorithm over other {@link ShortestPathSingleSource} algorithm, is that it can terminate
 * much faster for the specific source and target, especially if the heuristic is good.
 * <p>
 * The algorithm runs in \(O(m + n \log n)\) and uses linear space in the worse case. If the heuristic is good, smaller
 * running time and space (!) will be used.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/A*_search_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class AStar implements ShortestPathWithHeuristic {

	private HeapReferenceable.Builder<Double, Integer> heapBuilder =
			HeapReferenceable.newBuilder().keysTypePrimitive(double.class).valuesTypePrimitive(int.class);

	/**
	 * Construct a new AStart algorithm.
	 */
	AStar() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(HeapReferenceable.Builder<?, ?> heapBuilder) {
		this.heapBuilder = heapBuilder.keysTypePrimitive(double.class).valuesTypePrimitive(int.class);
	}

	@Override
	public Path computeShortestPath(Graph g, EdgeWeightFunc w, int source, int target, IntToDoubleFunction vHeuristic) {
		ArgumentCheck.onlyPositiveWeights(g, w);
		if (source == target)
			return new PathImpl(g, source, target, IntLists.emptyList());
		HeapReferenceable<Double, Integer> heap = heapBuilder.build();

		Int2ObjectMap<HeapReference<Double, Integer>> verticesPtrs = new Int2ObjectOpenHashMap<>();

		Int2DoubleMap distances = new Int2DoubleOpenHashMap();
		distances.defaultReturnValue(Double.POSITIVE_INFINITY);
		distances.put(source, 0);

		Int2IntMap backtrack = new Int2IntOpenHashMap();
		backtrack.defaultReturnValue(-1);

		for (int u = source;;) {
			final double uDistance = distances.get(u);
			assert uDistance != Double.POSITIVE_INFINITY;

			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				double distance = uDistance + w.weight(e);
				if (distance >= distances.get(v))
					continue;
				distances.put(v, distance);
				backtrack.put(v, e);
				double distanceEstimate = distance + vHeuristic.applyAsDouble(v);

				HeapReference<Double, Integer> vPtr = verticesPtrs.get(v);
				if (vPtr == null) {
					vPtr = heap.insert(Double.valueOf(distanceEstimate), Integer.valueOf(v));
					verticesPtrs.put(v, vPtr);
				} else {
					if (distanceEstimate < vPtr.key().doubleValue())
						heap.decreaseKey(vPtr, Double.valueOf(distanceEstimate));
				}
			}

			if (heap.isEmpty())
				break;
			HeapReference<Double, Integer> next = heap.extractMin();
			verticesPtrs.remove(u = next.value().intValue());
			if (u == target)
				return computePath(g, source, target, backtrack);
		}
		return null;
	}

	private static Path computePath(Graph g, int source, int target, Int2IntMap backtrack) {
		IntArrayList path = new IntArrayList();
		if (g.getCapabilities().directed()) {
			for (int v = target;;) {
				int e = backtrack.get(v);
				if (e == -1) {
					assert v == source;
					break;
				}
				path.add(e);
				v = g.edgeSource(e);
			}
		} else {
			for (int v = target;;) {
				int e = backtrack.get(v);
				if (e == -1) {
					assert v == source;
					break;
				}
				path.add(e);
				v = g.edgeEndpoint(e, v);
			}
		}
		IntArrays.reverse(path.elements(), 0, path.size());
		return new PathImpl(g, source, target, path);
	}

}
