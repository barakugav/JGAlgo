package com.ugav.algo;

public interface LCAStatic {

	/**
	 * Perform a static preprocessing of a tree for future LCA (Lowest common
	 * ancestor) queries
	 *
	 * @param t a tree
	 * @param r root of the tree
	 * @return a result data structure that can calculate efficiently the LCA of any
	 *         given query
	 */
	public void preprocessLCA(Graph t, int r);

	/**
	 * Calculate the LCA (Lowest common ancestor) of two vertices
	 *
	 * Can be called only after preprocessing of a tree
	 *
	 * @param u first vertex
	 * @param v second vertex
	 * @return the index of the LCA index of the two vertices
	 */
	public int calcLCA(int u, int v);

}
