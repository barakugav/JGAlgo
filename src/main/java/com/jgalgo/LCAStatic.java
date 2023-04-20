package com.jgalgo;

/**
 * Static Lowest Common Ancestor (LCA) algorithm.
 * <p>
 * The lowest common ancestor of two vertices in a tree is the vertex that
 * appear in both vertices paths to the root (common ancestor), and its farthest
 * from the root (lowest). Given a tree {@code G=(V,E)}, we would like to pre
 * process it and then answer queries of the type "what is the lower common
 * ancestor of two vertices {@code u} and {@code v}?".
 * <p>
 * Most implementation of this interface achieve linear or near linear
 * preprocessing time and constant or logarithmic query time.
 *
 * @author Barak Ugav
 */
public interface LCAStatic {

	/**
	 * Perform a static pre processing of a tree for future LCA (Lowest common
	 * ancestor) queries.
	 *
	 * @param tree a tree
	 * @param root root of the tree
	 */
	public LCAStatic.DataStructure preProcessTree(Graph tree, int root);

	/**
	 * Data structure result created from a static LCA pre-processing.
	 *
	 * @author Barak Ugav
	 */
	interface DataStructure {

		/**
		 * Find the lowest common ancestor of two vertices in the tree.
		 *
		 * @param u the first vertex
		 * @param v the second vertex
		 * @return the lowest common ancestor of {@code u} and {@code v}
		 */
		public int findLowestCommonAncestor(int u, int v);
	}

}
