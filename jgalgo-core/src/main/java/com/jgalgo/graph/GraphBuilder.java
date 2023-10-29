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

import java.util.Set;

/**
 * A builder for {@linkplain Graph graphs}.
 * <p>
 * The builder is used to construct <b>non-empty</b> graphs. Differing from {@link GraphFactory} which create new empty
 * graphs, the builder is used to add vertices and edges before actually creating the graph. This capability is required
 * to create immutable graphs, but can also be used to build mutable graph and may gain a performance boost compared to
 * creating an empty graph and adding the same vertices and edges.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        GraphBuilder#newUndirected()
 * @see        GraphBuilder#newDirected()
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
	 * Add a new vertex to the graph.
	 *
	 * @param vertex the new vertex
	 */
	void addVertex(V vertex);

	/**
	 * Add a new edge to the graph.
	 *
	 * @param source the source vertex of the new edge
	 * @param target the target vertex of the new edge
	 * @param edge   the new edge
	 */
	void addEdge(V source, V target, E edge);

	/**
	 * Hint about the number of vertices expected to be added to the builder.
	 * <p>
	 * This method does not affect the built graph, only the builder itself.
	 *
	 * @param verticesNum the expected number of vertices to be added to the builder
	 */
	void expectedVerticesNum(int verticesNum);

	/**
	 * Hint about the number of edges expected to be added to the builder.
	 * <p>
	 * This method does not affect the built graph, only the builder itself.
	 *
	 * @param edgesNum the expected number of edges to be added to the builder
	 */
	void expectedEdgesNum(int edgesNum);

	/**
	 * Get the vertices weights of some key.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key        key of the weights
	 * @return            vertices weights of the key, or {@code null} if no container found with the specified key
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link WeightsInt}, {@link WeightsDouble} ect.
	 */
	<T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key);

	/**
	 * Add a new weights container associated with the vertices of the built graph.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 */
	default <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type) {
		return addVerticesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the vertices of built graph with default value.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 */
	<T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type, T defVal);

	/**
	 * Get the keys of all the associated vertices weights.
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated vertices weights
	 */
	Set<String> getVerticesWeightsKeys();

	/**
	 * Get the edges weights of some key.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key        key of the weights
	 * @return            edges weights of the key, or {@code null} if no container found with the specified key
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link WeightsInt}, {@link WeightsDouble} ect.
	 */
	<T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key);

	/**
	 * Add a new weights container associated with the edges of the built graph.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 */
	default <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type) {
		return addEdgesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the edges of the built graph with default value.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link WeightsInt}, {@link WeightsDouble} ect.
	 */
	<T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal);

	/**
	 * Get the keys of all the associated edges weights.
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
	 * Build a new immutable graph with the builder vertices and edges.
	 *
	 * @return a new immutable graph with the vertices and edges that were added to the builder.
	 */
	Graph<V, E> build();

	/**
	 * Build a new mutable graph with the builder vertices and edges.
	 *
	 * @return a new mutable graph with the vertices and edges that were added to the builder.
	 */
	Graph<V, E> buildMutable();

	/**
	 * Create a new builder that builds undirected graphs.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new empty builder for undirected graphs
	 */
	static <V, E> GraphBuilder<V, E> newUndirected() {
		return new GraphBuilderImpl.Undirected<>();
	}

	/**
	 * Create a new builder that builds directed graphs.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new empty builder for directed graphs
	 */
	static <V, E> GraphBuilder<V, E> newDirected() {
		return new GraphBuilderImpl.Directed<>();
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, without copying the weights.
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     a builder initialized with the given graph vertices and edges, without the original graph
	 *             vertices/edges weights.
	 */
	static <V, E> GraphBuilder<V, E> newFrom(Graph<V, E> g) {
		return newFrom(g, false);
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, with/without copying the weights.
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  <V>         the vertices type
	 * @param  <E>         the edges type
	 * @param  g           a graph
	 * @param  copyWeights if {@code true}, the weights of the vertices and edges will be copied to the new graph
	 * @return             a builder initialized with the given graph vertices and edges, with/without the original
	 *                     graph vertices/edges weights.
	 */
	static <V, E> GraphBuilder<V, E> newFrom(Graph<V, E> g, boolean copyWeights) {
		return GraphBuilderImpl.newFrom(g, copyWeights);
	}

}
