package com.ugav.algo;

public interface SubtreeMergeFindmin<V, E> {

	/**
	 * Subtree Merge Find min is a data structure used in maximum weighted matching
	 * in general graphs. At any moment, a tree is maintain, divided into sub trees
	 * of continues nodes. AddLeaf operation is supported to add leaves to the tree.
	 * Merge operation can be used to union two adjacent sub trees into one, which
	 * doesn't change the actual tree structure, only the subtrees groups in it. The
	 * last two supported operations are addNonTreeEdge(u,v,weight) and
	 * findMinNonTreeEdge(), which add a edge with some weight without affecting the
	 * tree structure, and the findmin operation query for the non tree edge with
	 * minimum weight that connects two different subtrees.
	 */

	/**
	 * Init the tree and create the root node
	 *
	 * @param val node value of the root node
	 * @return the root node
	 * @throws IllegalStateException if the tree is not empty
	 */
	public Node<V> initTree(V val);

	/**
	 * Add a new node to the tree as leaf
	 *
	 * @param parent the parent node
	 * @param val    node value of the new node
	 * @return the new node
	 */
	public Node<V> addLeaf(Node<V> parent, V val);

	/**
	 * Check if two nodes are in the same sub tree
	 *
	 * @param u the first node
	 * @param v the second node
	 * @return true if both of the nodes are in the same sub tree
	 */
	public boolean isSameSubTree(Node<V> u, Node<V> v);

	/**
	 * Merge two adjacent sub tree
	 *
	 * If the two nodes are already in the same sub tree, this operation has no
	 * effect
	 *
	 * @param u a node from the first subtree
	 * @param v a node from the second subtree
	 * @throw IllegalArgumentException if the two nodes are from different subtrees
	 *        which are not adjacent
	 */
	public void mergeSubTrees(Node<V> u, Node<V> v);

	/**
	 * Add a non tree edge to the data structure
	 *
	 * @param u       source node
	 * @param v       target node
	 * @param edgeVal value of the new edge
	 */
	public void addNonTreeEdge(Node<V> u, Node<V> v, E edgeVal);

	/**
	 * Check if the data structure contains any edge between two different sub trees
	 *
	 * @return true if an edge exists between two different sub tress
	 */
	public boolean hasNonTreeEdge();

	/**
	 * Get the edge between two different sub trees with minimum weight
	 *
	 * @return minimum weight edge between two different sub trees
	 * @throw NoSuchElementException if there is no such edge
	 */
	public MinEdge<V, E> findMinNonTreeEdge();

	/**
	 * Get the number of nodes in the tree
	 *
	 * @return number of nodes
	 */
	public int size();

	/**
	 * Clear the data structure
	 */
	public void clear();

	public static interface MinEdge<V, E> {

		public Node<V> u();

		public Node<V> v();

		public E edgeVal();

	}

	public static interface Node<V> {

		public V getNodeData();

		public void setNodeVal(V val);

		public Node<V> getParent();

	}

}
