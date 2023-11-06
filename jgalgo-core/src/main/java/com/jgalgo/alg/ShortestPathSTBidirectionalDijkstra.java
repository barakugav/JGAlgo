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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.ds.HeapReference;
import com.jgalgo.internal.ds.HeapReferenceable;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntLists;

class ShortestPathSTBidirectionalDijkstra extends ShortestPathSTs.AbstractImpl {

	@SuppressWarnings("boxing") // TODO
	@Override
	IPath computeShortestPath(IndexGraph g, IWeightFunction w, int source, int target) {
		if (!g.vertices().contains(source))
			throw new IndexOutOfBoundsException(source);
		if (!g.vertices().contains(target))
			throw new IndexOutOfBoundsException(target);
		if (source == target)
			return new PathImpl(g, source, target, IntLists.emptyList());
		if (w == null)
			w = IWeightFunction.CardinalityWeightFunction;

		HeapReferenceable.Builder<Double, Integer> heapBuilder =
				HeapReferenceable.newBuilder().keysTypePrimitive(double.class).valuesTypePrimitive(int.class);
		HeapReferenceable<Double, Integer> heapS = heapBuilder.build();
		HeapReferenceable<Double, Integer> heapT = heapBuilder.build();

		Int2ObjectMap<Info> infoS = new Int2ObjectOpenHashMap<>();
		Int2ObjectMap<Info> infoT = new Int2ObjectOpenHashMap<>();

		int middle = -1;
		double mu = Double.POSITIVE_INFINITY;
		infoS.put(source, new Info());
		infoT.put(target, new Info());
		heapS.insert(.0, source);
		heapT.insert(.0, target);
		while (!heapS.isEmpty() && !heapT.isEmpty()) {

			HeapReference<Double, Integer> min = heapS.extractMin();
			double uDistanceS = min.key();
			int uS = min.value();
			Info uInfoS = infoS.get(uS);
			uInfoS.heapPtr = null;
			uInfoS.markVisited();
			uInfoS.distance = uDistanceS;

			min = heapT.extractMin();
			double uDistanceT = min.key();
			int uT = min.value();
			Info uInfoT = infoT.get(uT);
			uInfoT.heapPtr = null;
			uInfoT.markVisited();
			uInfoT.distance = uDistanceT;

			for (IEdgeIter eit = g.outEdges(uS).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				Info vInfoS = infoS.computeIfAbsent(v, k -> new Info());
				if (vInfoS.isVisited())
					continue;
				double ew = w.weight(e);
				Assertions.Graphs.onlyPositiveWeight(ew);
				double vDistance = uDistanceS + ew;
				Info vInfoT = infoT.get(v);
				if (vInfoT != null && vInfoT.isVisited()) {
					if (mu > vDistance + vInfoT.distance) {
						mu = vDistance + vInfoT.distance;
						middle = v;
					}
				}

				HeapReference<Double, Integer> vPtr = vInfoS.heapPtr;
				if (vPtr == null) {
					vInfoS.heapPtr = heapS.insert(Double.valueOf(vDistance), Integer.valueOf(v));
					vInfoS.backtrack = e;
				} else if (vDistance < vPtr.key().doubleValue()) {
					heapS.decreaseKey(vPtr, Double.valueOf(vDistance));
					vInfoS.backtrack = e;
				}
			}

			for (IEdgeIter eit = g.inEdges(uT).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.sourceInt();
				Info vInfoT = infoT.computeIfAbsent(v, k -> new Info());
				if (vInfoT.isVisited())
					continue;
				double ew = w.weight(e);
				Assertions.Graphs.onlyPositiveWeight(ew);
				double vDistance = uDistanceT + ew;
				Info vInfoS = infoS.get(v);
				if (vInfoS != null && vInfoS.isVisited()) {
					if (mu > vDistance + vInfoS.distance) {
						mu = vDistance + vInfoS.distance;
						middle = v;
					}
				}

				HeapReference<Double, Integer> vPtr = vInfoT.heapPtr;
				if (vPtr == null) {
					vInfoT.heapPtr = heapT.insert(Double.valueOf(vDistance), Integer.valueOf(v));
					vInfoT.backtrack = e;
				} else if (vDistance < vPtr.key().doubleValue()) {
					heapT.decreaseKey(vPtr, Double.valueOf(vDistance));
					vInfoT.backtrack = e;
				}
			}

			if (uDistanceS + uDistanceT >= mu)
				break;
		}
		if (middle == -1)
			return null;

		IntArrayList path = new IntArrayList();

		/* add edges from source to middle */
		if (g.isDirected()) {
			for (int u = middle, e; u != source; u = g.edgeSource(e))
				path.add(e = infoS.get(u).backtrack);
		} else {
			for (int u = middle, e; u != source; u = g.edgeEndpoint(e, u))
				path.add(e = infoS.get(u).backtrack);
		}
		IntArrays.reverse(path.elements(), 0, path.size());

		/* add edges from middle to target */
		if (g.isDirected()) {
			for (int u = middle, e; u != target; u = g.edgeTarget(e))
				path.add(e = infoT.get(u).backtrack);
		} else {
			for (int u = middle, e; u != target; u = g.edgeEndpoint(e, u))
				path.add(e = infoT.get(u).backtrack);
		}
		return new PathImpl(g, source, target, path);
	}

	static class Info {
		int backtrack = -1;
		double distance = Double.POSITIVE_INFINITY;
		HeapReference<Double, Integer> heapPtr;

		void markVisited() {
			assert heapPtr != VisitedMark : "already visited";
			heapPtr = VisitedMark;
		}

		boolean isVisited() {
			return heapPtr == VisitedMark;
		}

		@SuppressWarnings("boxing")
		private static final HeapReference<Double, Integer> VisitedMark = HeapReferenceable.newBuilder()
				.keysTypePrimitive(double.class).valuesTypePrimitive(int.class).build().insert(0.0, 0);
	}

}
