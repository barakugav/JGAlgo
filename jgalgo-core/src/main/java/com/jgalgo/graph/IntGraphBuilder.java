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
 * A builder for {@linkplain IntGraph int graphs}.
 *
 * <p>
 * The builder is used to construct <b>non-empty</b> int graphs. Differing from {@link IntGraphFactory} which create new
 * empty graphs, the builder is used to add vertices and edges before actually creating the graph. This capability is
 * required to create immutable graphs, but can also be used to build mutable graph and may gain a performance boost
 * compared to creating an empty graph and adding the same vertices and edges.
 *
 * <p>
 * This interface is a specific version of {@link GraphBuilder} for {@link IntGraph}.
 *
 * @see    IntGraphBuilder#newUndirected()
 * @see    IntGraphBuilder#newDirected()
 * @see    IndexGraphBuilder
 * @see    IntGraphFactory
 * @author Barak Ugav
 */
public interface IntGraphBuilder extends GraphBuilder<Integer, Integer> {

	@Override
	IntSet vertices();

	@Override
	IntSet edges();

	/**
	 * Add a new vertex to the graph.
	 *
	 * <p>
	 * The builder will choose identifier not used for any existing vertex, and will return it. It is also possible to
	 * add a new vertex and choose its identifier by using {@link #addVertex(int)}. Only one of {@link #addVertex()} and
	 * {@link #addVertex(int)} can be used during the construction of a graph.
	 *
	 * @return the new vertex identifier
	 */
	int addVertex();

	/**
	 * Add a new vertex to the graph, with user-chosen identifier.
	 *
	 * <p>
	 * This function is similar to {@link #addVertex()}, but let the user to choose the the identifier of the new
	 * vertex. Only one of {@link #addVertex()} and {@link #addVertex(int)} can be used during the construction of a
	 * graph.
	 *
	 * @param vertex the new vertex identifier
	 */
	void addVertex(int vertex);

	@Deprecated
	@Override
	default void addVertex(Integer vertex) {
		addVertex(vertex.intValue());
	}

	/**
	 * Add a new edge to the graph.
	 *
	 * <p>
	 * The builder will choose identifier not used for any existing edge, and will return it. It is also possible to add
	 * a new edge and choose its identifier by using {@link #addEdge(int, int, int)}. Only one of
	 * {@link #addEdge(int, int)} and {@link #addEdge(int, int, int)} can be used during the construction of a graph.
	 *
	 * @param  source the source vertex of the new edge
	 * @param  target the target vertex of the new edge
	 * @return        the new edge identifier
	 */
	int addEdge(int source, int target);

	/**
	 * Add a new edge to the graph, with user-chosen identifier.
	 *
	 * <p>
	 * This function is similar to {@link #addEdge(int, int)}, but let the user to choose the identifier of the new
	 * edge. Only one of {@link #addEdge(int, int)} and {@link #addEdge(int, int, int)} can be used during the
	 * construction of a graph.
	 *
	 * @param source the source vertex of the new edge
	 * @param target the target vertex of the new edge
	 * @param edge   the identifier of the new edge
	 */
	void addEdge(int source, int target, int edge);

	@Deprecated
	@Override
	default void addEdge(Integer source, Integer target, Integer edge) {
		addEdge(source.intValue(), target.intValue(), edge.intValue());
	}

	/**
	 * Get the vertices weights of some key.
	 *
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key        key of the weights
	 * @return            vertices weights of the key, or {@code null} if no container found with the specified key
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	<T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key);

	@Override
	default <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
		return getVerticesIWeights(key);
	}

	/**
	 * Get the edges weights of some key.
	 *
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key        key of the weights
	 * @return            edges weights of the key, or {@code null} if no container found with the specified key
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	<T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key);

	@Override
	default <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
		return getEdgesIWeights(key);
	}

	@Override
	IntGraph build();

	@Override
	IntGraph buildMutable();

	/**
	 * Create a new builder that builds undirected graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link IntGraphFactory}, namely they
	 * will not support self edges and will support parallel edges. See the factory documentation for more information.
	 *
	 * @return a new empty builder for undirected graphs
	 */
	static IntGraphBuilder newUndirected() {
		return IntGraphFactory.newUndirected().newBuilder();
	}

	/**
	 * Create a new builder that builds directed int graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link IntGraphFactory}, namely they
	 * will not support self edges and will support parallel edges. See the factory documentation for more information.
	 *
	 * @return a new empty builder for directed graphs
	 */
	static IntGraphBuilder newDirected() {
		return IntGraphFactory.newDirected().newBuilder();
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  g a graph
	 * @return   a builder initialized with the given graph vertices and edges, without the original graph
	 *           vertices/edges weights.
	 */
	static IntGraphBuilder newFrom(IntGraph g) {
		return newFrom(g, false, false);
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, with/without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  g                   a graph
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied from the graph to the
	 *                                 builder
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied from the graph to the
	 *                                 builder
	 * @return                     a builder initialized with the given graph vertices and edges, with/without the
	 *                             original graph vertices/edges weights.
	 */
	static IntGraphBuilder newFrom(IntGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return IntGraphBuilderImpl.newFrom(g, copyVerticesWeights, copyEdgesWeights);
	}

}
