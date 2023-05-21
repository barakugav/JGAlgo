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

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * TSP \(3/2\)-approximation using maximum matching.
 * <p>
 * The running of this algorithm is \(O(n^3)\) and it achieve \(3/2\)-approximation to the optimal TSP solution.
 *
 * @author Barak Ugav
 */
public class TSPMetricMatchingAppx implements TSPMetric {

	private final MST mstAlgo = MST.newBuilder().build();
	private final MaximumMatching matchingAlgo = MaximumMatching.newBuilder().build();

	private static final Object EdgeWeightKey = new Object();
	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Create a new TSP \(3/2\)-approximation algorithm.
	 */
	public TSPMetricMatchingAppx() {}

	@Override
	public Path computeShortestTour(Graph g, EdgeWeightFunc w) {
		final int n = g.vertices().size();
		if (n == 0)
			return null;
		ArgumentCheck.onlyUndirected(g);
		TSPMetricUtils.checkNoParallelEdges(g);
		// TSPMetricUtils.checkArgDistanceTableIsMetric(distances);

		/* Calculate MST */
		IntCollection mst = mstAlgo.computeMinimumSpanningTree(g, w).edges();
		if (mst.size() < n - 1)
			throw new IllegalArgumentException("graph is not connected");

		/*
		 * Build graph for the matching calculation, containing only vertices with odd degree from the MST
		 */
		int[] degree = GraphsUtils.calcDegree(g, mst);
		Graph mG = GraphBuilder.newUndirected().build();
		int[] mVtoV = new int[n];
		for (int u = 0; u < n; u++)
			if (degree[u] % 2 != 0)
				mVtoV[mG.addVertex()] = u;
		int mGn = mG.vertices().size();
		Weights.Double mGWeightsNeg = mG.addEdgesWeights(EdgeWeightKey, double.class);
		Weights.Int mGEdgeRef = mG.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		for (int u = 0; u < mGn; u++) {
			for (int v = u + 1; v < mGn; v++) {
				int e = g.getEdge(mVtoV[u], mVtoV[v]);
				int en = mG.addEdge(u, v);
				mGWeightsNeg.set(en, -w.weight(g.getEdge(mVtoV[u], mVtoV[v])));
				mGEdgeRef.set(en, e);
			}
		}

		/* Calculate maximum matching between the odd vertices */
		Matching matching = matchingAlgo.computeMaximumWeightedPerfectMatching(mG, mGWeightsNeg);

		/* Build a graph of the union of the MST and the matching result */
		Graph g1 = GraphBuilder.newUndirected().build(n);
		Weights.Int g1EdgeRef = g1.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		for (IntIterator it = mst.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int g1Edge = g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
			g1EdgeRef.set(g1Edge, e);
		}
		for (IntIterator it = matching.edges().iterator(); it.hasNext();) {
			int mGedge = it.nextInt();
			int u = mVtoV[mG.edgeSource(mGedge)];
			int v = mVtoV[mG.edgeTarget(mGedge)];
			int g1Edge = g1.addEdge(u, v);
			g1EdgeRef.set(g1Edge, mGEdgeRef.getInt(mGedge));
		}

		Path cycle = TSPMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, g1EdgeRef);

		/* Convert cycle of edges to list of vertices */
		mG.clear();
		return cycle;
	}

}
