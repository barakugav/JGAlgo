package com.ugav.algo;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

/* Tree-Path Maxima */
public interface TPM {

	/**
	 * Calculate all tree path maxima (TPM)
	 *
	 * Given a tree, weight function and queries pairs of vertices, the function
	 * will calculate for each query (u,v) the edge with the maximum weight in the
	 * path from u to v in the given tree. This can be used to validate MST.
	 *
	 * @param t          a tree
	 * @param w          a weight function
	 * @param queries    array of queries in format [u1, v1, u2, v2, ...]
	 * @param queriesNum number of queries in the queries array
	 * @return array of edges in the same size as queriesNum, where each edge is the
	 *         edge with maximum weight in the path from u to v in the tree.
	 */
	public <E> Edge<E>[] calcTPM(Graph<E> t, WeightFunction<E> w, int[] queries, int queriesNum);

}
