package com.jgalgo;

public interface LCAStatic {

	/**
	 * Perform a static pre processing of a tree for future LCA (Lowest common
	 * ancestor) queries
	 *
	 * @param tree a tree
	 * @param root root of the tree
	 */
	public LCAStatic.DataStructure preProcessTree(Graph tree, int root);

	interface DataStructure {

		/**
		 * Calculate the LCA (Lowest common ancestor) of two vertices
		 *
		 * Can be called only after pre processing of a tree
		 *
		 * @param u first vertex
		 * @param v second vertex
		 * @return the index of the LCA index of the two vertices
		 */
		public int findLowestCommonAncestor(int u, int v);
	}

}
