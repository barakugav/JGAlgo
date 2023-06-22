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
 * The {@link Graph} interface provide addition, removal and querying of vertices and edges, all using {@code int}
 * identifiers. These identifiers are fixed, and once a vertex or edge is assigned an ID, it will not change during the
 * graph lifetime. On the other hand, an <i>Index</i> graph is a {@link Graph} object in which the vertices and edges
 * identifiers of the graph are <b>always</b> {@code (0,1,2, ...,verticesNum-1)} and {@code (0,1,2, ...,edgesNum-1)}.
 * <p>
 * The index graph invariants allow for a great performance boost, as a simple array or bitmap can be used to associate
 * a value/weight/flag with each vertex/edge. But it does come with a cost: to maintain the invariants, implementations
 * may need to rename existing vertices or edges during the graph lifetime. These renames can be subscribed-to using
 * {@link #addVertexSwapListener(IndexSwapListener)} and {@link #addEdgeSwapListener(IndexSwapListener)}.
 * <p>
 * An index graph may be obtained as a view from a regular {@link Graph} using {@link Graph#indexGraph()}, or it can be
 * created on its own using {@link IndexGraph.Builder}. In cases where no removal of vertices or edges is required, and
 * there is no need to use pre-defined IDs, there is no drawback of using the {@link IndexGraph} as a regular
 * {@link Graph}, as it will expose an identical functionality while providing better performance.
 * <p>
 * All graph algorithms implementations should operation on Index graphs only, for best performance. If a regular
 * {@link Graph} is provided to an algorithm, the Index graph should be retrieved using {@link Graph#indexGraph()}, the
 * algorithm expensive logic should operate on the returned Index graph and finally the result should be transformed
 * back to the regular graph IDs. The mapping from a regular graph IDs to indices and visa versa is exposed using
 * {@link IndexIdMap}, which can be accessed using {@link Graph#indexGraphVerticesMap()} and
 * {@link Graph#indexGraphEdgesMap()}.
 *
 * @see    IndexIdMap
 * @author Barak Ugav
 */
public interface IndexGraph extends Graph {

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
		Graph.super.removeEdgesOf(vertex);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeSwapListener(IndexSwapListener)}.
	 */
	@Override
	default void removeOutEdgesOf(int source) {
		Graph.super.removeOutEdgesOf(source);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeSwapListener(IndexSwapListener)}.
	 */
	@Override
	default void removeInEdgesOf(int target) {
		Graph.super.removeInEdgesOf(target);
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
	default IndexIdMap indexGraphVerticesMap() {
		return Graphs.IndexIdMapIdentify;
	}

	/**
	 * The IDs and indices of an {@link IndexGraph} are the same.
	 *
	 * @deprecated this function will always return the identity mapping, no reason to call it
	 */
	@Override
	@Deprecated
	default IndexIdMap indexGraphEdgesMap() {
		return Graphs.IndexIdMapIdentify;
	}

	@Override
	default IndexGraph copy() {
		return newBuilderFrom(this).buildCopyOf(this);
	}

	// @Override
	// default IndexGraph fixedCopy() {
	// if (getCapabilities().directed()) {
	// return new GraphCSRUnmappedDirected(this);
	// } else {
	// return new GraphCSRUnmappedUndirected(this);
	// }
	// }

	@Override
	default IndexGraph unmodifiableView() {
		return Graphs.unmodifiableView(this);
	}

	@Override
	default IndexGraph reverseView() {
		return Graphs.reverseView(this);
	}

	/**
	 * Create an undirected index graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new undirected index graph.
	 *
	 * @return a new builder that can build undirected index graphs
	 */
	static IndexGraph.Builder newBuilderUndirected() {
		return new IndexGraphBuilderImpl(false);
	}

	/**
	 * Create a directed index graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new directed index graph.
	 *
	 * @return a new builder that can build directed index graphs
	 */
	static IndexGraph.Builder newBuilderDirected() {
		return new IndexGraphBuilderImpl(true);
	}

	/**
	 * Create a new index graph builder based on a given implementation.
	 * <p>
	 * The new builder will build graphs with the same capabilities as the given graph, possibly choosing to use a
	 * similar implementation. The builder will NOT copy the graph itself (the vertices, edges and weights), for such
	 * use case see {@link IndexGraph#copy()} and {@link IndexGraph.Builder#buildCopyOf(IndexGraph)}.
	 *
	 * @param  g a graph from which the builder should copy its capabilities
	 * @return   a new graph builder that will create graphs with the same capabilities of the given graph
	 */
	static IndexGraph.Builder newBuilderFrom(IndexGraph g) {
		return new IndexGraphBuilderImpl(g);
	}

	/**
	 * A builder for {@link IndexGraph} objects.
	 *
	 * @see    IndexGraph#newBuilderDirected()
	 * @see    IndexGraph#newBuilderUndirected()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<IndexGraph.Builder> {

		/**
		 * Create a new empty index graph.
		 *
		 * @return a new index graph with the builder options
		 */
		IndexGraph build();

		/**
		 * Create a copy of a given index graph.
		 * <p>
		 * An identical copy of the given graph will be created, with the same vertices, edges and weights. The returned
		 * Graph will always be modifiable, with no side affects on the original graph.
		 * <p>
		 * Differing from {@link IndexGraph#copy()}, the capabilities of the new graph are determined by the builder
		 * configuration, rather than copied from the given graph. Note for example that if the builder chooses to use
		 * an implementation that does not (have to) support self edges (if {@link #allowSelfEdges(boolean)} was not
		 * called with {@code true}), attempting to create a copy of a graph that does contains self edges will result
		 * in an exception.
		 *
		 * @param  g the original graph to copy
		 * @return   an identical copy of the given graph
		 */
		IndexGraph buildCopyOf(IndexGraph g);

		/**
		 * Determine if graphs built by this builder should be directed or not.
		 *
		 * @param  directed if {@code true}, graphs built by this builder will be directed
		 * @return          this builder
		 */
		IndexGraph.Builder setDirected(boolean directed);

		/**
		 * Determine if graphs built by this builder should be support self edges.
		 *
		 * @param  selfEdges if {@code true}, graphs built by this builder will support self edges
		 * @return           this builder
		 */
		IndexGraph.Builder allowSelfEdges(boolean selfEdges);

		/**
		 * Determine if graphs built by this builder should be support parallel edges.
		 *
		 * @param  parallelEdges if {@code true}, graphs built by this builder will support parallel edges
		 * @return               this builder
		 */
		IndexGraph.Builder allowParallelEdges(boolean parallelEdges);

		/**
		 * Set the expected number of vertices that will exist in the graph.
		 *
		 * @param  expectedVerticesNum the expected number of vertices in the graph
		 * @return                     this builder
		 */
		IndexGraph.Builder expectedVerticesNum(int expectedVerticesNum);

		/**
		 * Set the expected number of edges that will exist in the graph.
		 *
		 * @param  expectedEdgesNum the expected number of edges in the graph
		 * @return                  this builder
		 */
		IndexGraph.Builder expectedEdgesNum(int expectedEdgesNum);

		/**
		 * Add a hint to this builder.
		 * <p>
		 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
		 *
		 * @param  hint the hint to add
		 * @return      this builder
		 */
		IndexGraph.Builder addHint(Graph.Builder.Hint hint);

		/**
		 * Remove a hint from this builder.
		 * <p>
		 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
		 *
		 * @param  hint the hint to remove
		 * @return      this builder
		 */
		IndexGraph.Builder removeHint(Graph.Builder.Hint hint);

	}

}
