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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.IndexHeapDouble;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;

class VoronoiAlgoDijkstra extends VoronoiAlgos.AbstractImpl {

	@Override
	VoronoiAlgo.IResult computeVoronoiCells(IndexGraph g, IntCollection sites, IWeightFunction w) {
		if (sites.isEmpty())
			throw new IllegalArgumentException("no sites provided");
		w = IWeightFunction.replaceNullWeightFunc(w);
		final int n = g.vertices().size();
		double[] distance = new double[n];
		int[] backtrack = new int[n];
		int[] cell = new int[n];
		IndexHeapDouble heap = IndexHeapDouble.newInstance(distance);
		Arrays.fill(distance, Double.POSITIVE_INFINITY);
		Arrays.fill(backtrack, -1);
		Arrays.fill(cell, -1);

		int[] sitesArr = sites.toIntArray();
		for (int siteIdx : range(sitesArr.length)) {
			int site = sitesArr[siteIdx];
			if (cell[site] >= 0)
				throw new IllegalArgumentException("Duplicate site: " + site);
			cell[site] = siteIdx;
			heap.insert(site, 0.0);
		}

		while (heap.isNotEmpty()) {
			int u = heap.extractMin();
			double uDistance = distance[u];
			int uCell = cell[u];

			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				boolean vInHeap = heap.isInserted(v);
				if (!vInHeap && distance[v] != Double.POSITIVE_INFINITY)
					continue;
				double ew = w.weight(e);
				Assertions.onlyPositiveWeight(ew);
				double vDistance = uDistance + ew;

				if (!vInHeap) {
					heap.insert(v, vDistance);
					backtrack[v] = e;
					cell[v] = uCell;
				} else if (vDistance < heap.key(v)) {
					heap.decreaseKey(v, vDistance);
					backtrack[v] = e;
					cell[v] = uCell;
				}
			}
		}
		boolean hasUnreachable = false;
		int unreachableCell = sitesArr.length;
		for (int v : range(n)) {
			if (cell[v] < 0) {
				hasUnreachable = true;
				cell[v] = unreachableCell;
			}
		}
		int blockNum = sitesArr.length + (hasUnreachable ? 1 : 0);

		return new VoronoiAlgos.ResultImpl(g, blockNum, cell, distance, backtrack, sitesArr);
	}

}
