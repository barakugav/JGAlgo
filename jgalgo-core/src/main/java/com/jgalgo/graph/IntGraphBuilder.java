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
import it.unimi.dsi.fastutil.ints.IntCollection;
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
 * To create a new builder, use one of the static methods {@link #undirected()}, {@link #directed()} or
 * {@link #newInstance(boolean)}. For more options, create a new {@link IntGraphFactory} and use
 * {@link IntGraphFactory#newBuilder()}, or use {@link IntGraphFactory#newBuilderCopyOf(Graph)} to create a builder
 * initialized with an existing graph vertices and edges.
 *
 * <p>
 * This interface is a specific version of {@link GraphBuilder} for {@link IntGraph}.
 *
 * @see    IntGraphBuilder#undirected()
 * @see    IntGraphBuilder#directed()
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
	 * Add a new vertex to the built graph.
	 *
	 * <p>
	 * Vertices must be non negative integers.
	 *
	 * <p>
	 * If there is a vertex builder, namely if {@link #vertexBuilder()} does not return {@code null}, the method
	 * {@link #addVertexInt()} can be used, which uses the vertex builder to create the new vertex object instead of
	 * requiring the user to provide it.
	 *
	 * @param  vertex                   new vertex
	 * @throws IllegalArgumentException if {@code vertex} is already in the built graph, or if {@code vertex} is
	 *                                      negative, as negative identifiers are not allowed
	 */
	void addVertex(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @throws     IllegalArgumentException if the given vertex is negative or any of reasons specified in
	 *                                          {@link GraphBuilder#addVertex(Object)}
	 * @deprecated                          Please use {@link #addVertex(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void addVertex(Integer vertex) {
		addVertex(vertex.intValue());
	}

	/**
	 * Add a new vertex to the built graph, using the vertex builder.
	 *
	 * <p>
	 * Unlike {@link #addVertex(int)} in which the vertex is provided by the user, this method uses the vertex builder
	 * obtained by {@link #vertexBuilder()} to create the new vertex and adds it to the graph.
	 *
	 * <p>
	 * This method is equivalent to:
	 *
	 * <pre> {@code
	 * int vertex = vertexBuilder().build(vertices());
	 * addVertex(vertex);
	 * return vertex;
	 * }</pre>
	 *
	 * @return                               the new vertex
	 * @throws UnsupportedOperationException if the builder does not have a vertex builder, namely if
	 *                                           {@link #vertexBuilder()} returns {@code null}
	 */
	default int addVertexInt() {
		IdBuilderInt vertexBuilder = vertexBuilder();
		if (vertexBuilder == null)
			throw new UnsupportedOperationException("No vertex builder");
		int vertex = vertexBuilder.build(vertices());
		addVertex(vertex);
		return vertex;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #addVertexInt()} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer addVertex() {
		return Integer.valueOf(addVertexInt());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Prefer to pass {@link IntCollection} instead of {@link Collection}&lt;{@link Integer}&gt; as collection of
	 * vertices.
	 */
	@Override
	void addVertices(Collection<? extends Integer> vertices);

	/**
	 * Add a new edge to the built graph.
	 *
	 * <p>
	 * If the built graph does not support self or parallel edges and the added edge is such edge, an exception will
	 * <b>not</b> be thrown. The edges are validated only when the graph is built, and an exception will be thrown only
	 * then.
	 *
	 * <p>
	 * Edges must be non negative integers.
	 *
	 * <p>
	 * If there is an edge builder, namely if {@link #edgeBuilder()} does not return {@code null}, the method
	 * {@link #addEdge(int, int)} can be used, which uses the edge builder to create the new edge object instead of
	 * requiring the user to provide it.
	 *
	 * @param  source                   a source vertex
	 * @param  target                   a target vertex
	 * @param  edge                     a new edge identifier
	 * @throws IllegalArgumentException if {@code edge} is already in the graph or if it is negative, as negative
	 *                                      identifiers are not allowed
	 * @throws NoSuchVertexException    if {@code source} or {@code target} are not valid vertices identifiers
	 */
	void addEdge(int source, int target, int edge);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #addEdge(int, int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void addEdge(Integer source, Integer target, Integer edge) {
		addEdge(source.intValue(), target.intValue(), edge.intValue());
	}

	/**
	 * Add a new edge to the built graph, using the edge builder.
	 *
	 * <p>
	 * Unlike {@link #addEdge(int, int, int)} in which the edge (identifier) is provided by the user, this method uses
	 * the edge builder obtained by {@link #edgeBuilder()} to create the new edge object and adds it to the graph.
	 *
	 * <p>
	 * If the graph does not support self or parallel edges and the added edge is such edge, an exception will
	 * <b>not</b> be thrown. The edges are validated only when the graph is built, and an exception will be thrown only
	 * then.
	 *
	 * <p>
	 * This method is equivalent to:
	 *
	 * <pre> {@code
	 * int edge = edgeBuilder().build(edges());
	 * addEdge(source, target, edge);
	 * return edge;
	 * }</pre>
	 *
	 * @param  source                        a source vertex
	 * @param  target                        a target vertex
	 * @return                               the new edge
	 * @throws UnsupportedOperationException if the builder does not have an edge builder, namely if
	 *                                           {@link #edgeBuilder()} returns {@code null}
	 * @throws NoSuchVertexException         if {@code source} or {@code target} are not valid vertices identifiers
	 */
	default int addEdge(int source, int target) {
		IdBuilderInt edgeBuilder = edgeBuilder();
		if (edgeBuilder == null)
			throw new UnsupportedOperationException("No edge builder");
		int edge = edgeBuilder.build(edges());
		addEdge(source, target, edge);
		return edge;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #addEdge(int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer addEdge(Integer source, Integer target) {
		return Integer.valueOf(addEdge(source.intValue(), target.intValue()));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Prefer to pass {@link IEdgeSet} instead of {@link EdgeSet}&lt;{@link Integer}, {@link Integer}&gt; as set of
	 * edges. See {@link IEdgeSet#of(IntSet, IntGraph)}.
	 *
	 * @throws IllegalArgumentException if any of the given edges are negative or any of reasons specified in
	 *                                      {@link GraphBuilder#addEdges(EdgeSet)}
	 */
	@Override
	void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges);

	/**
	 * {@inheritDoc}
	 *
	 * The return object is always some sub class of {@link IWeights}, such as {@link IWeightsInt} or
	 * {@link IWeightsDouble}.
	 */
	@Override
	<T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key);

	/**
	 * {@inheritDoc}
	 *
	 * The return object is always some sub class of {@link IWeights}, such as {@link IWeightsInt} or
	 * {@link IWeightsDouble}.
	 */
	@Override
	<T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key);

	@Override
	IdBuilderInt vertexBuilder();

	@Override
	IdBuilderInt edgeBuilder();

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
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IntGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @return a new empty builder for undirected graphs
	 */
	static IntGraphBuilder undirected() {
		return IntGraphFactory.undirected().newBuilder();
	}

	/**
	 * Create a new builder that builds directed int graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link IntGraphFactory}, namely they
	 * will not support self edges and will support parallel edges. See the factory documentation for more information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IntGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @return a new empty builder for directed graphs
	 */
	static IntGraphBuilder directed() {
		return IntGraphFactory.directed().newBuilder();
	}

	/**
	 * Create a new builder that builds un/directed int graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link IntGraphFactory}, namely they
	 * will not support self edges and will support parallel edges. See the factory documentation for more information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IntGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  directed if {@code true}, the new builder will build directed graphs, otherwise it will build undirected
	 *                      graphs
	 * @return          a new empty builder for un/directed graphs
	 */
	static IntGraphBuilder newInstance(boolean directed) {
		return IntGraphFactory.newInstance(directed).newBuilder();
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IntGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  g a graph
	 * @return   a builder initialized with the given graph vertices and edges, without the original graph
	 *           vertices/edges weights.
	 */
	static IntGraphBuilder newCopyOf(IntGraph g) {
		return newCopyOf(g, false, false);
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, with/without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IntGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  g                   a graph
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied from the graph to the
	 *                                 builder
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied from the graph to the
	 *                                 builder
	 * @return                     a builder initialized with the given graph vertices and edges, with/without the
	 *                             original graph vertices/edges weights.
	 */
	static IntGraphBuilder newCopyOf(IntGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return IntGraphFactory.newInstance(g.isDirected()).newBuilderCopyOf(g, copyVerticesWeights, copyEdgesWeights);
	}

}
