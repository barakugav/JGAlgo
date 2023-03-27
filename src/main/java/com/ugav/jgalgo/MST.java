package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

public interface MST {

	/**
	 * Calculate the minimum spanning tree (MST) of a given graph
	 *
	 * @param g a graph
	 * @param w weight function
	 * @return all edges that compose the MST, n-1 if the graph is connected (or
	 *         some forest if not)
	 */
	public IntCollection calcMST(Graph g, EdgeWeightFunc w);

	/**
	 * Verify that the given edges actually form a MST of g
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
	public static boolean verifyMST(UGraph g, EdgeWeightFunc w, IntCollection mstEdges, TPM tpmAlgo) {
		int n = g.vertices().size();
		UGraph mst = new GraphArrayUndirected(n);
		Weights.Int edgeRef = mst.addEdgesWeight("edgeRef").ofInts();
		for (IntIterator it = mstEdges.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ne = mst.addEdge(u, v);
			edgeRef.set(ne, e);
		}

		return verifyMST(g, w, mst, tpmAlgo, edgeRef);
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
	public static boolean verifyMST(Graph g, EdgeWeightFunc w, Graph mst, TPM tpmAlgo, Weights.Int edgeRef) {
		if (g instanceof DiGraph)
			throw new IllegalArgumentException("Directed graphs are not supported");
		if (!Graphs.isTree(mst))
			return false;

		EdgeWeightFunc w0 = e -> w.weight(edgeRef.getInt(e));
		int m = g.edges().size();
		int[] queries = new int[m * 2];

		int i = 0;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			queries[i * 2] = g.edgeSource(e);
			queries[i * 2 + 1] = g.edgeTarget(e);
			i++;
		}

		int[] tpmResults = tpmAlgo.calcTPM(mst, w0, queries, m);

		i = 0;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int mstEdge = tpmResults[i++];
			if (mstEdge == -1 || w.weight(e) < w0.weight(mstEdge))
				return false;
		}
		return true;
	}

}
