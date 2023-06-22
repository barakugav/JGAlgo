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

import java.util.Arrays;
import com.jgalgo.graph.GraphBuilderFixedUnmapped;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * TSP \(3/2\)-approximation using maximum matching.
 * <p>
 * The running of this algorithm is \(O(n^3)\) and it achieve \(3/2\)-approximation to the optimal TSP solution.
 * <p>
 * Based on 'Worst-Case Analysis of a New Heuristic for the Travelling Salesman Problem' by Nicos Christofides (1976).
 *
 * @author Barak Ugav
 */
public class TSPMetricMatchingAppx extends TSPMetricUtils.AbstractImpl {

	private final MinimumSpanningTree mstAlgo = MinimumSpanningTree.newBuilder().build();
	private final MaximumMatching matchingAlgo = MaximumMatching.newBuilder().build();

	/**
	 * Create a new TSP \(3/2\)-approximation algorithm.
	 */
	public TSPMetricMatchingAppx() {}

	@Override
	Path computeShortestTour(IndexGraph g, WeightFunction w) {
		final int n = g.vertices().size();
		if (n == 0)
			return null;
		ArgumentCheck.onlyUndirected(g);
		ArgumentCheck.noParallelEdges(g, "parallel edges are not supported");
		// TSPMetricUtils.checkArgDistanceTableIsMetric(distances);

		/* Calculate MST */
		IntCollection mst = mstAlgo.computeMinimumSpanningTree(g, w).edges();
		if (mst.size() < n - 1)
			throw new IllegalArgumentException("graph is not connected");

		/*
		 * Build graph for the matching calculation, containing only vertices with odd degree from the MST
		 */
		int[] degree = GraphsUtils.calcDegree(g, mst);
		int mGn = 0;
		int[] mVtoV = new int[n];
		for (int u = 0; u < n; u++)
			if (degree[u] % 2 != 0)
				mVtoV[mGn++] = u;
		IndexGraph mG = Graphs.newCompleteGraphUndirected(mGn);
		Weights.Double mGWeightsNeg = Weights.createExternalEdgesWeights(mG, double.class);
		for (int e : mG.edges()) {
			int u = mVtoV[mG.edgeSource(e)];
			int v = mVtoV[mG.edgeTarget(e)];
			mGWeightsNeg.set(e, -w.weight(g.getEdge(u, v)));
		}

		/* Calculate maximum matching between the odd vertices */
		Matching matching = matchingAlgo.computeMaximumWeightedPerfectMatching(mG, mGWeightsNeg);

		/* Build a graph of the union of the MST and the matching result */
		GraphBuilderFixedUnmapped g1Builder = GraphBuilderFixedUnmapped.newUndirected();
		for (int v = 0; v < n; v++) {
			int vFixed = g1Builder.addVertex();
			assert vFixed == v;
		}
		int[] g1EdgeRef = new int[mst.size()];
		for (int e : mst) {
			int g1Edge = g1Builder.addEdge(g.edgeSource(e), g.edgeTarget(e));
			if (g1Edge == g1EdgeRef.length)
				g1EdgeRef = Arrays.copyOf(g1EdgeRef, Math.max(2, 2 * g1EdgeRef.length));
			g1EdgeRef[g1Edge] = e;
		}
		for (int mGedge : matching.edges()) {
			int u = mVtoV[mG.edgeSource(mGedge)];
			int v = mVtoV[mG.edgeTarget(mGedge)];
			int g1Edge = g1Builder.addEdge(u, v);
			if (g1Edge == g1EdgeRef.length)
				g1EdgeRef = Arrays.copyOf(g1EdgeRef, Math.max(2, 2 * g1EdgeRef.length));
			g1EdgeRef[g1Edge] = g.getEdge(u, v);
		}
		IndexGraph g1 = g1Builder.build();

		Path cycle = TSPMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, g1EdgeRef);

		/* Convert cycle of edges to list of vertices */
		return cycle;
	}

}
