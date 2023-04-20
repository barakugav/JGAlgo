package com.jgalgo;

/**
 * Dynamic algorithm for Lowest Common Ancestor (LCA) queries.
 * <p>
 * The lowest common ancestor of two vertices in a tree is the vertex that
 * appear in both vertices paths to the root (common ancestor), and its farthest
 * from the root (lowest). Algorithm implementing this interface support
 * modifying the tree by adding leafs as children to existing parents nodes,
 * while supporting LCA queries.
 * <p>
 *
 * <pre> {@code
 * LCADynamic lca = ...;
 * Node rt = lca.initTree();
 * Node n1 = lca.addLeaf(rt);
 * Node n2 = lca.addLeaf(rt);
 * Node n3 = lca.addLeaf(n1);
 *
 * assert lca.findLowestCommonAncestor(n1, n2) == rt;
 * assert lca.findLowestCommonAncestor(n1, n3) == n1;
 *
 * Node n4 = lca.addLeaf(n1);
 * assert lca.findLowestCommonAncestor(n1, n4) == n1;
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface LCADynamic {

	/**
	 * Initialize the tree the LCA will operate on and create a root node.
	 * <p>
	 *
	 * @return the new root node
	 * @throws IllegalStateException if the tree is not empty
	 */
	public Node initTree();

	/**
	 * Add a new leaf node to the tree.
	 * <p>
	 *
	 * @param parent parent of the new node
	 * @return the new node
	 */
	public Node addLeaf(Node parent);

	/**
	 * Find the lowest common ancestor of two nodes in the tree.
	 * <p>
	 *
	 * @param u the first node
	 * @param v the second node
	 * @return the lowest common ancestor of the two nodes
	 */
	public Node findLowestCommonAncestor(Node u, Node v);

	/**
	 * Get the number of nodes in the tree.
	 *
	 * @return number of nodes in the tree
	 */
	public int size();

	/**
	 * Clear the data structure by removing all nodes in the tree.
	 */
	public void clear();

	/**
	 * A tree node in an {@link LCADynamic} data structure.
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
		 * @param <D> the data type
		 * @return the user data of this node
		 */
		public <D> D getNodeData();

		/**
		 * Set the user data of this node.
		 *
		 * @param data new value for this node
		 */
		public void setNodeData(Object data);

	}

}
