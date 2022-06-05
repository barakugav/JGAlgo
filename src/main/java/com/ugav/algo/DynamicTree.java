package com.ugav.algo;

public interface DynamicTree<E> {

	/**
	 * Create a new tree in the forest with a single node
	 *
	 * @return an identifier of the node
	 */
	public int makeTree();

	/**
	 * Find the root of the tree containing v
	 *
	 * @param v a node
	 * @return the root of the tree containing v
	 */
	public int findRoot(int v);

	/**
	 * Find the minimum edge on the path from a node to it's tree root
	 *
	 * @param v a node
	 * @return the minimum edge from v to it's tree root, or null if no such edge
	 *         found
	 */
	public MinEdge<E> findMinEdge(int v);

	/**
	 * Add a weight to all of the edges from v to it's tree root
	 *
	 * @param v a node
	 * @param w a weight to add
	 */
	public void addWeight(int v, double w);

	/**
	 * Link a root to be a child of some other node of another tree
	 *
	 * @param u   a root of some tree
	 * @param v   a node in another tree
	 * @param w   the new edge weight
	 * @param val user param of the edge
	 */
	public void link(int u, int v, double w, E val);

	/**
	 * Remove the edge from a node to it's parent
	 *
	 * @param v a node
	 */
	public void cut(int v);

	/**
	 * Change the root of a tree to be a node
	 *
	 * @param v a node which will be the new root of it's tree
	 */
	public void evert(int v);

	/**
	 * Get the parent of a node in the tree
	 *
	 * @param v a node
	 * @return parent of v or -1 if it has no parent
	 */
	public int getParent(int v);

	/**
	 * Clear the whole data structure
	 */
	public void clear();

	public static interface MinEdge<E> {

		public int u();

		public int v();

		public double weight();

		public E val();

	}

}
