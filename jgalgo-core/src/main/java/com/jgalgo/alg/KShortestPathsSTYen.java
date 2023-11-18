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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.DoubleIntReferenceableHeap;
import com.jgalgo.internal.ds.DoubleObjReferenceableHeap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectLists;

/**
 * Yen's algorithm for computing the K shortest paths between two vertices in a graph.
 *
 * <p>
 * This implementation contains Lawler's improvements to the original algorithm, which avoid checked for duplicate paths
 * in the heap by storing for each path the index in which it deviates from the previous path, and only considering
 * paths that deviate from a path after that index, which eliminate duplications entirely.
 *
 * <p>
 * The algorithms runs in \(O(nk(m+n \log n))\) time.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Yen%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class KShortestPathsSTYen extends KShortestPathsSTs.AbstractImpl {

	@Override
	List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k) {
		if (source == target)
			return List.of(new PathImpl(g, source, target, IntList.of()));
		if (w == null)
			w = IWeightFunction.CardinalityWeightFunction;
		final int n = g.vertices().size();
		final int m = g.edges().size();
		DoubleObjReferenceableHeap<ObjectIntPair<IPath>> heap = DoubleObjReferenceableHeap.newInstance();
		Bitmap verticesMask = new Bitmap(n);
		Bitmap edgesMask = new Bitmap(m);
		ShortestPathSubroutine spFunc = new ShortestPathSubroutine(g, w, target, verticesMask, edgesMask);

		/* compute the shortest path from source to target */
		ObjectDoublePair<IPath> shortestPath = spFunc.computeShortestPath(source);
		if (shortestPath == null)
			return ObjectLists.emptyList();
		heap.insert(shortestPath.secondDouble(), ObjectIntPair.of(shortestPath.first(), 0));

		List<IPath> paths = k <= m ? new ArrayList<>(k) : new ArrayList<>();
		while (heap.isNotEmpty()) {
			DoubleObjReferenceableHeap.Ref<ObjectIntPair<IPath>> min = heap.extractMin();
			IPath kthPath = min.value().first();
			final int kthPathDeviationIdx = min.value().secondInt();
			assert kthPath.isSimple();
			paths.add(kthPath);
			if (paths.size() == k)
				break;

			IntList kthPathEdges = kthPath.edges();
			IntList kthPathVertices = kthPath.vertices();
			assert kthPathVertices.getInt(kthPathVertices.size() - 1) == target;
			for (int deviationIdx = kthPathDeviationIdx; deviationIdx < kthPathVertices.size() - 1; deviationIdx++) {
				int spurNode = kthPathVertices.getInt(deviationIdx);
				IntList rootPath = kthPathEdges.subList(0, deviationIdx);

				/* remove edges that are part of the previous shortest paths */
				for (IPath p1 : paths) {
					IntList p1Edges = p1.edges();
					if (p1Edges.size() > rootPath.size() && p1Edges.subList(0, deviationIdx).equals(rootPath))
						edgesMask.set(p1Edges.getInt(deviationIdx));
				}
				/* remove vertices that are part of the root path to enforce simple paths */
				for (int v : kthPathVertices.subList(0, deviationIdx))
					verticesMask.set(v);

				shortestPath = spFunc.computeShortestPath(spurNode);
				if (shortestPath != null) {
					IntList path = new IntArrayList(rootPath.size() + shortestPath.first().edges().size());
					path.addAll(rootPath);
					path.addAll(shortestPath.first().edges());
					heap.insert(w.weightSum(path),
							ObjectIntPair.of(new PathImpl(g, source, target, path), deviationIdx));

					assert heap.stream().map(r -> r.value().left().edges()).distinct().count() == ObjectIterables
							.size(heap) : "heap contains duplicate paths";
				}

				verticesMask.clear();
				edgesMask.clear();
			}
		}
		return paths;
	}

	private static class ShortestPathSubroutine {

		private final IndexGraph g;
		private final IWeightFunction w;
		private final int target;
		private final Bitmap verticesMask;
		private final Bitmap edgesMask;

		private final DoubleIntReferenceableHeap heapS;
		private final DoubleIntReferenceableHeap heapT;

		/*
		 * usually bidirectional dijkstra uses hashtable to store vertex information, to hopefully achieve less than
		 * linear time and space. Because we call this subroutine many times, we use arrays instead of hashtables to
		 * avoid the overhead of hashtable operations. This leads to linear space, but we allocate it only once, and for
		 * each shortest path computation we only need to clear the arrays, which we do in less than linear time using
		 * toClearS and toClearT which store exactly which vertices we need to clean.
		 */

		private final int[] backtrackS;
		private final int[] backtrackT;
		private final double[] distanceS;
		private final double[] distanceT;
		private final DoubleIntReferenceableHeap.Ref[] heapPtrsS;
		private final DoubleIntReferenceableHeap.Ref[] heapPtrsT;
		private final Bitmap visitedS;
		private final Bitmap visitedT;

		private final IntList toClearS = new IntArrayList();
		private final IntList toClearT = new IntArrayList();

		ShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target, Bitmap verticesMask, Bitmap edgesMask) {
			this.g = g;
			this.w = w;
			this.target = target;
			this.verticesMask = verticesMask;
			this.edgesMask = edgesMask;

			heapS = DoubleIntReferenceableHeap.newInstance();
			heapT = DoubleIntReferenceableHeap.newInstance();

			final int n = g.vertices().size();
			backtrackS = new int[n];
			backtrackT = new int[n];
			// Arrays.fill(backtrackS, -1);
			// Arrays.fill(backtrackT, -1);
			distanceS = new double[n];
			distanceT = new double[n];
			// Arrays.fill(distanceS, Double.POSITIVE_INFINITY);
			// Arrays.fill(distanceT, Double.POSITIVE_INFINITY);
			heapPtrsS = new DoubleIntReferenceableHeap.Ref[n];
			heapPtrsT = new DoubleIntReferenceableHeap.Ref[n];
			visitedS = new Bitmap(n);
			visitedT = new Bitmap(n);
		}

		ObjectDoublePair<IPath> computeShortestPath(int source) {
			final ObjectDoublePair<IPath> res = computeShortestPath0(source);
			heapS.clear();
			heapT.clear();
			JGAlgoUtils.clearAllUnsafe(heapPtrsS, toClearS);
			JGAlgoUtils.clearAllUnsafe(heapPtrsT, toClearT);
			visitedS.clearAllUnsafe(toClearS);
			visitedT.clearAllUnsafe(toClearT);
			toClearS.clear();
			toClearT.clear();
			return res;
		}

		private ObjectDoublePair<IPath> computeShortestPath0(int source) {
			assert source != target;
			assert !verticesMask.get(source);
			assert !verticesMask.get(target);

			assert visitedS.isEmpty();
			assert visitedT.isEmpty();
			assert toClearS.isEmpty();
			assert toClearT.isEmpty();
			assert Arrays.stream(heapPtrsS).allMatch(Objects::isNull);
			assert Arrays.stream(heapPtrsT).allMatch(Objects::isNull);

			int middle = -1;
			double mu = Double.POSITIVE_INFINITY;
			heapS.insert(.0, source);
			heapT.insert(.0, target);
			toClearS.add(source);
			toClearT.add(target);
			while (heapS.isNotEmpty() && heapT.isNotEmpty()) {

				DoubleIntReferenceableHeap.Ref min = heapS.extractMin();
				double uDistanceS = min.key();
				int uS = min.value();
				heapPtrsS[uS] = null;
				visitedS.set(uS);
				distanceS[uS] = uDistanceS;

				min = heapT.extractMin();
				double uDistanceT = min.key();
				int uT = min.value();
				heapPtrsT[uT] = null;
				visitedT.set(uT);
				distanceT[uT] = uDistanceT;

				for (IEdgeIter eit = g.outEdges(uS).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (edgesMask.get(e))
						continue;
					int v = eit.targetInt();
					if (verticesMask.get(v))
						continue;
					if (visitedS.get(v))
						continue;
					double ew = w.weight(e);
					Assertions.Graphs.onlyPositiveWeight(ew);
					double vDistance = uDistanceS + ew;
					if (visitedT.get(v)) {
						if (mu > vDistance + distanceT[v]) {
							mu = vDistance + distanceT[v];
							middle = v;
						}
					}

					DoubleIntReferenceableHeap.Ref vPtr = heapPtrsS[v];
					if (vPtr == null) {
						toClearS.add(v);
						heapPtrsS[v] = heapS.insert(vDistance, v);
						backtrackS[v] = e;
					} else if (vDistance < vPtr.key()) {
						heapS.decreaseKey(vPtr, vDistance);
						backtrackS[v] = e;
					}
				}

				for (IEdgeIter eit = g.inEdges(uT).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (edgesMask.get(e))
						continue;
					int v = eit.sourceInt();
					if (verticesMask.get(v))
						continue;
					if (visitedT.get(v))
						continue;
					double ew = w.weight(e);
					Assertions.Graphs.onlyPositiveWeight(ew);
					double vDistance = uDistanceT + ew;
					if (visitedS.get(v)) {
						if (mu > vDistance + distanceS[v]) {
							mu = vDistance + distanceS[v];
							middle = v;
						}
					}

					DoubleIntReferenceableHeap.Ref vPtr = heapPtrsT[v];
					if (vPtr == null) {
						toClearT.add(v);
						heapPtrsT[v] = heapT.insert(vDistance, v);
						backtrackT[v] = e;
					} else if (vDistance < vPtr.key()) {
						heapT.decreaseKey(vPtr, vDistance);
						backtrackT[v] = e;
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
					path.add(e = backtrackS[u]);
			} else {
				for (int u = middle, e; u != source; u = g.edgeEndpoint(e, u))
					path.add(e = backtrackS[u]);
			}
			IntArrays.reverse(path.elements(), 0, path.size());

			/* add edges from middle to target */
			if (g.isDirected()) {
				for (int u = middle, e; u != target; u = g.edgeTarget(e))
					path.add(e = backtrackT[u]);
			} else {
				for (int u = middle, e; u != target; u = g.edgeEndpoint(e, u))
					path.add(e = backtrackT[u]);
			}
			return ObjectDoublePair.of(new PathImpl(g, source, target, path), mu);
		}

	}

}
