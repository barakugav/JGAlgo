package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graphs.EdgeWeightComparator;

public class MSTKruskal1956 implements MST {

	/*
	 * O(m log n)
	 */

	public MSTKruskal1956() {
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w) {
		if (g instanceof Graph.Directed<?>)
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();
		if (n == 0)
			return Collections.emptyList();

		/* sort edges */
		@SuppressWarnings("unchecked")
		Edge<E>[] edges = g.edges().toArray(new Edge[g.edges().size()]);
		Arrays.sort(edges, new EdgeWeightComparator<>(w));

		/* create union find data structure for each vertex */
		UnionFind uf = new UnionFindArray(n);

		/* iterate over the edges and build the MST */
		Collection<Edge<E>> mst = new ArrayList<>(n - 1);
		for (Edge<E> e : edges) {
			int u = e.u(), v = e.v();

			if (uf.find(u) != uf.find(v)) {
				uf.union(u, v);
				mst.add(e);
			}
		}
		return mst;
	}

}
