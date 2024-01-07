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

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import com.jgalgo.alg.MinimumVertexCutUtils.AuxiliaryGraph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Kanevsky algorithm for computing all minimum unweighted vertex cuts in a graph.
 *
 * <p>
 * Based on 'Finding all minimum-size separating vertex sets in a graph' by A. Kanevsky (1993).
 *
 * @author Barak Ugav
 */
class MinimumVertexCutAllGlobalKanevsky extends MinimumVertexCutUtils.AbstractImplAllGlobal {

	private final MinimumVertexCutGlobal globalConnectivityAlgo = MinimumVertexCutGlobal.newInstance();
	private final MaximumFlow maxFlowAlgo = MaximumFlow.newInstance();
	private final MinimumEdgeCutAllSTPicardQueyranne minEdgeCutAllStAlgo = new MinimumEdgeCutAllSTPicardQueyranne();

	@Override
	Iterator<IntSet> minimumCutsIter(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		Assertions.onlyCardinality(w);

		/* Find k, the (global) connectivity of the graph */
		final int k = globalConnectivityAlgo.computeMinimumCut(g, null).size();

		return minimumCutsIter(g, k);
	}

	Iterator<IntSet> minimumCutsIter(IndexGraph g, int k) {
		if (k == 0)
			return Collections.emptyIterator();

		g = g.copy(); /* copy, we add edges to g */
		final int n = g.vertices().size();

		/* Find k vertices with highest degree */
		int[] vertices = g.vertices().toIntArray();
		/* Get (n-k)-th element in-place. After the computation the last k elements have the highest degrees */
		IndexGraph g0 = g;
		ArraysUtils
				.getKthElement(vertices, 0, n, n - k,
						(v1, v2) -> Integer.compare(g0.outEdges(v1).size(), g0.outEdges(v2).size()), true);
		IntSet kVertices = ImmutableIntArraySet.ofBitmap(Bitmap.fromOnes(n, IntIterators.wrap(vertices, n - k, k)));

		/*
		 * Unfortunately, we must check for duplicates cuts during the iteration. This require us to maintain a set of
		 * cuts. If we already use space proportional to the number of cuts, its simpler to implement the algorithm as a
		 * single run over all cuts instead of implementing the Iterator interface.
		 */
		Set<IntSet> cuts = new ObjectOpenHashSet<>();

		if (MinimumVertexCutGlobal.isCut(g0, kVertices)) {
			int[] cut = kVertices.toIntArray();
			cuts.add(ImmutableIntArraySet.withNaiveContains(cut));
		}

		AuxiliaryGraph auxGraph = new AuxiliaryGraph(g, null, true);
		for (int x : kVertices) {
			Bitmap nonAdjacent = new Bitmap(n);
			nonAdjacent.setAll();
			nonAdjacent.clear(x);
			for (IEdgeIter eit = g.outEdges(x).iterator(); eit.hasNext();) {
				eit.nextInt();
				nonAdjacent.clear(eit.targetInt());
			}

			for (int v : nonAdjacent) {
				int xAux = x * 2 + 1, vAux = v * 2 + 0;
				IFlow maxFlow = (IFlow) maxFlowAlgo
						.computeMaximumFlow(auxGraph.graph, null, Integer.valueOf(xAux), Integer.valueOf(vAux));
				int xvConnectivity = (int) maxFlow.getSupply(xAux);
				if (xvConnectivity == k) {
					Iterator<IVertexBiPartition> minEdgeCuts =
							minEdgeCutAllStAlgo.minimumCutsIter(auxGraph.graph, null, xAux, vAux, maxFlow);

					for (IVertexBiPartition minEdgeCut : IterTools.foreach(minEdgeCuts)) {
						int[] cut = minEdgeCut.crossEdges().toIntArray();
						auxGraph.edgeCutToVertexCut(cut);
						cuts.add(ImmutableIntArraySet.withNaiveContains(cut));
					}

					/* add edge (x,v) to g */
					g.addEdge(x, v);
					auxGraph.graph.addEdge(x * 2 + 1, v * 2 + 0);
					auxGraph.graph.addEdge(x * 2 + 1, v * 2 + 0);
					auxGraph.graph.addEdge(v * 2 + 1, x * 2 + 0);
					auxGraph.graph.addEdge(v * 2 + 1, x * 2 + 0);
				}

			}
		}

		/* The queue iterator stores the cuts and use less and less memory as the elements are consumed */
		Iterator<IntSet> cutsIter = JGAlgoUtils.queueIter(cuts);
		cuts.clear();
		return IterTools.map(cutsIter, cut -> ImmutableIntArraySet.ofBitmap(cut, n));
	}

}
