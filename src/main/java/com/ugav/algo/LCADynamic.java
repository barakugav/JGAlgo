package com.ugav.algo;

public interface LCADynamic<V> {

	/**
	 * Initialize the tree the LCA will operate on and create a root node
	 *
	 * @param nodeData user data for the new node
	 * @return the new root node
	 * @throws IllegalStateException if the tree is not empty
	 */
	public Node<V> initTree(V nodeData);

	/**
	 * Add a new leaf in the tree
	 *
	 * @param parent   parent of the new node
	 * @param nodeData user data for the new node
	 * @return the new node
	 * @throws IllegalArgumentException if the parent identifier is not valid
	 */
	public Node<V> addLeaf(Node<V> parent, V nodeData);

	/**
	 * Calculate the lowest common ancestor of two nodes in the tree
	 *
	 * @param u the first node
	 * @param v the second node
	 * @return the lowest common ancestor of the two nodes
	 */
	public Node<V> calcLCA(Node<V> u, Node<V> v);

	/**
	 * Get the number of nodes in the tree
	 *
	 * @return nodes count in the tree
	 */
	public int size();

	/**
	 * Clear the data structure
	 */
	public void clear();

	public static interface Node<V> {

		public V getNodeData();

		public void setNodeData(V data);

		public Node<V> getParent();

	}

}
