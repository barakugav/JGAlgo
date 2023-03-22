package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntCollection;

/* Directed version of MST */
public interface MDST extends MST {

	/**
	 * Calculate MDST from some vertex in the graph.
	 */
	@Override
	public IntCollection calcMST(Graph g, EdgeWeightFunc w);

	/**
	 * Calculate minimum directed spanning tree (MDST) in a directed graph, rooted
	 * at the given vertex
	 *
	 * @param g    a directed graph
	 * @param w    weight function
	 * @param root vertex in the graph the spanning tree will be rooted from
	 * @return all edges composing the spanning tree
	 */
	public IntCollection calcMST(Graph g, EdgeWeightFunc w, int root);

}
