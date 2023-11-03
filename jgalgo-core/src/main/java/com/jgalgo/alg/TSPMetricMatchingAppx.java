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

import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
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

	private final MinimumSpanningTree mstAlgo = MinimumSpanningTree.newInstance();
	private final MatchingAlgo matchingAlgo = MatchingAlgo.newInstance();

	/**
	 * Create a new TSP \(3/2\)-approximation algorithm.
	 */
	public TSPMetricMatchingAppx() {}

	@Override
	IPath computeShortestTour(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		if (n == 0)
			return null;
		Assertions.Graphs.onlyUndirected(g);
		Assertions.Graphs.noParallelEdges(g, "parallel edges are not supported");
		// TSPMetricUtils.checkArgDistanceTableIsMetric(distances);

		/* Calculate MST */
		IntCollection mst = ((MinimumSpanningTree.IResult) mstAlgo.computeMinimumSpanningTree(g, w)).edges();
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
		IndexGraphBuilder mG0 = IndexGraphBuilder.newUndirected();
		mG0.expectedVerticesNum(mGn);
		mG0.expectedEdgesNum(mGn * (mGn - 1) / 2);
		for (int v = 0; v < mGn; v++)
			mG0.addVertex();
		for (int v = 0; v < mGn; v++)
			for (int u = v + 1; u < mGn; u++)
				mG0.addEdge(v, u);
		IndexGraph oddGraph = mG0.reIndexAndBuild(true, true).graph();
		IWeightFunction mGWeights = e -> {
			int u = mVtoV[oddGraph.edgeSource(e)];
			int v = mVtoV[oddGraph.edgeTarget(e)];
			return w.weight(g.getEdge(u, v));
		};
		IndexGraph mG = mG0.reIndexAndBuild(true, true).graph();

		/* Calculate maximum matching between the odd vertices */
		IMatching matching = (IMatching) matchingAlgo.computeMinimumWeightedPerfectMatching(mG, mGWeights);

		/* Build a graph of the union of the MST and the matching result */
		IndexGraphBuilder g1Builder = IndexGraphBuilder.newUndirected();
		g1Builder.expectedVerticesNum(n);
		g1Builder.expectedEdgesNum(mst.size() + matching.edges().size());
		for (int v = 0; v < n; v++) {
			int vBuilder = g1Builder.addVertex();
			assert vBuilder == v;
		}
		int[] g1EdgeRef = new int[mst.size() + matching.edges().size()];
		for (int e : mst) {
			int g1Edge = g1Builder.addEdge(g.edgeSource(e), g.edgeTarget(e));
			g1EdgeRef[g1Edge] = e;
		}
		for (int mGedge : matching.edges()) {
			int u = mVtoV[mG.edgeSource(mGedge)];
			int v = mVtoV[mG.edgeTarget(mGedge)];
			int g1Edge = g1Builder.addEdge(u, v);
			g1EdgeRef[g1Edge] = g.getEdge(u, v);
		}
		IndexGraph g1 = g1Builder.build();

		IPath cycle = TSPMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, g1EdgeRef);

		/* Convert cycle of edges to list of vertices */
		return cycle;
	}

}
