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

import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.ds.HeapReference;
import com.jgalgo.internal.ds.HeapReferenceable;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;

class VoronoiAlgoDijkstra extends VoronoiAlgos.AbstractImpl {

	@SuppressWarnings("boxing")
	@Override
	VoronoiAlgo.Result computeVoronoiCells(IndexGraph g, IntCollection sites, IWeightFunction w) {
		if (sites.isEmpty())
			throw new IllegalArgumentException("no sites provided");
		final int n = g.vertices().size();
		HeapReferenceable<Double, Integer> heap =
				HeapReferenceable.newBuilder().keysTypePrimitive(double.class).valuesTypePrimitive(int.class).build();
		@SuppressWarnings("unchecked")
		HeapReference<Double, Integer>[] heapVPtrs = new HeapReference[n];

		double[] distance = new double[n];
		Arrays.fill(distance, Double.POSITIVE_INFINITY);
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);
		int[] cell = new int[n];
		Arrays.fill(cell, -1);

		int[] sitesArr = sites.toIntArray();
		for (int siteIdx = 0; siteIdx < sitesArr.length; siteIdx++) {
			int site = sitesArr[siteIdx];
			if (cell[site] != -1)
				throw new IllegalArgumentException("Duplicate site: " + site);
			cell[site] = siteIdx;
			distance[site] = 0;
			heapVPtrs[site] = heap.insert(0.0, site);
		}

		while (!heap.isEmpty()) {
			HeapReference<Double, Integer> min = heap.extractMin();
			int u = min.value();
			double uDistance = distance[u] = min.key();
			int uCell = cell[u];
			heapVPtrs[u] = null;

			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (distance[v] != Double.POSITIVE_INFINITY)
					continue;
				double ew = w.weight(e);
				Assertions.Graphs.onlyPositiveWeight(ew);
				double vDistance = uDistance + ew;

				HeapReference<Double, Integer> vPtr = heapVPtrs[v];
				if (vPtr == null) {
					heapVPtrs[v] = heap.insert(Double.valueOf(vDistance), Integer.valueOf(v));
					backtrack[v] = e;
					cell[v] = uCell;
				} else if (vDistance < vPtr.key().doubleValue()) {
					heap.decreaseKey(vPtr, Double.valueOf(vDistance));
					backtrack[v] = e;
					cell[v] = uCell;
				}
			}
		}
		boolean hasUnreachable = false;
		int unreachableCell = sitesArr.length;
		for (int v = 0; v < n; v++) {
			if (cell[v] == -1) {
				hasUnreachable = true;
				cell[v] = unreachableCell;
			}
		}
		int blockNum = sitesArr.length + (hasUnreachable ? 1 : 0);

		return new VoronoiAlgos.ResultImpl(g, blockNum, cell, distance, backtrack, sitesArr);
	}

}
