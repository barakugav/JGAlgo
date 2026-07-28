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

package com.jgalgo.internal.ds;

/**
 * Dynamic tree data structure that support {@code link} and {@code cut} operations.
 *
 * <p>
 * The dynamic tree data structure is a set of vertices forming a forest and support the following operations:
 * <ul>
 * <li>{@link #makeTree()} - create a new vertex which will form a tree of size one.</li>
 * <li>{@link #findRoot(Vertex)} - find the root vertex of a given vertex.</li>
 * <li>{@link #link(Vertex, Vertex, double)} - link a vertex (root) to a parent with weighted edge.</li>
 * <li>{@link #cut(Vertex)} - remove the edge from a vertex to its parent.</li>
 * <li>{@link #addWeight(Vertex, double)} - add a weight to all edges on the path from a vertex to its tree root.</li>
 * <li>{@link #findMinEdge(Vertex)} - find the edge with minimum weight from on the path from a vertex to its tree
 * root.</li>
 * </ul>
 *
 * <p>
 * Note: this API will change in the future
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Link/cut_tree">Wikipedia</a>
 * @author Barak Ugav
 */
public interface DynamicTree {

	/**
	 * Create a new tree in the forest with a single vertex.
	 *
	 * @return the new vertex
	 */
	Vertex makeTree();

	/**
	 * Find the root of the tree containing \(v\).
	 *
	 * @param  v a vertex
	 * @return   the root of the tree containing \(v\)
	 */
	Vertex findRoot(Vertex v);

	/**
	 * Find the minimum edge on the path from a vertex to it's tree root.
	 *
	 * @param  v a vertex
	 * @return   the minimum edge from \(v\) to it's tree root, or {@code null} if no such edge exists
	 */
	MinEdge findMinEdge(Vertex v);

	/**
	 * Add a weight to all of the edges from \(v\) to it's tree root.
	 *
	 * @param v a vertex
	 * @param w a weight to add
	 */
	void addWeight(Vertex v, double w);

	/**
	 * Link a root to be a child of some other vertex of another tree.
	 *
	 * @param  child                    a root of some tree
	 * @param  parent                   a vertex in another tree
	 * @param  w                        the new edge weight
	 * @throws IllegalArgumentException if {@code child} is not a root or if {@code child} and {@code root} are in the
	 *                                      same tree.
	 */
	void link(Vertex child, Vertex parent, double w);

	/**
	 * Remove the edge from a vertex to it's parent.
	 *
	 * @param v a vertex
	 */
	void cut(Vertex v);

	/**
	 * Clear the whole data structure.
	 */
	void clear();

	/**
	 * A vertex in the forest of {@link DynamicTree} data structure.
	 *
	 * @author Barak Ugav
	 */
	static interface Vertex {

		/**
		 * Get the user data of this vertex.
		 *
		 * @param  <V> the data type
		 * @return     the user data or {@code null} if it was not set
		 */
		<V> V getData();

		/**
		 * Set the user data of this vertex.
		 *
		 * @param data new data for the vertex
		 */
		void setData(Object data);

		/**
		 * Get the parent vertex of this vertex.
		 *
		 * @return the parent vertex of this vertex
		 */
		Vertex getParent();

	}

	/**
	 * A return type for {@link DynamicTree#findMinEdge(Vertex)} method representing the minimum edge from a vertex to
	 * its tree root.
	 *
	 * @author Barak Ugav
	 */
	static interface MinEdge {

		/**
		 * Get the source of this edge.
		 *
		 * <p>
		 * The source was determined as the child vertex during the creation of the vertex via
		 * {@link DynamicTree#link(Vertex, Vertex, double)} operation.
		 *
		 * @return the edge source.
		 */
		Vertex source();

		/**
		 * Get the weight of the edge.
		 *
		 * <p>
		 * The weight of the edge is a sum over the initial weight assigned during the
		 * {@link DynamicTree#link(Vertex, Vertex, double)} operation and all
		 * {@link DynamicTree#addWeight(Vertex, double)} operations that affected this edge.
		 *
		 * @return the weight of the edge.
		 */
		double weight();

	}

	/**
	 * Get an extension supported by this dynamic tree.
	 *
	 * <p>
	 * Different extensions are supported in addition to the regular {@link DynamicTree} interface. The extensions do
	 * not change the asymptotical running time of the implementation. Here is an example of a
	 * {@link DynamicTreeExtension.TreeSize} extension use.
	 *
	 * <pre> {@code
	 * DynamicTree.Builder builder = DynamicTree.builder();
	 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
	 *
	 * DynamicTree dt = builder.build();
	 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
	 * ...
	 * DynamicTree.Vertex n1 = dt.makeTree();
	 * DynamicTree.Vertex n2 = dt.makeTree();
	 *
	 * System.out.println("The number of vertices in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
	 * }</pre>
	 *
	 * @param  <ExtT>        the extension type
	 * @param  extensionType the extension type class
	 * @return               the extension object or {@code null} if no matching extension was found
	 * @see                  DynamicTreeExtension
	 * @see                  DynamicTree.Builder#addExtension(Class)
	 */
	<ExtT extends DynamicTreeExtension> ExtT getExtension(Class<ExtT> extensionType);

	/**
	 * Create a new dynamic trees algorithm builder.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link DynamicTree} object.
	 *
	 * @return a new builder that can build {@link DynamicTree} objects
	 */
	static DynamicTree.Builder builder() {
		return new DynamicTreeBuilderImpl();
	}

	/**
	 * A builder for {@link DynamicTree} objects.
	 *
	 * @see    DynamicTree#builder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new dynamic trees algorithm.
		 *
		 * @return a new dynamic trees algorithm
		 */
		DynamicTree build();

		/**
		 * Set the maximum edge weight the dynamic trees should support.
		 *
		 * <p>
		 * Some implementations required this value to be set before building {@link DynamicTree} instances.
		 *
		 * @param  maxWeight a limit on the weights of the edges. The limit is an upper bound on the sum of each edge
		 *                       weight and the weights modification that are performed using
		 *                       {@link #addWeight(com.jgalgo.internal.ds.DynamicTree.Vertex, double)}.
		 * @return           this builder
		 */
		DynamicTree.Builder setMaxWeight(double maxWeight);

		/**
		 * Enable/disable integer weights.
		 *
		 * <p>
		 * More efficient and accurate implementations may be supported if the weights are known to be integers.
		 *
		 * @param  enable if {@code true}, the built {@link DynamicTree} objects will support only integer weights
		 * @return        this builder
		 */
		DynamicTree.Builder setIntWeights(boolean enable);

		/**
		 * Add an extension to all trees built by this builder.
		 *
		 * <p>
		 * For example, this is the recommended way to create a dynamic tree data structure with tree size extension:
		 *
		 * <pre> {@code
		 * DynamicTree.Builder builder = DynamicTree.builder();
		 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
		 *
		 * DynamicTree dt = builder.build();
		 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
		 * ...
		 * DynamicTree.Vertex n1 = dt.makeTree();
		 * DynamicTree.Vertex n2 = dt.makeTree();
		 *
		 * System.out.println("The number of vertices in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
		 * }</pre>
		 *
		 * @param  extensionType the extension type
		 * @return               this builder
		 * @see                  DynamicTree#getExtension(Class)
		 */
		DynamicTree.Builder addExtension(Class<? extends DynamicTreeExtension> extensionType);

		/**
		 * Remove an extension that was added using {@link #addExtension(Class)}.
		 *
		 * @param  extensionType the extension type
		 * @return               this builder
		 */
		DynamicTree.Builder removeExtension(Class<? extends DynamicTreeExtension> extensionType);

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
		default DynamicTree.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
