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

/**
 * Dynamic tree data structure that support {@code link} and {@code cut} operations.
 * <p>
 * The dynamic tree data structure is a set of nodes forming a forest and support the following operations:
 * <ul>
 * <li>{@link #makeTree()} - create a new node which will form a tree of size one.</li>
 * <li>{@link #findRoot(Node)} - find the root node of a given node.</li>
 * <li>{@link #link(Node, Node, double)} - link a node (root) to a parent with weighted edge.</li>
 * <li>{@link #cut(Node)} - remove the edge from a node to its parent.</li>
 * <li>{@link #addWeight(Node, double)} - add a weight to all edges on the path from a node to its tree root.</li>
 * <li>{@link #findMinEdge(Node)} - find the edge with minimum weight from on the path from a node to its tree
 * root.</li>
 * </ul>
 * <p>
 * Note: this API will change in the future
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Link/cut_tree">Wikipedia</a>
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
	 * Find the root of the tree containing \(v\).
	 *
	 * @param  v a node
	 * @return   the root of the tree containing \(v\)
	 */
	public Node findRoot(Node v);

	/**
	 * Find the minimum edge on the path from a node to it's tree root.
	 *
	 * @param  v a node
	 * @return   the minimum edge from \(v\) to it's tree root, or {@code null} if no such edge exists
	 */
	public MinEdge findMinEdge(Node v);

	/**
	 * Add a weight to all of the edges from \(v\) to it's tree root.
	 *
	 * @param v a node
	 * @param w a weight to add
	 */
	public void addWeight(Node v, double w);

	/**
	 * Link a root to be a child of some other node of another tree.
	 *
	 * @param  child                    a root of some tree
	 * @param  parent                   a node in another tree
	 * @param  w                        the new edge weight
	 * @throws IllegalArgumentException if {@code child} is not a root or if {@code child} and {@code root} are in the
	 *                                      same tree.
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
		 * @param  <V> the data type
		 * @return     the user data or {@code null} if it was not set
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
	 * A return type for {@link DynamicTree#findMinEdge(Node)} method representing the minimum edge from a node to its
	 * tree root.
	 *
	 * @author Barak Ugav
	 */
	public static interface MinEdge {

		/**
		 * Get the source of this edge.
		 * <p>
		 * The source was determined as the child node during the creation of the node via
		 * {@link DynamicTree#link(Node, Node, double)} operation.
		 *
		 * @return the edge source.
		 */
		public Node source();

		/**
		 * Get the weight of the edge.
		 * <p>
		 * The weight of the edge is a sum over the initial weight assigned during the
		 * {@link DynamicTree#link(Node, Node, double)} operation and all {@link DynamicTree#addWeight(Node, double)}
		 * operations that affected this edge.
		 *
		 * @return the weight of the edge.
		 */
		public double weight();

	}

	/**
	 * Get an extension supported by this dynamic tree.
	 * <p>
	 * Different extensions are supported in addition to the regular {@link DynamicTree} interface. The extensions do
	 * not change the asymptotical running time of the implementation. Here is an example of a
	 * {@link DynamicTreeExtension.TreeSize} extension use.
	 *
	 * <pre> {@code
	 * DynamicTree.Builder builder = DynamicTree.newBuilder();
	 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
	 *
	 * DynamicTree dt = builder.build();
	 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
	 * ...
	 * DynamicTree.Node n1 = dt.makeTree();
	 * DynamicTree.Node n2 = dt.makeTree();
	 *
	 * System.out.println("The number of nodes in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
	 * }</pre>
	 *
	 * @param  <Ext>         the extension type
	 * @param  extensionType the extension type class
	 * @return               the extension object or {@code null} if no matching extension was found
	 * @see                  DynamicTreeExtension
	 * @see                  DynamicTree.Builder#addExtension(Class)
	 */
	public <Ext extends DynamicTreeExtension> Ext getExtension(Class<Ext> extensionType);

	/**
	 * Create a new dynamic trees algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link DynamicTree} object.
	 *
	 * @return a new builder that can build {@link DynamicTree} objects
	 */
	static DynamicTree.Builder newBuilder() {
		return new DynamicTreeBuilderImpl();
	}

	/**
	 * A builder for {@link DynamicTree} objects.
	 *
	 * @see    DynamicTree#newBuilder()
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
		 * <p>
		 * Some implementations required this value to be set before building {@link DynamicTree} instances.
		 *
		 * @param  maxWeight a limit on the weights of the edges. The limit is an upper bound on the sum of each edge
		 *                       weight and the weights modification that are performed using
		 *                       {@link #addWeight(com.jgalgo.DynamicTree.Node, double)}.
		 * @return           this builder
		 */
		DynamicTree.Builder setMaxWeight(double maxWeight);

		/**
		 * Enable/disable integer weights.
		 * <p>
		 * More efficient and accurate implementations may be supported if the weights are known to be integers.
		 *
		 * @param  enable if {@code true}, the built {@link DynamicTree} objects will support only integer weights
		 * @return        this builder
		 */
		DynamicTree.Builder setIntWeights(boolean enable);

		/**
		 * Add an extension to all trees built by this builder.
		 * <p>
		 * For example, this is the recommended way to create a dynamic tree data structure with tree size extension:
		 *
		 * <pre> {@code
		 * DynamicTree.Builder builder = DynamicTree.newBuilder();
		 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
		 *
		 * DynamicTree dt = builder.build();
		 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
		 * ...
		 * DynamicTree.Node n1 = dt.makeTree();
		 * DynamicTree.Node n2 = dt.makeTree();
		 *
		 * System.out.println("The number of nodes in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
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
	}

}
