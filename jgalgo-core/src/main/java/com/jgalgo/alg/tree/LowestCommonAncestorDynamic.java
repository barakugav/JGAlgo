/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.alg.tree;

/**
 * Dynamic algorithm for Lowest Common Ancestor (LCA) queries.
 *
 * <p>
 * The lowest common ancestor of two vertices in a tree is the vertex that appear in both vertices paths to the root
 * (common ancestor), and its farthest from the root (lowest). Algorithm implementing this interface support modifying
 * the tree by adding leafs as children to existing parents vertices, while supporting LCA queries.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * <pre> {@code
 * LowestCommonAncestorDynamic lca = LowestCommonAncestorDynamic.newInstance();
 * LowestCommonAncestorDynamic.Vertex rt = lca.initTree();
 * LowestCommonAncestorDynamic.Vertex n1 = lca.addLeaf(rt);
 * LowestCommonAncestorDynamic.Vertex n2 = lca.addLeaf(rt);
 * LowestCommonAncestorDynamic.Vertex n3 = lca.addLeaf(n1);
 *
 * assert lca.findLowestCommonAncestor(n1, n2) == rt;
 * assert lca.findLowestCommonAncestor(n1, n3) == n1;
 *
 * LowestCommonAncestorDynamic.Vertex n4 = lca.addLeaf(n1);
 * assert lca.findLowestCommonAncestor(n1, n4) == n1;
 * }</pre>
 *
 * @see    LowestCommonAncestorStatic
 * @see    LowestCommonAncestorOffline
 * @author Barak Ugav
 */
public interface LowestCommonAncestorDynamic {

	/**
	 * Initialize the tree the LCA will operate on and create a root vertex.
	 *
	 * @return                       the new root vertex
	 * @throws IllegalStateException if the tree is not empty
	 */
	public Vertex initTree();

	/**
	 * Add a new leaf vertex to the tree.
	 *
	 * @param  parent parent of the new vertex
	 * @return        the new vertex
	 */
	public Vertex addLeaf(Vertex parent);

	/**
	 * Find the lowest common ancestor of two vertices in the tree.
	 *
	 * @param  u the first vertex
	 * @param  v the second vertex
	 * @return   the lowest common ancestor of the two vertices
	 */
	public Vertex findLowestCommonAncestor(Vertex u, Vertex v);

	/**
	 * Get the number of vertices in the tree.
	 *
	 * @return number of vertices in the tree
	 */
	public int size();

	/**
	 * Clear the data structure by removing all vertices in the tree.
	 */
	public void clear();

	/**
	 * A tree vertex in an {@link LowestCommonAncestorDynamic} data structure.
	 *
	 * @author Barak Ugav
	 */
	public static interface Vertex {

		/**
		 * Get the parent vertex of this vertex.
		 *
		 * @return the parent of this vertex or {@code null} if this vertex is the root of the tree.
		 */
		public Vertex getParent();

		/**
		 * Get the user data of this vertex.
		 *
		 * <p>
		 * Note that the conversion of the data stored in the implementation to the user type is unsafe.
		 *
		 * @param  <D> the data type
		 * @return     the user data of this vertex
		 */
		public <D> D getData();

		/**
		 * Set the user data of this vertex.
		 *
		 * @param data new value for this vertex
		 */
		public void setData(Object data);

	}

	/**
	 * Create a new algorithm for dynamic LCA queries.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link LowestCommonAncestorDynamic} object.
	 *
	 * @return a default implementation of {@link LowestCommonAncestorDynamic}
	 */
	static LowestCommonAncestorDynamic newInstance() {
		return new LowestCommonAncestorDynamicGabowLongs();
	}

}
