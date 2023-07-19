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

package com.jgalgo;

import com.jgalgo.internal.util.BuilderAbstract;

/**
 * Dynamic algorithm for Lowest Common Ancestor (LCA) queries.
 * <p>
 * The lowest common ancestor of two vertices in a tree is the vertex that appear in both vertices paths to the root
 * (common ancestor), and its farthest from the root (lowest). Algorithm implementing this interface support modifying
 * the tree by adding leafs as children to existing parents nodes, while supporting LCA queries.
 *
 * <pre> {@code
 * LowestCommonAncestorDynamic lca = LowestCommonAncestorDynamic.newBuilder().build();
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
public interface LowestCommonAncestorDynamic {

	/**
	 * Initialize the tree the LCA will operate on and create a root node.
	 *
	 * @return                       the new root node
	 * @throws IllegalStateException if the tree is not empty
	 */
	public Node initTree();

	/**
	 * Add a new leaf node to the tree.
	 *
	 * @param  parent parent of the new node
	 * @return        the new node
	 */
	public Node addLeaf(Node parent);

	/**
	 * Find the lowest common ancestor of two nodes in the tree.
	 *
	 * @param  u the first node
	 * @param  v the second node
	 * @return   the lowest common ancestor of the two nodes
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
	 * A tree node in an {@link LowestCommonAncestorDynamic} data structure.
	 *
	 * @author Barak Ugav
	 */
	public static interface Node {

		/**
		 * Get the parent node of this node.
		 *
		 * @return the parent of this node or {@code null} if this node is the root of the tree.
		 */
		public Node getParent();

		/**
		 * Get the user data of this node.
		 * <p>
		 * Note that the conversion of the data stored in the implementation to the user type is unsafe.
		 *
		 * @param  <D> the data type
		 * @return     the user data of this node
		 */
		public <D> D getNodeData();

		/**
		 * Set the user data of this node.
		 *
		 * @param data new value for this node
		 */
		public void setNodeData(Object data);

	}

	/**
	 * Create a new dynamic LCA algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link LowestCommonAncestorDynamic} object.
	 *
	 * @return a new builder that can build {@link LowestCommonAncestorDynamic} objects
	 */
	static LowestCommonAncestorDynamic.Builder newBuilder() {
		return new LowestCommonAncestorDynamic.Builder() {
			String impl;

			@Override
			public LowestCommonAncestorDynamic build() {
				if (impl != null) {
					switch (impl) {
						case "GabowLinear":
							return new LowestCommonAncestorDynamicGabowLinear();
						case "GabowSimple":
							return new LowestCommonAncestorDynamicGabowSimple();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new LowestCommonAncestorDynamicGabowLinear();
			}

			@Override
			public LowestCommonAncestorDynamic.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link LowestCommonAncestorDynamic} objects.
	 *
	 * @see    LowestCommonAncestorDynamic#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<LowestCommonAncestorDynamic.Builder> {

		/**
		 * Create a new dynamic LCA algorithm.
		 *
		 * @return a new dynamic LCA algorithm
		 */
		LowestCommonAncestorDynamic build();
	}

}
