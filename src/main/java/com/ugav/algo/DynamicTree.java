package com.ugav.algo;

public interface DynamicTree {

	/**
	 * Create a new tree in the forest with a single node
	 *
	 * @return an identifier of the node
	 */
	public Node makeTree();

	/**
	 * Find the root of the tree containing v
	 *
	 * @param v a node
	 * @return the root of the tree containing v
	 */
	public Node findRoot(Node v);

	/**
	 * Find the minimum edge on the path from a node to it's tree root
	 *
	 * @param v a node
	 * @return node with edge to parent with minimum weight and it's weight
	 */
	public Pair<Node, Double> findMinEdge(Node v);

	/**
	 * Add a weight to all of the edges from v to it's tree root
	 *
	 * @param v a node
	 * @param w a weight to add
	 */
	public void addWeight(Node v, double w);

	/**
	 * Link a root to be a child of some other node of another tree
	 *
	 * @param u a root of some tree
	 * @param v a node in another tree
	 * @param w the new edge weight
	 */
	public void link(Node u, Node v, double w);

	/**
	 * Remove the edge from a node to it's parent
	 *
	 * @param v a node
	 */
	public void cut(Node v);

	/**
	 * Change the root of a tree to be a node
	 *
	 * @param v a node which will be the new root of it's tree
	 */
	public void evert(Node v);

	public static interface Node {
	}

}
