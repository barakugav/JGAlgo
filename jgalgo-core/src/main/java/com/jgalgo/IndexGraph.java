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
 * may need to rename existing vertices or edges along the graph lifetime. These renames are managed by a
 * {@link IdStrategy} that can be accessed using {@link #getVerticesIdStrategy()} or {@link #getEdgesIdStrategy()} which
 * allow for a subscription to these renames via
 * {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
 * <p>
 * An index graph may be obtained as a view from a regular {@link Graph} using {@link Graph#indexGraph()}, or it can be
 * created by its own using {@link IndexGraph.Builder}. In cases where no removal of vertices or edges is required, and
 * there is no need to use per-defined IDs, there is no drawback to use the {@link IndexGraph} as regular {@link Graph},
 * as it will provide the best performance.
 * <p>
 * All graph algorithms implementations should operation on Index graphs only, for best performance. If a regular
 * {@link Graph} is provided to an algorithm, the Index graph should be retrieved using {@link Graph#indexGraph()}, the
 * algorithm expensive logic should operate on the returned Index graph and finally the result should be transformed
 * back to the regular graph IDs. The mapping from a regular graph IDs to indices and visa versa is exposed using
 * {@link IndexIdMap}, which can be accessed using {@link Graph#indexGraphVerticesMap()} and
 * {@link Graph#indexGraphEdgesMap()}.
 *
 * @see    IdStrategy
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
	 * After removing a vertex, the vertices ID strategy may rename other vertices identifiers to maintain its
	 * invariants, see {@link #getVerticesIdStrategy()}. Theses renames can be subscribed using
	 * {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
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
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIdStrategy()}. Theses renames can be subscribed using
	 * {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
	 */
	@Override
	void removeEdge(int edge);

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIdStrategy()}. Theses renames can be subscribed using
	 * {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
	 */
	@Override
	default void removeEdgesOf(int vertex) {
		Graph.super.removeEdgesOf(vertex);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIdStrategy()}. Theses renames can be subscribed using
	 * {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
	 */
	@Override
	default void removeEdgesOutOf(int source) {
		Graph.super.removeEdgesOutOf(source);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIdStrategy()}. Theses renames can be subscribed using
	 * {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
	 *
	 * @param  target                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code target} is not a valid vertex identifier
	 */
	@Override
	default void removeEdgesInOf(int target) {
		Graph.super.removeEdgesInOf(target);
	}

	/**
	 * Get the ID strategy of the vertices of the graph.
	 * <p>
	 * In an Index graph, the set of vertices are always {@code (0,1,2, ...,verticesNum-1)}. To maintain this invariant
	 * during the lifetime of the graph, an {@link IdStrategy} determine how to rename existing vertices when needed.
	 * These renames can be subscribed using {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
	 *
	 * @see    IdStrategy
	 * @return the vertices IDs strategy
	 */
	IdStrategy getVerticesIdStrategy();

	/**
	 * Get the ID strategy of the edges of the graph.
	 * <p>
	 * In an Index graph, the set of edges are always {@code (0,1,2, ...,edgesNum-1)}. To maintain this invariant during
	 * the lifetime of the graph, an {@link IdStrategy} determine how to rename existing edges when needed. These
	 * renames can be subscribed using {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
	 *
	 * @see    IdStrategy
	 * @return the edges IDs strategy
	 */
	IdStrategy getEdgesIdStrategy();

	@Override
	IndexGraph copy();

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
		 * Determine if graphs built by this builder should be directed or not.
		 *
		 * @param  directed if {@code true}, graphs built by this builder will be directed
		 * @return          this builder
		 */
		IndexGraph.Builder setDirected(boolean directed);

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
