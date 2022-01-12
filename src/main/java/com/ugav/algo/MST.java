package com.ugav.algo;

import java.util.Collection;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public interface MST {

	/**
	 * Calculate the minimum spanning tree (MST) of a given graph
	 *
	 * @param g a graph
	 * @param w weight function
	 * @return all edges that compose the MST, n-1 if the graph is connected (or
	 *         some forest if not)
	 */
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w);

	public static <E> boolean verifyMST(Graph<E> g, WeightFunction<E> w, Collection<Edge<E>> mstEdges, TPM tpmAlgo) {
		Graph<E> mst = GraphArray.valueOf(g.vertices(), mstEdges, DirectedType.Undirected);
		return verifyMST(g, w, mst, tpmAlgo);
	}

	public static <E> boolean verifyMST(Graph<E> g, WeightFunction<E> w, Graph<E> mst, TPM tpmAlgo) {
		if (g.isDirected())
			throw new IllegalArgumentException("Directed graphs are not supported");
		if (!Graphs.isTree(mst))
			return false;

		int m = g.edges().size();
		int[] queries = new int[m * 2];

		int i = 0;
		for (Edge<E> e : g.edges()) {
			queries[i * 2] = e.u();
			queries[i * 2 + 1] = e.v();
			i++;
		}

		Edge<E>[] tpmResults = tpmAlgo.calcTPM(mst, w, queries, m);

		i = 0;
		for (Edge<E> e : g.edges()) {
			Edge<E> mstEdge = tpmResults[i];
			if (w.weight(e) < w.weight(mstEdge))
				return false;
			i++;
		}
		return true;
	}

}
