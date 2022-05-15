package com.ugav.algo;

public interface LCADynamic {

	/**
	 * Initialize the tree the LCA will operate on
	 *
	 * @return identifier of the root node
	 */
	public int initTree();

	/**
	 * Add a new leaf in the tree
	 *
	 * @param parent identifier of the parent node
	 * @return identifier of the new node
	 */
	public int addLeaf(int parent);

	/**
	 * Calculate the lowest common ancestor of two nodes in the tree
	 *
	 * @param u identifier of the first node
	 * @param v identifier of the second node
	 * @return identifier of the lowest common ancestor of the two nodes
	 */
	public int calcLCA(int u, int v);

}
