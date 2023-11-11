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

package com.jgalgo.alg;

/**
 * Dynamic algorithm for Lowest Common Ancestor (LCA) queries.
 *
 * <p>
 * The lowest common ancestor of two vertices in a tree is the vertex that appear in both vertices paths to the root
 * (common ancestor), and its farthest from the root (lowest). Algorithm implementing this interface support modifying
 * the tree by adding leafs as children to existing parents vertices, while supporting LCA queries.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
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
	 * This is the recommended way to instantiate a new {@link LowestCommonAncestorDynamic} object. The
	 * {@link LowestCommonAncestorDynamic.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link LowestCommonAncestorDynamic}
	 */
	static LowestCommonAncestorDynamic newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new dynamic LCA algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
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
						case "gabow-simple":
							return new LowestCommonAncestorDynamicGabowSimple();
						case "gabow-ints":
							return new LowestCommonAncestorDynamicGabowInts();
						case "gabow-longs":
							return new LowestCommonAncestorDynamicGabowLongs();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new LowestCommonAncestorDynamicGabowLongs();
			}

			@Override
			public LowestCommonAncestorDynamic.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						LowestCommonAncestorDynamic.Builder.super.setOption(key, value);
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
	static interface Builder {

		/**
		 * Create a new dynamic LCA algorithm.
		 *
		 * @return a new dynamic LCA algorithm
		 */
		LowestCommonAncestorDynamic build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 *
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 *
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default LowestCommonAncestorDynamic.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
