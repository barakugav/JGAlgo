package com.ugav.algo;

import com.ugav.algo.Graph.WeightFunction;

import it.unimi.dsi.fastutil.ints.IntCollection;

public interface MST {

	/**
	 * Calculate the minimum spanning tree (MST) of a given graph
	 *
	 * @param g a graph
	 * @param w weight function
	 * @return all edges that compose the MST, n-1 if the graph is connected (or
	 *         some forest if not)
	 */
	public IntCollection calcMST(Graph<?> g, WeightFunction w);

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
	public static boolean verifyMST(Graph<?> g, WeightFunction w, IntCollection mstEdges, TPM tpmAlgo) {
		Graph<?> mst = GraphArrayUndirectedOld.valueOf(g.vertices(), mstEdges);
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
	public static boolean verifyMST(Graph<?> g0, WeightFunction w, Graph<?> mst, TPM tpmAlgo) {
		if (!(g0 instanceof Graph.Undirected<?>))
			throw new IllegalArgumentException("Directed graphs are not supported");
		Graph.Undirected<?> g = (Graph.Undirected<?>) g0;
		if (!Graphs.isTree(mst))
			return false;

		int m = g.edges();
		int[] queries = new int[m * 2];

		for (int e = 0; e < m; e++) {
			queries[e * 2] = g.getEdgeSource(e);
			queries[e * 2 + 1] = g.getEdgeTarget(e);
		}

		int[] tpmResults = tpmAlgo.calcTPM(mst, w, queries, m);

		int i = 0;
		for (int e = 0; e < m; e++) {
			int mstEdge = tpmResults[i];
			if (mstEdge == -1 || w.weight(e) < w.weight(mstEdge))
				return false;
		}
		return true;
	}

}
