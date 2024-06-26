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

package com.jgalgo.alg.span;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.ds.UnionFind;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Kruskal's minimum spanning tree algorithm.
 *
 * <p>
 * The algorithm first sort all the edges of the graph by their weight, and then examine them in increasing weight
 * order. For each examined edge, if it connects two connected components that were not connected beforehand, the edge
 * is added to the forest. The algorithm terminate after all edges were examined.
 *
 * <p>
 * The running time of the algorithm is \(O(m \log n)\) and it uses linear time. This algorithm perform good in practice
 * and its running time compete with other algorithms such as {@link MinimumSpanningTreePrim}, which have better time
 * bounds in theory. Note that only undirected graphs are supported.
 *
 * <p>
 * Based on "On the shortest spanning subtree of a graph and the traveling salesman problem" by Kruskal, J. B. (1956) in
 * the book "Proceedings of the American Mathematical Society".
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Kruskal%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class MinimumSpanningTreeKruskal extends MinimumSpanningTreeAbstract {

	private boolean parallelEnable = JGAlgoConfigImpl.ParallelByDefault;

	/**
	 * Construct a new MST algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link MinimumSpanningTree#newInstance()} to get a default implementation for the
	 * {@link MinimumSpanningTree} interface.
	 */
	public MinimumSpanningTreeKruskal() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	protected MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		final int n = g.vertices().size();
		final int m = g.edges().size();
		if (n == 0 || m == 0)
			return MinimumSpanningTrees.IndexResult.Empty;

		/* sort edges */
		int[] edges = range(m).toIntArray();
		JGAlgoUtils.sort(edges, 0, m, w, parallelEnable);

		/* create union find data structure for each vertex */
		UnionFind uf = UnionFind.newInstance();
		uf.makeMany(n);

		/* iterate over the edges and build the MST */
		IntArrayList mst = new IntArrayList(n - 1);
		for (int e : edges) {
			int U = uf.find(g.edgeSource(e));
			int V = uf.find(g.edgeTarget(e));

			if (U != V) {
				uf.union(U, V);
				mst.add(e);
			}
		}
		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return newIndexResult(mstSet);
	}

}
