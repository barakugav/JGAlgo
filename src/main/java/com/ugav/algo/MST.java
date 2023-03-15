package com.ugav.algo;

import java.util.Collection;

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

	/**
	 * Verify that the given edges are actually form a MST of g
	 *
	 * The verification is done by calculating for each original edge in g the
	 * maximum edge in the given MST. If all of the edges which are not in the MST
	 * have a bigger weight than the maximum one in the path of the MST, the MST is
	 * valid.
	 *
	 * @param g        a undirected graph
	 * @param w        weight function
	 * @param mstEdges collection of edges that form a MST
	 * @param tpmAlgo  tree path maximum algorithm, used for verification. The
	 *                 efficiency of the verification highly depends on this
	 *                 algorithm.
	 * @return true if the collection of edges form a MST of g
	 */
	public static <E> boolean verifyMST(Graph<E> g, WeightFunction<E> w, Collection<Edge<E>> mstEdges, TPM tpmAlgo) {
		Graph<E> mst = GraphArrayUndirected.valueOf(g.vertices(), mstEdges);
		return verifyMST(g, w, mst, tpmAlgo);
	}

	/**
	 * Verify that the given MST is actually a MST of g
	 *
	 * The verification is done by calculating for each original edge in g the
	 * maximum edge in the given MST. If all of the edges which are not in the MST
	 * have a bigger weight than the maximum one in the path of the MST, the MST is
	 * valid.
	 *
	 * @param g       a undirected graph
	 * @param w       weight function
	 * @param mst     spanning tree of g
	 * @param tpmAlgo tree path maximum algorithm, used for verification. The
	 *                efficiency of the verification highly depends on this
	 *                algorithm.
	 * @return true if the given spanning tree is a MST of g
	 */
	public static <E> boolean verifyMST(Graph<E> g, WeightFunction<E> w, Graph<E> mst, TPM tpmAlgo) {
		if (g instanceof Graph.Directed<?>)
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
			if (mstEdge == null || w.weight(e) < w.weight(mstEdge))
				return false;
			i++;
		}
		return true;
	}

}
