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

import java.util.function.IntConsumer;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 *
 *
 * <p>
 * Based on 'On computing the connectivities of graphs and digraphs' by Abdol H. Esfahanian, S. Louis Hakimi.
 *
 * @author Barak Ugav
 */
class MinimumEdgeCutGlobalEsfahanianHakimi extends MinimumEdgeCutGlobalAbstract {

	@Override
	IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		final int m = g.edges().size();
		if (g.isDirected()) {

		} else {
			/* build a spanning tree */
			Bitmap mstVertices = new Bitmap(n);
			Bitmap mstEdges = new Bitmap(m);
			Bitmap mstLeaves = new Bitmap(n);
			Bitmap isNeighbor = new Bitmap(n);
			IntList neighbors = new IntArrayList();
			IntConsumer addNeighbors = leaf -> {
				for (IEdgeIter eit = g.outEdges(leaf).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					if (!mstVertices.get(v) && !isNeighbor.get(v)) {
						mstEdges.set(e);
						mstVertices.set(v);
						mstLeaves.set(v);
					}
				}
				isNeighbor.clearAllUnsafe(neighbors);
				neighbors.clear();
			};

			int startVertex = g.vertices().iterator().nextInt();
			mstVertices.set(startVertex);
			addNeighbors.accept(startVertex);
			mstLeaves.set(startVertex, mstVertices.cardinality() < 3);
			for (;;) {
				int bestLeaf = -1, bestLeafNeighborsNum = 0;
				for (int leaf : mstLeaves) { // TODO this loop always runs O(n) times
					for (IEdgeIter eit = g.outEdges(leaf).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.targetInt();
						if (!mstVertices.get(v) && !isNeighbor.get(v)) {
							isNeighbor.set(v);
							neighbors.add(v);
						}
					}
					if (bestLeafNeighborsNum < neighbors.size()) {
						bestLeafNeighborsNum = neighbors.size();
						bestLeaf = leaf;
					}
					isNeighbor.clearAllUnsafe(neighbors);
					neighbors.clear();
				}
				if (bestLeafNeighborsNum == 0)
					break;
				mstLeaves.clear(bestLeaf);
				addNeighbors.accept(bestLeaf);
			}
			assert MinimumSpanningTree.isSpanningForest(g, new IntArrayList(mstEdges.iterator()));

			int[] P;
			if (mstLeaves.cardinality() <= n / 2) {
				P = new int[mstLeaves.cardinality()];
				int i = 0;
				for (int v : mstLeaves)
					P[i++] = v;
				assert i == P.length;
			} else {
				P = new int[mstVertices.cardinality() - mstLeaves.cardinality()];
				int i = 0;
				for (int v : mstVertices)
					if (!mstLeaves.get(v))
						P[i++] = v;
				assert i == P.length;
			}
			// if (P.length < 2)
		}
		return null;
	}

}
