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
package com.jgalgo.graph;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A graph whose vertices and edges identifiers are indices.
 * <p>
 * The {@link IntGraph} interface provide addition, removal and querying of vertices and edges, all using {@code int}
 * identifiers. These identifiers are fixed, and once a vertex or edge is assigned an ID, it will not change during the
 * graph lifetime. On the other hand, an <i>Index</i> graph is a {@link IntGraph} object in which the vertices and edges
 * identifiers of the graph are <b>always</b> {@code (0,1,2, ...,verticesNum-1)} and {@code (0,1,2, ...,edgesNum-1)}.
 * <p>
 * The index graph invariants allow for a great performance boost, as a simple array or bitmap can be used to associate
 * a value/weight/flag with each vertex/edge. But it does come with a cost: to maintain the invariants, implementations
 * may need to rename existing vertices or edges during the graph lifetime. These renames can be subscribed-to using
 * {@link #addVertexSwapListener(IndexSwapListener)} and {@link #addEdgeSwapListener(IndexSwapListener)}.
 * <p>
 * An index graph may be obtained as a view from a regular {@link IntGraph} using {@link IntGraph#indexGraph()}, or it
 * can be created on its own using {@link IndexGraphFactory}. In cases where no removal of vertices or edges is
 * required, and there is no need to use pre-defined IDs, there is no drawback of using the {@link IndexGraph} as a
 * regular {@link IntGraph}, as it will expose an identical functionality while providing better performance.
 * <p>
 * All graph algorithms implementations should operation on Index graphs only, for best performance. If a regular
 * {@link IntGraph} is provided to an algorithm, the Index graph should be retrieved using
 * {@link IntGraph#indexGraph()}, the algorithm expensive logic should operate on the returned Index graph and finally
 * the result should be transformed back to the regular graph IDs. The mapping from a regular graph IDs to indices and
 * vice versa is exposed using {@link IndexIntIdMap}, which can be accessed using {@link IntGraph#indexGraphVerticesMap()}
 * and {@link IntGraph#indexGraphEdgesMap()}.
 * <p>
 * To create a new empty index graph, use {@link #newUndirected()} or {@link #newDirected()}. The returned graph will
 * use the default implementation. For more control over the graph details, see {@link IndexGraphFactory}. To construct
 * an immutable index graph, use {@link IndexGraphBuilder}.
 *
 * @see    IndexIntIdMap
 * @author Barak Ugav
 */
public interface IndexGraph extends IntGraph {

	/**
	 * {@inheritDoc}
	 * <p>
	 * In an Index graph, the set of vertices are always {@code (0,1,2, ...,verticesNum-1)}.
	 */
	@Override
	IntSet vertices();

	/**
	 * {@inheritDoc}
	 * <p>
	 * In an Index graph, the set of edges are always {@code (0,1,2, ...,edgesNum-1)}.
	 */
	@Override
	IntSet edges();

	/**
	 * Unsupported operation.
	 * <p>
	 * Index graphs vertices IDs are always {@code (0,1,2, ...,verticesNum-1)} and do not support user chosen IDs.
	 *
	 * @throws UnsupportedOperationException always
	 */
	@Override
	@Deprecated
	default void addVertex(int vertex) {
		throw new UnsupportedOperationException("Index graphs do not support user chosen IDs");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing a vertex, the graph implementation may swap and rename vertices to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addVertexSwapListener(IndexSwapListener)}.
	 */
	@Override
	void removeVertex(int vertex);

	/**
	 * Unsupported operation.
	 * <p>
	 * Index graphs edges IDs are always {@code (0,1,2, ...,edgesNum-1)} and do not support user chosen IDs.
	 *
	 * @throws UnsupportedOperationException always
	 */
	@Override
	@Deprecated
	default void addEdge(int source, int target, int edge) {
		throw new UnsupportedOperationException("Index graphs do not support user chosen IDs");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeSwapListener(IndexSwapListener)}.
	 */
	@Override
	void removeEdge(int edge);

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeSwapListener(IndexSwapListener)}.
	 */
	@Override
	default void removeEdgesOf(int vertex) {
		IntGraph.super.removeEdgesOf(vertex);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeSwapListener(IndexSwapListener)}.
	 */
	@Override
	default void removeOutEdgesOf(int source) {
		IntGraph.super.removeOutEdgesOf(source);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeSwapListener(IndexSwapListener)}.
	 */
	@Override
	default void removeInEdgesOf(int target) {
		IntGraph.super.removeInEdgesOf(target);
	}

	/**
	 * Adds a listener that will be called each time a vertex swap is performed.
	 * <p>
	 * An {@link IndexGraph} may rename vertices during its lifetime to maintain the invariant that all vertices are
	 * identified by {@code 0,1,2,...,verticesNum-1}. This method can be used to track these changes, by registering a
	 * listener that will be invoked each time such a rename is performed.
	 *
	 * @param listener a swap listener that will be called each time a vertex swap is performed
	 */
	void addVertexSwapListener(IndexSwapListener listener);

	/**
	 * Removes a vertex swap listener.
	 * <p>
	 * After a listener was added using {@link #addVertexSwapListener(IndexSwapListener)}, this method removes may
	 * remove it.
	 *
	 * @param listener a swap listener that should be removed
	 */
	void removeVertexSwapListener(IndexSwapListener listener);

	/**
	 * Adds a listener that will be called each time a edge swap is performed.
	 * <p>
	 * An {@link IndexGraph} may rename edges during its lifetime to maintain the invariant that all edges are
	 * identified by {@code 0,1,2,...,edgesNum-1}. This method can be used to track these changes, by registering a
	 * listener that will be invoked each time such a rename is performed.
	 *
	 * @param listener a swap listener that will be called each time a edge swap is performed
	 */
	void addEdgeSwapListener(IndexSwapListener listener);

	/**
	 * Removes an edge swap listener.
	 * <p>
	 * After a listener was added using {@link #addEdgeSwapListener(IndexSwapListener)}, this method removes may remove
	 * it.
	 *
	 * @param listener a swap listener that should be removed
	 */
	void removeEdgeSwapListener(IndexSwapListener listener);

	/**
	 * The index graph of an {@link IndexGraph} is itself.
	 *
	 * @return     this graph
	 * @deprecated this function will always return the same graph, no reason to call it
	 */
	@Override
	@Deprecated
	default IndexGraph indexGraph() {
		return this;
	}

	/**
	 * The IDs and indices of an {@link IndexGraph} are the same.
	 *
	 * @deprecated this function will always return the identity mapping, no reason to call it
	 */
	@Override
	@Deprecated
	default IndexIntIdMap indexGraphVerticesMap() {
		return Graphs.IndexIdMapIdentify;
	}

	/**
	 * The IDs and indices of an {@link IndexGraph} are the same.
	 *
	 * @deprecated this function will always return the identity mapping, no reason to call it
	 */
	@Override
	@Deprecated
	default IndexIntIdMap indexGraphEdgesMap() {
		return Graphs.IndexIdMapIdentify;
	}

	@Override
	default IndexGraph copy() {
		return IndexGraphFactory.newFrom(this).newCopyOf(this);
	}

	@Override
	default IndexGraph copy(boolean copyWeights) {
		return IndexGraphFactory.newFrom(this).newCopyOf(this, copyWeights);
	}

	@Override
	default IndexGraph immutableCopy() {
		return immutableCopy(false);
	}

	@Override
	default IndexGraph immutableCopy(boolean copyWeights) {
		if (isDirected()) {
			return new GraphCSRDirected(this, copyWeights);
		} else {
			return new GraphCSRUndirected(this, copyWeights);
		}
	}

	@Override
	default IndexGraph immutableView() {
		return Graphs.immutableView(this);
	}

	@Override
	default IndexGraph reverseView() {
		return Graphs.reverseView(this);
	}

	/**
	 * Create a new undirected empty index graph.
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link IndexGraphFactory}.
	 *
	 * @return a new undirected empty index graph
	 */
	static IndexGraph newUndirected() {
		return IndexGraphFactory.newUndirected().newGraph();
	}

	/**
	 * Create a new directed empty index graph.
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link IndexGraphFactory}.
	 *
	 * @return a new directed empty index graph
	 */
	static IndexGraph newDirected() {
		return IndexGraphFactory.newDirected().newGraph();
	}

}
