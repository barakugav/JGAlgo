package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class TSPMetricMatchingAppx implements TSPMetric {

	/**
	 * Calculate a TSP 3/2-approximation in O(n^3) using maximum matching
	 */

	private static final Object EdgeWeightKey = new Object();
	private static final Object EdgeRefWeightKey = new Object();

	public TSPMetricMatchingAppx() {
	}

	@Override
	public int[] calcTSP(double[][] distances) {
		int n = distances.length;
		if (n == 0)
			return new int[0];
		TSPMetric.checkArgDistanceTableSymmetric(distances);
		TSPMetric.checkArgDistanceTableIsMetric(distances);

		/* Build graph from the distances table */
		UGraph g = new GraphTableUndirected(n);
		Weights.Double weights = EdgesWeights.ofDoubles(g, EdgeWeightKey);
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				weights.set(g.addEdge(u, v), distances[u][v]);

		/* Calculate MST */
		IntCollection mst = new MSTPrim1957().calcMST(g, weights);

		/*
		 * Build graph for the matching calculation, containing only vertices with odd
		 * degree from the MST
		 */
		int[] degree = Graphs.calcDegree(g, mst);
		UGraph mG = new GraphArrayUndirected();
		int[] mVtoV = new int[n];
		for (int u = 0; u < n; u++)
			if (degree[u] % 2 == 1)
				mVtoV[mG.addVertex()] = u;
		int mGn = mG.vertices().size();
		Weights.Double mGWeightsNeg = EdgesWeights.ofDoubles(mG, EdgeWeightKey);
		Weights.Int mGEdgeRef = EdgesWeights.ofInts(mG, EdgeRefWeightKey, -1);
		for (int u = 0; u < mGn; u++) {
			for (int v = u + 1; v < mGn; v++) {
				int e = g.getEdge(mVtoV[u], mVtoV[v]);
				int en = mG.addEdge(u, v);
				mGWeightsNeg.set(en, -distances[mVtoV[u]][mVtoV[v]]);
				mGEdgeRef.set(en, e);
			}
		}

		/* Calculate maximum matching between the odd vertices */
		IntCollection matching = new MatchingWeightedGabow2017().calcPerfectMaxMatching(mG, mGWeightsNeg);

		/* Build a graph of the union of the MST and the matching result */
		UGraph g1 = new GraphArrayUndirected(n);
		Weights.Int g1EdgeRef = EdgesWeights.ofInts(g1, EdgeRefWeightKey, -1);
		for (IntIterator it = mst.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int g1Edge = g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
			g1EdgeRef.set(g1Edge, e);
		}
		for (IntIterator it = matching.iterator(); it.hasNext();) {
			int mGedge = it.nextInt();
			int u = mVtoV[mG.edgeSource(mGedge)];
			int v = mVtoV[mG.edgeTarget(mGedge)];
			int g1Edge = g1.addEdge(u, v);
			g1EdgeRef.set(g1Edge, mGEdgeRef.getInt(mGedge));
		}

		IntList cycle = TSPMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, g1EdgeRef);

		/* Convert cycle of edges to list of vertices */
		int[] res = TSPMetricUtils.edgeListToVerticesList(g, cycle).toIntArray();

		mG.clear();

		return res;
	}

}
