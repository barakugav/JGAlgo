package com.ugav.algo;

import java.util.Collection;
import java.util.List;



public class TSPMetricMatchingAppx implements TSPMetric {

	/**
	 * Calculate a TSP 3/2-approximation in O(n^3) using maximum matching
	 */

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
		Graph<Double> g = new GraphTableUndirected<>(n);
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				g.addEdge(u, v).setData(Double.valueOf(distances[u][v]));

		/* Calculate MST */
		Collection<Edge<Double>> mst = new MSTPrim1957().calcMST(g, Graphs.WEIGHT_FUNC_DEFAULT);

		/*
		 * Build graph for the matching calculation, containing only vertices with odd
		 * degree from the MST
		 */
		int[] degree = Graphs.calcDegree(mst, n);
		Graph<Double> mG = new GraphArrayUndirectedOld<>();
		int[] mVtoV = new int[n];
		for (int u = 0; u < n; u++)
			if (degree[u] % 2 == 1)
				mVtoV[mG.newVertex()] = u;
		int mGn = mG.vertices();
		for (int u = 0; u < mGn; u++)
			for (int v = u + 1; v < mGn; v++)
				mG.addEdge(u, v).setData(Double.valueOf(distances[mVtoV[u]][mVtoV[v]]));

		/* Calculate maximum matching between the odd vertices */
		Collection<Edge<Double>> matching = new MatchingWeightedGabow2017().calcPerfectMaxMatching(mG,
				e -> -e.data().doubleValue());
		mG.clear(); /* not needed anymore */

		/* Build a graph of the union of the MST and the matching result */
		Graph<Double> g1 = new GraphArrayUndirectedOld<>(n);
		for (Edge<Double> e : mst)
			g1.addEdge(e);
		for (Edge<Double> e : matching) {
			int u = mVtoV[e.u()], v = mVtoV[e.v()];
			g1.addEdge(u, v).setData(Double.valueOf(distances[u][v]));
		}

		List<Edge<Double>> cycle = TSPMetricUtils.calcEulerianAndConvertToHamiltonianCycle(g, g1);

		/* Convert cycle of edges to list of vertices */
		int[] res = new int[n];
		for (int i = 0; i < cycle.size(); i++)
			res[i] = cycle.get(i).u();
		return res;
	}

}
