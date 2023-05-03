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

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * TSP \(2\)-approximation using MST.
 * <p>
 * An MST of a graph weight is less or equal to the optimal TSP tour. By doubling each edge in such MST and finding an
 * Eulerian tour on these edges a tour was found with weight at most \(2\) times the optimal TSP tour. In addition,
 * shortcuts are used if vertices are repeated in the initial Eulerian tour - this is possible only in the metric
 * special case.
 * <p>
 * The running of this algorithm is \(O(n^2)\) and it achieve \(2\)-approximation to the optimal TSP solution.
 *
 * @author Barak Ugav
 */
public class TSPMetricMSTAppx implements TSPMetric {

	/*
	 * If true, the algorithm will validate the distance table and check the metric constrain is satisfied. This
	 * increases the running time to O(n^3)
	 */
	private static final boolean VALIDATE_METRIC = true;
	private static final Object DoubleWeightKey = new Object();
	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Create a new TSP \(2\)-approximation algorithm.
	 */
	public TSPMetricMSTAppx() {}

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
