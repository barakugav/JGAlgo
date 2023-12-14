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

import java.util.Collection;
import java.util.Set;

/**
 * A builder for {@linkplain Graph graphs}.
 *
 * <p>
 * The builder is used to construct <b>non-empty</b> graphs. Differing from {@link GraphFactory} which create new empty
 * graphs, the builder is used to add vertices and edges before actually creating the graph. This capability is required
 * to create immutable graphs, but can also be used to build mutable graph and may gain a performance boost compared to
 * creating an empty graph and adding the same vertices and edges.
 *
 * <p>
 * To create a new builder, use one of the static methods {@link #undirected()}, {@link #directed()} or
 * {@link #newInstance(boolean)}. For more options, create a new {@link GraphFactory} and use
 * {@link GraphFactory#newBuilder()}, or use {@link GraphFactory#newBuilderCopyOf(Graph)} to create a builder
 * initialized with an existing graph vertices and edges.
 *
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        GraphBuilder#undirected()
 * @see        GraphBuilder#directed()
 * @see        GraphFactory
 * @author     Barak Ugav
 */
public interface GraphBuilder<V, E> {

	/**
	 * Get the set of vertices that were added to the graph.
	 *
	 * @return the graph vertices
	 */
	Set<V> vertices();

	/**
	 * Get the set of edges that were added to the graph.
	 *
	 * @return the graph edges
	 */
	Set<E> edges();

	/**
	 * Add a new vertex to the built graph.
	 *
	 * <p>
	 * A vertex can be any non null hashable object, namely it must implement the {@link Object#hashCode()} and
	 * {@link Object#equals(Object)} methods. Duplicate vertices are not allowed.
	 *
	 * @param  vertex                   the new vertex
	 * @throws IllegalArgumentException if {@code vertex} is already in the built graph
	 */
	void addVertex(V vertex);

	/**
	 * Add multiple vertices to the built graph.
	 *
	 * <p>
	 * A vertex can be any non null hashable object, namely it must implement the {@link Object#hashCode()} and
	 * {@link Object#equals(Object)} methods. Duplicate vertices are not allowed.
	 *
	 * @param  vertices                 new vertices
	 * @throws IllegalArgumentException if {@code vertices} contains duplications or if any of the vertices is already
	 *                                      in the built graph
	 * @throws NullPointerException     if {@code vertices} is {@code null} or if any of the vertices is {@code null}
	 */
	void addVertices(Collection<? extends V> vertices);

	/**
	 * Add a new edge to the graph.
	 *
	 * <p>
	 * If the graph does not support self or parallel edges and the added edge is such edge, an exception will
	 * <b>not</b> be thrown. The edges are validated only when the graph is built, and an exception will be thrown only
	 * then.
	 *
	 * @param  source                   the source vertex of the new edge
	 * @param  target                   the target vertex of the new edge
	 * @param  edge                     the new edge
	 * @throws IllegalArgumentException if {@code edge} is already in the graph
	 * @throws NullPointerException     if {@code edge} is {@code null}, as {@code null} identifiers are not allowed
	 * @throws NoSuchVertexException    if {@code source} or {@code target} are not vertices in the graph
	 */
	void addEdge(V source, V target, E edge);

	/**
	 * Add multiple edges to the built graph.
	 *
	 * <p>
	 * The {@link EdgeSet} passed to this method contains both the edges themselves (the identifiers) and their
	 * endpoints (sources and targets), see {@link EdgeSet#iterator()}, {@link EdgeIter#source()},
	 * {@link EdgeIter#target()}. An {@link EdgeSet} can be obtained from one of the methods of a {@link Graph}, or
	 * using {@link EdgeSet#of(Set, Graph)}.
	 *
	 * <p>
	 * An edge can be any non null hashable object, namely it must implement the {@link Object#hashCode()} and
	 * {@link Object#equals(Object)} methods. Duplicate edges are not allowed.
	 *
	 * <p>
	 * In the following snippet, a maximum cardinality matching is computed on a graph, and a new graph containing only
	 * the matching edges is created:
	 *
	 * <pre> {@code
	 * Graph<V, E> g = ...;
	 * Set<E> matching = MatchingAlgo.newInstance().computeMaximumMatching(g, null).edges();
	 *
	 * GraphBuilder<V,E> matchingGraphBuilder = GraphBuilder.undirected();
	 * matchingGraphBuilder.addVertices(g.vertices());
	 * matchingGraphBuilder.addEdges(EdgeSet.of(matching, g));
	 * Graph<V,E> matchingGraph = matchingGraphBuilder.build()
	 * }</pre>
	 *
	 * @param  edges                    the set of new edges, from which the edges identifiers as well as the endpoints
	 *                                      (source and target) of each edge are accessible (see
	 *                                      {@link EdgeSet#iterator()}, {@link EdgeIter#source()},
	 *                                      {@link EdgeIter#target()}).
	 * @throws IllegalArgumentException if {@code edges} contains duplications, or if any of the edges is already in the
	 *                                      graph
	 * @throws NullPointerException     if {@code edges} is {@code null} or if any of the edges is {@code null}
	 * @throws NoSuchVertexException    if any of the edges endpoint is not a vertex in the graph
	 */
	void addEdges(EdgeSet<? extends V, ? extends E> edges);

	/**
	 * Hint the implementation to allocate space for at least {@code vertexCapacity} vertices.
	 *
	 * <p>
	 * The implementation may ignore any calls to this function.
	 *
	 * @param vertexCapacity the minimum number of vertices to allocate space for
	 */
	void ensureVertexCapacity(int vertexCapacity);

	/**
	 * Hint the implementation to allocate space for at least {@code edgeCapacity} edges.
	 *
	 * <p>
	 * The implementation may ignore any calls to this function.
	 *
	 * @param edgeCapacity the minimum number of edges to allocate space for
	 */
	void ensureEdgeCapacity(int edgeCapacity);

	/**
	 * Get the vertices weights of some key.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  key        key of the weights
	 * @return            vertices weights of the key, or {@code null} if no container found with the specified key
	 */
	<T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key);

	/**
	 * Add a new weights container associated with the vertices of the built graph.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 */
	default <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type) {
		return addVerticesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the vertices of built graph with default value.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 */
	<T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type, T defVal);

	/**
	 * Get the keys of all the associated vertices weights.
	 *
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated vertices weights
	 */
	Set<String> getVerticesWeightsKeys();

	/**
	 * Get the edges weights of some key.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  key        key of the weights
	 * @return            edges weights of the key, or {@code null} if no container found with the specified key
	 */
	<T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key);

	/**
	 * Add a new weights container associated with the edges of the built graph.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 */
	default <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type) {
		return addEdgesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the edges of the built graph with default value.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 */
	<T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal);

	/**
	 * Get the keys of all the associated edges weights.
	 *
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated edges weights
	 */
	Set<String> getEdgesWeightsKeys();

	/**
	 * Clear the builder by removing all vertices and edges added to it.
	 */
	void clear();

	/**
	 * Check if the graph built by this builder is directed or undirected.
	 *
	 * @return {@code true} if the graph built by this builder is directed, {@code false} if it is undirected
	 */
	boolean isDirected();

	/**
	 * Build a new immutable graph with the builder vertices and edges.
	 *
	 * <p>
	 * Before the graph is built, the edges are validated. If the graph does not support self or parallel edges and such
	 * edges were added to the builder, an exception will be thrown.
	 *
	 * @return                          a new immutable graph with the vertices and edges that were added to the
	 *                                  builder.
	 * @throws IllegalArgumentException if the built graph does not support self or parallel edges and such edges were
	 *                                      added to the builder
	 */
	Graph<V, E> build();

	/**
	 * Build a new mutable graph with the builder vertices and edges.
	 *
	 * <p>
	 * Before the graph is built, the edges are validated. If the graph does not support self or parallel edges and such
	 * edges were added to the builder, an exception will be thrown.
	 *
	 * @return                          a new mutable graph with the vertices and edges that were added to the builder.
	 * @throws IllegalArgumentException if the built graph does not support self or parallel edges and such edges were
	 *                                      added to the builder
	 */
	Graph<V, E> buildMutable();

	/**
	 * Create a new builder that builds undirected graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link GraphFactory}, namely they
	 * will not support self edges and will support parallel edges. See the factory documentation for more information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link GraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new empty builder for undirected graphs
	 */
	static <V, E> GraphBuilder<V, E> undirected() {
		return GraphFactory.<V, E>undirected().newBuilder();
	}

	/**
	 * Create a new builder that builds directed graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link GraphFactory}, namely they
	 * will not support self edges and will support parallel edges. See the factory documentation for more information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link GraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new empty builder for directed graphs
	 */
	static <V, E> GraphBuilder<V, E> directed() {
		return GraphFactory.<V, E>directed().newBuilder();
	}

	/**
	 * Create a new builder that builds un/directed graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link GraphFactory}, namely they
	 * will not support self edges and will support parallel edges. See the factory documentation for more information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link GraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  directed if {@code true}, the new builder will build directed graphs, otherwise it will build undirected
	 *                      graphs
	 * @return          a new empty builder for un/directed graphs
	 */
	static <V, E> GraphBuilder<V, E> newInstance(boolean directed) {
		return GraphFactory.<V, E>newInstance(directed).newBuilder();
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link GraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     a builder initialized with the given graph vertices and edges, without the original graph
	 *             vertices/edges weights.
	 */
	static <V, E> GraphBuilder<V, E> newCopyOf(Graph<V, E> g) {
		return newCopyOf(g, false, false);
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, with/without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link GraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  <V>                 the vertices type
	 * @param  <E>                 the edges type
	 * @param  g                   a graph
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied from the graph to the
	 *                                 builder
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied from the graph to the
	 *                                 builder
	 * @return                     a builder initialized with the given graph vertices and edges, with/without the
	 *                             original graph vertices/edges weights.
	 */
	static <V, E> GraphBuilder<V, E> newCopyOf(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return GraphFactory.<V, E>newInstance(g.isDirected()).newBuilderCopyOf(g, copyVerticesWeights,
				copyEdgesWeights);
	}

}
