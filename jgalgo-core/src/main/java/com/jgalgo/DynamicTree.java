package com.jgalgo;

/**
 * Dynamic tree data structure that support link/cut operations.
 * <p>
 * The dynamic tree data structure is a set of nodes forming a forest and
 * support the following operations:
 * <ul>
 * <li>{@link #makeTree()} - create a new node which will form a tree of size
 * one.</li>
 * <li>{@link #findRoot(Node)} - find the root node of a given node.</li>
 * <li>{@link #link(Node, Node, double)} - link a node (root) to a parent with
 * weighted edge.</li>
 * <li>{@link #cut(Node)} - remove the edge from a node to its parent.</li>
 * <li>{@link #addWeight(Node, double)} - add a weight to all edges on the path
 * from a node to its tree root.</li>
 * <li>{@link #findMinEdge(Node)} - find the edge with minimum weight from on
 * the path from a node to its tree root.</li>
 * </ul>
 * <p>
 * Note: this API will change in the future
 *
 * @see <a href="https://en.wikipedia.org/wiki/Link/cut_tree">Wikipedia</a>
 * @author Barak Ugav
 */
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
	 * the minimum edge from a node to its tree root.
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

}
