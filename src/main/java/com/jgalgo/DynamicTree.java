package com.jgalgo;

public interface DynamicTree {

	/**
	 * Create a new tree in the forest with a single node.
	 *
	 * @return the new node
	 */
	public Node makeTree();

	/**
	 * Find the root of the tree containing {@code v}
	 *
	 * @param v a node
	 * @return the root of the tree containing {@code v}
	 */
	public Node findRoot(Node v);

	/**
	 * Find the minimum edge on the path from a node to it's tree root.
	 *
	 * @param v a node
	 * @return the minimum edge from {@code v} to it's tree root, or {@code null} if
	 *         no such edge exists
	 */
	public MinEdge findMinEdge(Node v);

	/**
	 * Add a weight to all of the edges from {@code v} to it's tree root
	 *
	 * @param v a node
	 * @param w a weight to add
	 */
	public void addWeight(Node v, double w);

	/**
	 * Link a root to be a child of some other node of another tree.
	 *
	 * @param child  a root of some tree
	 * @param parent a node in another tree
	 * @param w      the new edge weight
	 * @throws IllegalArgumentException if {@code child} is not a root or if
	 *                                  {@code child} and {@code root} are in the
	 *                                  same tree.
	 */
	public void link(Node child, Node parent, double w);

	/**
	 * Remove the edge from a node to it's parent.
	 *
	 * @param v a node
	 */
	public void cut(Node v);

	/**
	 * Change the root of a tree to be a node.
	 *
	 * @param v a node which will be the new root of it's tree
	 */
	public void evert(Node v);

	/**
	 * Get the number of nodes in the node tree.
	 *
	 * @param v a node
	 * @return size of the node's tree
	 */
	public int size(Node v);

	/**
	 * Clear the whole data structure
	 */
	public void clear();

	/**
	 * A node in the forest of {@link DynamicTree} data structure.
	 *
	 * @author Barak Ugav
	 */
	public static interface Node {

		/**
		 * Get the user data of this node.
		 *
		 * @param <V> the data type
		 * @return the user data or {@code null} if it was not set
		 */
		public <V> V getNodeData();

		/**
		 * Set the user data of this node.
		 *
		 * @param data new data for the node
		 */
		public void setNodeData(Object data);

		/**
		 * Get the parent node of this node.
		 *
		 * @return the parent node of this node
		 */
		public Node getParent();

	};

	/**
	 * A return type for {@link DynamicTree#findMinEdge(Node)} method representing
	 * the minimum edge from the node to its tree root.
	 *
	 * @author Barak Ugav
	 */
	public static interface MinEdge {

		/**
		 * Get the source of this edge.
		 * <p>
		 * The source was determined as the child node during the creation of the node
		 * via {@link DynamicTree#link(Node, Node, double)} operation.
		 *
		 * @return the edge source.
		 */
		public Node u();

		/**
		 * Get the weight of the edge.
		 * <p>
		 * The weight of the edge is a sum over the initial weight assigned during the
		 * {@link DynamicTree#link(Node, Node, double)} operation and all
		 * {@link DynamicTree#addWeight(Node, double)} operations that affected this
		 * edge.
		 *
		 * @return the weight of the edge.
		 */
		public double weight();

	}

	/**
	 * {@link DynamicTree} with {@code int} weights.
	 *
	 * @author Barak Ugav
	 */
	public static interface Int extends DynamicTree {

		/**
		 * Add a weight to all of the edges from {@code v} to it's tree root
		 *
		 * @param v a node
		 * @param w a weight to add
		 */
		public void addWeight(Node v, int w);

		@Deprecated
		@Override
		default void addWeight(Node v, double w) {
			addWeight(v, (int) w);
		}

		/**
		 * Link a root to be a child of some other node of another tree.
		 *
		 * @param child  a root of some tree
		 * @param parent a node in another tree
		 * @param w      the new edge weight
		 * @throws IllegalArgumentException if {@code child} is not a root or if
		 *                                  {@code child} and {@code root} are in the
		 *                                  same tree.
		 */
		public void link(Node child, Node parent, int w);

		@Deprecated
		@Override
		default void link(Node child, Node parent, double w) {
			link(child, parent, (int) w);
		}
	}

}
