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
import com.jgalgo.alg.MinimumVertexCutUtils.AuxiliaryGraph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.BitmapSet;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Esfahanian-Hakimi algorithm for computing a minimum unweighted vertex cut in a graph.
 *
 * <p>
 * Based on 'On computing the connectivities of graphs and digraphs' by Abdol H. Esfahanian, S. Louis Hakimi.
 *
 * @author Barak Ugav
 */
class MinimumVertexCutGlobalEsfahanianHakimi extends MinimumVertexCutUtils.AbstractImplGlobal {

	private final MinimumVertexCutSTEdgeCut minCutStAlgo = new MinimumVertexCutSTEdgeCut();

	@Override
	IntSet computeMinimumCut(IndexGraph g, IWeightFunction w) {
		Assertions.onlyCardinality(w);
		if (g.vertices().isEmpty())
			throw new IllegalArgumentException("No vertex cut exists in an empty graph");

		final int n = g.vertices().size();

		Neighbors neighbors = new Neighbors(g);
		int minCutSize = n - 1;
		int startVertex = -1;
		boolean startVertexNeighborsIsOut = false;
		if (g.isDirected()) {
			for (int v : range(n)) {
				IntSet vNeighbors = neighbors.outNeighbors(v);
				if (minCutSize > vNeighbors.size()) {
					minCutSize = vNeighbors.size();
					startVertexNeighborsIsOut = true;
					startVertex = v;
				}
				vNeighbors = neighbors.inNeighbors(v);
				if (minCutSize > vNeighbors.size()) {
					minCutSize = vNeighbors.size();
					startVertexNeighborsIsOut = false;
					startVertex = v;
				}
			}
		} else {
			for (int v : range(n)) {
				IntSet vNeighbors = neighbors.outNeighbors(v);
				if (minCutSize > vNeighbors.size()) {
					minCutSize = vNeighbors.size();
					startVertexNeighborsIsOut = true;
					startVertex = v;
				}
			}
		}
		if (startVertex == -1) {
			/* the graph is a clique, we can disconnect it only by removing n-1 vertices */
			return range(g.vertices().size() - 1);
		}

		int[] minCut =
				(startVertexNeighborsIsOut ? neighbors.outNeighbors(startVertex) : neighbors.inNeighbors(startVertex))
						.toIntArray();

		final AuxiliaryGraph auxiliaryGraph = new AuxiliaryGraph(g, null);

		IntSet startVertexNeighbors = neighbors.neighbors(startVertex);

		for (int v : range(n)) {
			if (v == startVertex || startVertexNeighbors.contains(v))
				continue;
			int[] cut = minCutStAlgo.computeMinCut(g, startVertex, v, auxiliaryGraph);
			if (cut != null && minCutSize > cut.length) {
				minCutSize = cut.length;
				minCut = cut;
			}
		}

		int d = startVertexNeighbors.size();
		int[] startVertexNeighborsArray = startVertexNeighbors.toIntArray();
		for (int i : range(d - 1)) { // should be up to d - 1
			for (int j : range(i + 1, d)) { // should be up to d - 1
				int u = startVertexNeighborsArray[i], v = startVertexNeighborsArray[j];
				int[] cut = minCutStAlgo.computeMinCut(g, u, v, auxiliaryGraph);
				if (cut != null && minCutSize > cut.length) {
					minCutSize = cut.length;
					minCut = cut;
				}
			}
		}

		return ImmutableIntArraySet.ofBitmap(Bitmap.fromOnes(n, minCut));
	}

	private static class Neighbors {
		private final IndexGraph g;
		private final BitmapSet neighbors;

		Neighbors(IndexGraph g) {
			this.g = g;
			final int n = g.vertices().size();
			neighbors = new BitmapSet(n);
		}

		IntSet outNeighbors(int source) {
			neighbors.clear();
			addOutNeighbors(source);
			return neighbors;
		}

		IntSet inNeighbors(int target) {
			neighbors.clear();
			addInNeighbors(target);
			return neighbors;
		}

		IntSet neighbors(int vertex) {
			neighbors.clear();
			addOutNeighbors(vertex);
			if (g.isDirected())
				addInNeighbors(vertex);
			return neighbors;
		}

		private void addOutNeighbors(int source) {
			for (IEdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (v != source)
					neighbors.set(v);
			}
		}

		private void addInNeighbors(int target) {
			for (IEdgeIter eit = g.inEdges(target).iterator(); eit.hasNext();) {
				eit.nextInt();
				int u = eit.sourceInt();
				if (u != target)
					neighbors.set(u);
			}
		}
	}

}
