package com.ugav.algo;

import java.util.Collection;
import java.util.List;



public class TSPMetricMSTAppx implements TSPMetric {

	/**
	 * Calculate a TSP 2-approximation in O(n^2) using MST
	 */

	/*
	 * If true, the algorithm will validate the distance table and check the metric
	 * constrain is satisfied. This increases the running time to O(n^3)
	 */
	private static final boolean VALIDATE_METRIC = true;

	public TSPMetricMSTAppx() {
	}

	@Override
	public int[] calcTSP(double[][] distances) {
		int n = distances.length;
		if (n == 0)
			return new int[0];
		TSPMetric.checkArgDistanceTableSymmetric(distances);
		if (VALIDATE_METRIC)
			TSPMetric.checkArgDistanceTableIsMetric(distances);

		/* Build graph from the distances table */
		Graph<Double> g = new GraphTableUndirected<>(n);
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				g.addEdge(u, v).setData(Double.valueOf(distances[u][v]));

		/* Calculate MST */
		Collection<Edge<Double>> mst = new MSTPrim1957().calcMST(g, Graphs.WEIGHT_FUNC_DEFAULT);

		/* Build a graph with each MST edge duplicated */
		Graph<Double> g1 = new GraphArrayUndirectedOld<>(n);
		for (Edge<Double> e : mst) {
			g1.addEdge(e);
			g1.addEdge(e.u(), e.v()).setData(e.data());
		}

		List<Edge<Double>> cycle = TSPMetricUtils.calcEulerianAndConvertToHamiltonianCycle(g, g1);
		assert cycle.size() == n;

		/* Convert cycle of edges to list of vertices */
		int[] res = new int[n];
		for (int i = 0; i < cycle.size(); i++)
			res[i] = cycle.get(i).u();
		return res;
	}

}
