package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * TSP 2-approximation using MST.
 * <p>
 * An MST of a graph weight is less or equal to the optimal TSP tour. By
 * doubling each edge in such MST and finding an Eulerian tour on these edges a
 * tour was found with weight at most {@code 2} times the optimal TSP tour. In
 * addition, shortcuts are used if vertices are repeated in the initial Eulerian
 * tour - this is possible only in the metric special case.
 * <p>
 * The running of this algorithm is {@code O(n^2)} and it achieve
 * 2-approximation to the optimal TSP solution.
 *
 * @author Barak Ugav
 */
public class TSPMetricMSTAppx implements TSPMetric {

	/*
	 * If true, the algorithm will validate the distance table and check the metric
	 * constrain is satisfied. This increases the running time to O(n^3)
	 */
	private static final boolean VALIDATE_METRIC = true;
	private static final Object DoubleWeightKey = new Object();
	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Create a new TSP 2-approximation algorithm.
	 */
	public TSPMetricMSTAppx() {
	}

	@Override
	public int[] computeShortestTour(double[][] distances) {
		int n = distances.length;
		if (n == 0)
			return IntArrays.EMPTY_ARRAY;
		TSPMetricUtils.checkArgDistanceTableSymmetric(distances);
		if (VALIDATE_METRIC)
			TSPMetricUtils.checkArgDistanceTableIsMetric(distances);

		/* Build graph from the distances table */
		UGraph g = new GraphTableUndirected(n);
		Weights.Double weights = g.addEdgesWeights(DoubleWeightKey, double.class);
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				weights.set(g.addEdge(u, v), distances[u][v]);

		/* Calculate MST */
		IntCollection mst = new MSTPrim().computeMinimumSpanningTree(g, weights);

		/* Build a graph with each MST edge duplicated */
		UGraph g1 = new GraphArrayUndirected(n);
		Weights.Int edgeRef = g1.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		for (IntIterator it = mst.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			edgeRef.set(g1.addEdge(u, v), e);
			edgeRef.set(g1.addEdge(u, v), e);
		}

		Path cycle = TSPMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, edgeRef);
		assert cycle.size() == n;

		/* Convert cycle of edges to list of vertices */
		return TSPMetricUtils.pathToVerticesList(cycle).toIntArray();
	}

}
