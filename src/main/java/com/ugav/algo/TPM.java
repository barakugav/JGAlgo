package com.ugav.algo;

import java.util.Iterator;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

/* Tree-Path Maxima */
public interface TPM {

	public <E> Edge<E>[] calcTPM(Graph<E> t, WeightFunction<E> w, int[] queries);

	public static <E> boolean verifyMST(TPM tpmAlgo, Graph<E> g, WeightFunction<E> w, Graph<E> mst) {
		if (g.isDirected())
			throw new IllegalArgumentException("Directed graphs are not supported");
		if (!Graphs.isTree(mst))
			return false;

		int m = g.edgesNum();
		int[] queries = new int[m * 2];

		@SuppressWarnings("unchecked")
		Edge<E>[] edges = new Edge[m];
		int i = 0;
		for (Iterator<Edge<E>> it = g.edges(); it.hasNext();) {
			Edge<E> e = it.next();
			queries[i * 2] = e.u();
			queries[i * 2 + 1] = e.v();
			edges[i++] = e;
		}

		Edge<E>[] tpmResults = tpmAlgo.calcTPM(mst, w, queries);

		for (i = 0; i < m; i++) {
			Edge<E> mstEdge = tpmResults[i];
			Edge<E> nonMstEdge = edges[i];
			if (w.weight(nonMstEdge) < w.weight(mstEdge))
				return false;
		}
		return true;
	}

}
