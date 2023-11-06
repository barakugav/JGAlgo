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

import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * TSP \(2\)-approximation using MST.
 *
 * <p>
 * An MST of a graph weight is less or equal to the optimal TSP tour. By doubling each edge in such MST and finding an
 * Eulerian tour on these edges a tour was found with weight at most \(2\) times the optimal TSP tour. In addition,
 * shortcuts are used if vertices are repeated in the initial Eulerian tour - this is possible only in the metric
 * special case.
 *
 * <p>
 * The running of this algorithm is \(O(n^2)\) and it achieve \(2\)-approximation to the optimal TSP solution.
 *
 * @author Barak Ugav
 */
public class TspMetricMSTAppx extends TspMetricUtils.AbstractImpl {

	private final MinimumSpanningTree mstAlgo = MinimumSpanningTree.newInstance();
	// /*
	// * If true, the algorithm will validate the distance table and check the metric constrain is satisfied. This
	// * increases the running time to O(n^3)
	// */
	// private static final boolean VALIDATE_METRIC = true;

	/**
	 * Create a new TSP \(2\)-approximation algorithm.
	 */
	public TspMetricMSTAppx() {}

	@Override
	IPath computeShortestTour(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		if (n == 0)
			return null;
		Assertions.Graphs.onlyUndirected(g);
		Assertions.Graphs.noParallelEdges(g, "parallel edges are not supported");
		// if (VALIDATE_METRIC)
		// TSPMetricUtils.checkArgDistanceTableIsMetric(distances);

		/* Calculate MST */
		IntCollection mst = ((MinimumSpanningTree.IResult) mstAlgo.computeMinimumSpanningTree(g, w)).edges();
		if (mst.size() < n - 1)
			throw new IllegalArgumentException("graph is not connected");

		/* Build a graph with each MST edge duplicated */
		IndexGraphBuilder g1Builder = IndexGraphBuilder.newUndirected();
		g1Builder.expectedVerticesNum(n);
		g1Builder.expectedEdgesNum(mst.size() + mst.size() * 2);
		for (int v = 0; v < n; v++) {
			int vBuilder = g1Builder.addVertex();
			assert v == vBuilder;
		}
		int[] edgeRef = new int[mst.size() * 2];
		for (int e : mst) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			edgeRef[g1Builder.addEdge(u, v)] = e;
			edgeRef[g1Builder.addEdge(u, v)] = e;
		}
		IndexGraph g1 = g1Builder.build();

		IPath cycle = TspMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, edgeRef);
		assert cycle.edges().size() == n;
		assert cycle.isCycle();

		/* Convert cycle of edges to list of vertices */
		return cycle;
	}

}
