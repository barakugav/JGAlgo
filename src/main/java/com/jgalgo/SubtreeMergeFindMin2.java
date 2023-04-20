package com.jgalgo;

import java.util.NoSuchElementException;

/**
 * Subtree Merge Find Min data structure.
 * <p>
 * Subtree Merge Find min is a data structure used in maximum weighted matching
 * in general graphs. At any moment, a tree is maintain, divided into sub trees
 * of continues nodes. AddLeaf operation is supported to add leaves to the tree.
 * Merge operation can be used to union two adjacent sub trees into one, which
 * doesn't change the actual tree structure, only the subtrees groups in it. The
 * last two supported operations are addNonTreeEdge(u,v,weight) and
 * findMinNonTreeEdge(), which add a edge with some weight without affecting the
 * tree structure, and the findMin operation query for the non tree edge with
 * minimum weight that connects two different subtrees.
 *
 * @author Barak Ugav
 */
public interface SubtreeMergeFindMin2<E> {

	/**
	 * Init the tree and create the root node.
	 *
	 * @return the root node
	 * @throws IllegalStateException if the tree is not empty
	 */
	public Node initTree();

	/**
	 * Add a new node to the tree as leaf.
	 *
	 * @param parent the parent node
	 * @return the new node
	 */
	public Node addLeaf(Node parent);

	/**
	 * Check if two nodes are in the same sub tree.
	 *
	 * @param u the first node
	 * @param v the second node
	 * @return {@code true} if both of the nodes are in the same sub tree
	 */
	public boolean isSameSubTree(Node u, Node v);

	/**
	 * Merge two adjacent sub tree.
	 * <p>
	 * If the two nodes are already in the same sub tree, this operation has no
	 * effect.
	 *
	 * @param u a node from the first subtree
	 * @param v a node from the second subtree
	 * @throws IllegalArgumentException if the two nodes are from different subtrees
	 *                                  which are not adjacent
	 */
	public void mergeSubTrees(Node u, Node v);

	/**
	 * Add a non tree edge to the data structure.
	 *
	 * @param u        source node
	 * @param v        target node
	 * @param edgeData data of the new edge
	 */
	public void addNonTreeEdge(Node u, Node v, E edgeData);

	/**
	 * Check if the data structure contains any edge between two different sub
	 * trees.
	 *
	 * @return {@code true} if an edge exists between two different sub tress
	 */
	public boolean hasNonTreeEdge();

	/**
	 * Get the edge between two different sub trees with minimum weight.
	 *
	 * @return minimum weight edge between two different sub trees
	 * @throws NoSuchElementException if there is no such edge
	 */
	public MinEdge<E> findMinNonTreeEdge();

	/**
	 * Get the number of nodes in the tree.
	 *
	 * @return number of nodes
	 */
	public int size();

	/**
	 * Clear the data structure.
	 */
	public void clear();

	/**
	 * A result of {@link SubtreeMergeFindMin2#findMinNonTreeEdge()} query.
	 *
	 * @author Barak Ugav
	 */
	public static interface MinEdge<E> {

		/**
		 * The source node of the edge.
		 *
		 * @return the edge source node
		 */
		public Node u();

		/**
		 * The target node of the edge.
		 *
		 * @return the edge target node
		 */
		public Node v();

		/**
		 * Get the edge data.
		 *
		 * @return the edge data
		 */
		public E edgeData();

	}

	/**
	 * A tree node in an {@link SubtreeMergeFindMin2} data structure.
	 *
	 * @author Barak Ugav
	 */
	public static interface Node {

		/**
		 * Get the parent node of this node.
		 *
		 * @return the parent of this node or {@code null} if this node is the root of
		 *         the tree.
		 */
		public Node getParent();

		/**
		 * Get the user data of this node.
		 * <p>
		 * Note that the conversion of the data stored in the implementation to the user
		 * type is unsafe.
		 *
		 * @param <V> the data type
		 * @return the user data of this node
		 */
		public <V> V getNodeData();

		/**
		 * Set the user data of this node.
		 *
		 * @param data new value for this node
		 */
		public void setNodeData(Object data);

	}

}
