package com.ugav.algo;

public interface DynamicTree<V, E> {

	/**
	 * Create a new tree in the forest with a single node
	 *
	 * @param nodeData user data for the node
	 * @return the new node
	 */
	public Node<V, E> makeTree(V nodeData);

	/**
	 * Find the root of the tree containing v
	 *
	 * @param v a node
	 * @return the root of the tree containing v
	 */
	public Node<V, E> findRoot(Node<V, E> v);

	/**
	 * Find the minimum edge on the path from a node to it's tree root
	 *
	 * @param v a node
	 * @return the minimum edge from v to it's tree root, or null if no such edge
	 *         found
	 */
	public MinEdge<V, E> findMinEdge(Node<V, E> v);

	/**
	 * Add a weight to all of the edges from v to it's tree root
	 *
	 * @param v a node
	 * @param w a weight to add
	 */
	public void addWeight(Node<V, E> v, double w);

	/**
	 * Link a root to be a child of some other node of another tree
	 *
	 * @param u        a root of some tree
	 * @param v        a node in another tree
	 * @param w        the new edge weight
	 * @param edgeData user data for the edge
	 */
	public void link(Node<V, E> u, Node<V, E> v, double w, E edgeData);

	/**
	 * Remove the edge from a node to it's parent
	 *
	 * @param v a node
	 */
	public void cut(Node<V, E> v);

	/**
	 * Change the root of a tree to be a node
	 *
	 * @param v a node which will be the new root of it's tree
	 */
	public void evert(Node<V, E> v);

	/**
	 * Get the number of nodes in the node tree
	 *
	 * @param v a node
	 * @return size of the node's tree
	 */
	public int size(Node<V, E> v);

	/**
	 * Clear the whole data structure
	 */
	public void clear();

	public static interface Node<V, E> {

		public V getNodeData();

		public void setNodeData(V data);

		public E getEdgeData();

		public void setEdgeData(E data);

		public Node<V, E> getParent();

	};

	public static interface MinEdge<V, E> {

		public Node<V, E> u();

		public double weight();

		public E getData();

	}

}
