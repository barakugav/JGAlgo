package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graphs.EdgeWeightComparator;

public class MSTKruskal implements MST {

	private MSTKruskal() {
	}

	private static final MSTKruskal INSTANCE = new MSTKruskal();

	public static MSTKruskal getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();
		if (n == 0)
			return Collections.emptyList();

		/* sort edges */
		@SuppressWarnings("unchecked")
		Edge<E>[] edges = new Edge[g.edgesNum()];
		int i = 0;
		for (Iterator<Edge<E>> it = g.edges(); it.hasNext();)
			edges[i++] = it.next();
		Arrays.sort(edges, new EdgeWeightComparator<>(w));

		/* create union find data structure for each vertex */
		UnionFind uf = UnionFindImpl.getInstance();
		@SuppressWarnings("unchecked")
		UnionFind.Element<Integer>[] verticesUfElms = new UnionFind.Element[n];
		for (int v = 0; v < n; v++)
			verticesUfElms[v] = uf.make(v);

		/* iterate over the edges and build the MST */
		Collection<Edge<E>> mst = new ArrayList<>(n - 1);
		for (Edge<E> e : edges) {
			int u = e.u(), v = e.v();
			UnionFind.Element<Integer> ufElm = verticesUfElms[u], vfElm = verticesUfElms[v];

			if (uf.find(ufElm) != uf.find(vfElm)) {
				uf.union(ufElm, vfElm);
				mst.add(e);
			}
		}
		return mst;
	}

}
