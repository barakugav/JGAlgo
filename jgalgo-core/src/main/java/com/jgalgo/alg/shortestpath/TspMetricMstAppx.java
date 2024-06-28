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

package com.jgalgo.alg.shortestpath;

import static com.jgalgo.internal.util.Range.range;
import java.util.Optional;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.span.MinimumSpanningTree;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Metric TSP \(2\)-approximation using minimum spanning trees.
 *
 * <p>
 * An MST weight of a graph is less or equal to the optimal TSP tour weight. By doubling each edge in such an MST and
 * finding an Eulerian tour on these edges a tour can be found with weight at most \(2\) times the optimal TSP tour
 * weight. In addition, shortcuts are used if vertices are repeated in the initial Eulerian tour - this is possible only
 * in the metric special case.
 *
 * <p>
 * This algorithm can only accept graphs and weight functions that satisfy the metric condition: every three vertices
 * \(u,v,w\) should satisfy \(d((u,v)) + d((v,w)) \leq d((u,w))$ where \(d(\cdot)\) is the distance of the shortest path
 * between two vertices. This condition is not validated for performance reason, but graphs that do not satisfy this
 * condition will result in an undefined behaviour.
 *
 * <p>
 * The running of this algorithm is \(O(n^2)\) and it achieve \(2\)-approximation to the optimal TSP solution.
 *
 * @author Barak Ugav
 */
public class TspMetricMstAppx extends TspAbstract {

	private final MinimumSpanningTree mstAlgo = MinimumSpanningTree.newInstance();

	/**
	 * Create a new TSP \(2\)-approximation algorithm.
	 */
	public TspMetricMstAppx() {}

	@Override
	protected Optional<IPath> computeShortestTour(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		Assertions.noParallelEdges(g, "parallel edges are not supported");
		final int n = g.vertices().size();
		if (n == 0)
			return Optional.empty();

		/* Calculate MST */
		IntCollection mst = ((MinimumSpanningTree.IResult) mstAlgo.computeMinimumSpanningTree(g, w)).edges();
		if (mst.size() < n - 1)
			return Optional.empty(); // graph is not connected

		/* Build a graph with each MST edge duplicated */
		IndexGraphBuilder g1Builder = IndexGraphBuilder.undirected();
		g1Builder.addVertices(range(n));
		g1Builder.ensureEdgeCapacity(mst.size() + mst.size() * 2);
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
		return Optional.of(cycle);
	}

}
