package com.ugav.algo;

public interface LCA {

	/**
	 * Perform a static preprocessing of a tree for future LCA (Lowest common
	 * ancestor) queries
	 *
	 * @param t a tree
	 * @param r root of the tree
	 * @return a result data structure that can calculate efficiently the LCA of any
	 *         given query
	 */
	public <E> Result preprocessLCA(Graph<E> t, int r);

	public static interface Result {

		/**
		 * Calculate the LCA (Lowest common ancestor) of two vertices
		 *
		 * @param u first vertex
		 * @param v second vertex
		 * @return the index of the LCA index of the two vertices
		 */
		public int query(int u, int v);

	}

}
