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
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleFunction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
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
class ShortestPathAStar implements ShortestPathHeuristicST {

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.builder();

	/**
	 * Construct a new AStart algorithm.
	 */
	ShortestPathAStar() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(ReferenceableHeap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Path<V, E> computeShortestPath(Graph<V, E> g, WeightFunction<E> w, V source, V target,
			ToDoubleFunction<V> vHeuristic) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			int source0 = ((Integer) source).intValue();
			int target0 = ((Integer) target).intValue();
			ToDoubleFunction<Integer> vHeuristic0 = (ToDoubleFunction<Integer>) vHeuristic;
			IntToDoubleFunction vHeuristic1 = v -> vHeuristic0.applyAsDouble(Integer.valueOf(v));
			return (Path<V, E>) computeShortestPath((IndexGraph) g, w0, source0, target0, vHeuristic1);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			IntToDoubleFunction indexVHeuristic = vIdx -> vHeuristic.applyAsDouble(viMap.indexToId(vIdx));
			IPath indexPath = NegativeCycleException
					.runAndConvertException(g,
							() -> computeShortestPath(iGraph, iw, iSource, iTarget, indexVHeuristic));
			return Paths.pathFromIndexPath(g, indexPath);
		}
	}

	@Override
	public IPath computeShortestPath(IntGraph g, IWeightFunction w, int source, int target,
			IntToDoubleFunction vHeuristic) {
		if (g instanceof IndexGraph) {
			IntToDoubleFunction vHeuristic1 = v -> vHeuristic.applyAsDouble(v);
			return computeShortestPath((IndexGraph) g, w, source, target, vHeuristic1);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			IntToDoubleFunction indexVHeuristic = vIdx -> vHeuristic.applyAsDouble(viMap.indexToIdInt(vIdx));
			IPath indexPath = NegativeCycleException
					.runAndConvertException(g,
							() -> computeShortestPath(iGraph, iw, iSource, iTarget, indexVHeuristic));
			return Paths.intPathFromIndexPath(g, indexPath);
		}

	}

	IPath computeShortestPath(IndexGraph g, IWeightFunction w, int source, int target, IntToDoubleFunction vHeuristic) {
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
