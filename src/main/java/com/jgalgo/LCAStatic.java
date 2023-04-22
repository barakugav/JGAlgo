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
 * <pre> {@code
 * Graph tree = new GraphArrayUndirected();
 * int rt = tree.addVertex();
 * int v1 = tree.addVertex();
 * int v2 = tree.addVertex();
 * int v3 = tree.addVertex();
 * int v4 = tree.addVertex();
 * int v5 = tree.addVertex();
 *
 * tree.addEdge(rt, v1);
 * tree.addEdge(v1, v2);
 * tree.addEdge(v1, v3);
 * tree.addEdge(rt, v4);
 * tree.addEdge(v4, v5);
 *
 * LCAStatic lca = ...;
 * LCAStatic.DataStructure lcaDS = lca.preProcessTree(tree, rt);
 * assert lcaDS.findLowestCommonAncestor(v1, v4) == rt;
 * assert lcaDS.findLowestCommonAncestor(v2, v3) == v1;
 * assert lcaDS.findLowestCommonAncestor(v4, v5) == v4;
 * assert lcaDS.findLowestCommonAncestor(v2, v5) == rt;
 * }</pre>
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
	 * @return a data structure built from the preprocessing, that can answer LCA
	 *         queries efficiently
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
