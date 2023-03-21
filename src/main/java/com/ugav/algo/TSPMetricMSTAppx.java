package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

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
		UGraph g = new GraphTableUndirected(n);
//		EdgeData.Double weights = new EdgeDataArray.Double(n * (n + 1) / 2);
		EdgesWeight.Double weights = g.newEdgeWeightDouble("weight");
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				weights.set(g.addEdge(u, v), distances[u][v]);

		/* Calculate MST */
		IntCollection mst = new MSTPrim1957().calcMST(g, weights);

		/* Build a graph with each MST edge duplicated */
		UGraph g1 = new GraphArrayUndirected(n);
//		EdgeData.Int edgeRef = new EdgeDataArray.Int(n - 1);
		EdgesWeight.Int edgeRef = g1.newEdgeWeightInt("edgeRef");
		for (IntIterator it = mst.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			edgeRef.set(g1.addEdge(u, v), e);
			edgeRef.set(g1.addEdge(u, v), e);
		}

		IntList cycle = TSPMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, edgeRef);
		assert cycle.size() == n;

		/* Convert cycle of edges to list of vertices */
		return TSPMetricUtils.edgeListToVerticesList(g, cycle).toIntArray();
	}

}
