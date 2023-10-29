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

import java.util.Optional;
import java.util.Set;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A builder for {@linkplain IndexGraph Index graphs}.
 * <p>
 * The builder is used to construct <b>non-empty</b> index graphs. Differing from {@link IndexGraphFactory} which create
 * new empty graphs, the builder is used to add vertices and edges before actually creating the graph. This capability
 * is required to create immutable graphs, but can also be used to build mutable graph and may gain a performance boost
 * compared to creating an empty graph and adding the same vertices and edges.
 *
 * @see    IndexGraphBuilder#newUndirected()
 * @see    IndexGraphBuilder#newDirected()
 * @see    IntGraphBuilder
 * @see    IndexGraphFactory
 * @author Barak Ugav
 */
public interface IndexGraphBuilder {

	/**
	 * Get the set of vertices that were added to the graph.
	 *
	 * @return the graph vertices
	 */
	IntSet vertices();

	/**
	 * Get the set of edges that were added to the graph.
	 *
	 * @return the graph edges
	 */
	IntSet edges();

	/**
	 * Add a new vertex to the graph.
	 * <p>
	 * As the built graph is an Index graph, the vertices must be {@code 0,1,2,...,verticesNum-1} and user-chosen IDs
	 * are not supported. A new vertex will be assigned ID of value {@code vertices().size()}.
	 *
	 * @return the identifier of the new vertex
	 */
	int addVertex();

	/**
	 * Add a new edge to the graph.
	 * <p>
	 * As the built graph is an Index graph, the edges must be {@code 0,1,2,...,edgesNum-1}. A new edge will be assigned
	 * ID of value {@code edges().size()}.
	 * <p>
	 * It is possible to construct a graph by inserting the edges in a different order than their indices (IDs), by
	 * using {@link #addEdge(int, int, int)} in which the ID of the inserted edge is specified along with the source and
	 * target vertices. If this method is used, the set of edges will be validated when a new graph is created, and it
	 * must be equal {@code 0,1,2,...,edgesNum-1}. Only one of {@link #addEdge(int, int)} and
	 * {@link #addEdge(int, int, int)} can be used during the construction of a graph.
	 *
	 * @param  source the source vertex of the new edge
	 * @param  target the target vertex of the new edge
	 * @return        the identifier of the new edge
	 */
	int addEdge(int source, int target);

	/**
	 * Add a new edge to the graph, with user-chosen identifier.
	 * <p>
	 * This function is similar to {@link #addEdge(int, int)}, but let the user to choose the identifier of the new
	 * edge. As the built graph is an Index graph, the edges must be {@code 0,1,2,...,edgesNum-1}. This constraint is
	 * validated when the graph is actually created by the builder.
	 * <p>
	 * Instead of this method, {@link #addEdge(int, int)} can be used, letting the builder to choose the identifier of
	 * the new edge.Only one of {@link #addEdge(int, int)} and {@link #addEdge(int, int, int)} can be used during the
	 * construction of a graph.
	 *
	 * @param source the source vertex of the new edge
	 * @param target the target vertex of the new edge
	 * @param edge   the identifier of the new edge
	 */
	void addEdge(int source, int target, int edge);

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
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key        key of the weights
	 * @return            vertices weights of the key, or {@code null} if no container found with the specified key
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	<T, WeightsT extends IWeights<T>> WeightsT getVerticesWeights(String key);

	/**
	 * Add a new weights container associated with the vertices of the built graph.
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 * @param  <V>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	default <T, WeightsT extends IWeights<T>> WeightsT addVerticesWeights(String key, Class<? super T> type) {
		return addVerticesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the vertices of built graph with default value.
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 * @param  <V>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	<T, WeightsT extends IWeights<T>> WeightsT addVerticesWeights(String key, Class<? super T> type, T defVal);

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
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key        key of the weights
	 * @return            edges weights of the key, or {@code null} if no container found with the specified key
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	<T, WeightsT extends IWeights<T>> WeightsT getEdgesWeights(String key);

	/**
	 * Add a new weights container associated with the edges of the built graph.
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	default <T, WeightsT extends IWeights<T>> WeightsT addEdgesWeights(String key, Class<? super T> type) {
		return addEdgesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the edges of the built graph with default value.
	 * <p>
	 * See {@link IWeights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      key of the weights
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 * @param  <T>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types such
	 *                                      as {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	<T, WeightsT extends IWeights<T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal);

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
	 * Build a new immutable index graph with the builder vertices and edges.
	 *
	 * @return a new immutable index graph with the vertices and edges that were added to the builder
	 */
	IndexGraph build();

	/**
	 * Build a new mutable index graph with the builder vertices and edges.
	 *
	 * @return a new mutable index graph with the vertices and edges that were added to the builder
	 */
	IndexGraph buildMutable();

	/**
	 * Re-Index the vertices/edges and build a new immutable graph with the new indexing.
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 * <p>
	 * Note that this method is not <i>required</i> to re-index the vertices (edges) if {@code reIndexVertices}
	 * ({@code reIndexEdges}) is {@code true}, it is simply allowed to. Whether or not a re-indexing was performed can
	 * be checked via the {@link ReIndexedGraph} return value.
	 *
	 * @param  reIndexVertices if {@code true}, the implementation is allowed to (note that it is not required) to
	 *                             re-index the vertices of the graph. If {@code false}, the original vertices
	 *                             identifiers are used. Whether or not re-indexing was performed can be checked via
	 *                             {@link ReIndexedGraph#verticesReIndexing()}.
	 * @param  reIndexEdges    if {@code true}, the implementation is allowed to (note that it is not required) to
	 *                             re-index the edges of the graph. If {@code false}, the original edges identifiers are
	 *                             used. Whether or not re-indexing was performed can be checked via
	 *                             {@link ReIndexedGraph#edgesReIndexing()}.
	 * @return                 the re-indexed immutable graph, along with the re-indexing mapping to the original
	 *                         indices
	 */
	IndexGraphBuilder.ReIndexedGraph reIndexAndBuild(boolean reIndexVertices, boolean reIndexEdges);

	/**
	 * Re-Index the vertices/edges and build a new mutable graph with the new indexing.
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 * <p>
	 * Note that this method is not <i>required</i> to re-index the vertices (edges) if {@code reIndexVertices}
	 * ({@code reIndexEdges}) is {@code true}, it is simply allowed to. Whether or not a re-indexing was performed can
	 * be checked via the {@link ReIndexedGraph} return value.
	 *
	 * @param  reIndexVertices if {@code true}, the implementation is allowed to (note that it is not required) to
	 *                             re-index the vertices of the graph. If {@code false}, the original vertices
	 *                             identifiers are used. Whether or not re-indexing was performed can be checked via
	 *                             {@link ReIndexedGraph#verticesReIndexing()}.
	 * @param  reIndexEdges    if {@code true}, the implementation is allowed to (note that it is not required) to
	 *                             re-index the edges of the graph. If {@code false}, the original edges identifiers are
	 *                             used. Whether or not re-indexing was performed can be checked via
	 *                             {@link ReIndexedGraph#edgesReIndexing()}.
	 * @return                 the re-indexed mutable graph, along with the re-indexing mapping to the original indices
	 */
	IndexGraphBuilder.ReIndexedGraph reIndexAndBuildMutable(boolean reIndexVertices, boolean reIndexEdges);

	/**
	 * A result object of re-indexing and building a graph operation.
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 * <p>
	 * During the lifetime of a {@link IndexGraphBuilder}, vertices and edges are added to it, each one of them has a
	 * unique {@code int} identifier which is also its index (see {@link IndexGraph}). The builder can re-index the
	 * vertices/edges and build a new graph, resulting in a re-indexed graph {@link #graph()}, the vertices re-indexing
	 * {@link #verticesReIndexing()} and the edges re-indexing {@link #edgesReIndexing()}.
	 *
	 * @see    IndexGraphBuilder#reIndexAndBuild(boolean, boolean)
	 * @see    ReIndexingMap
	 * @author Barak Ugav
	 */
	static interface ReIndexedGraph {

		/**
		 * Get the newly created re-indexed graph
		 *
		 * @return the actual re-indexed graph
		 */
		IndexGraph graph();

		/**
		 * Get the re-indexing map of the vertices.
		 * <p>
		 * The returned object (if present) can map each original vertex index to its new index after re-indexing. If
		 * the returned is not present, the vertices were no re-indexed.
		 *
		 * @return the re-indexing map of the vertices
		 */
		Optional<IndexGraphBuilder.ReIndexingMap> verticesReIndexing();

		/**
		 * Get the re-indexing map of the edges.
		 * <p>
		 * The returned object (if present) can map each original edge index to its new index after re-indexing. If the
		 * returned is not present, the edges were no re-indexed.
		 *
		 * @return the re-indexing map of the edges
		 */
		Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing();
	}

	/**
	 * A map of indices, mapping an original index to a re-indexed index.
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 * <p>
	 * A 're-indexed' index is the index assigned to vertex/edge after a re-indexing operation on a graph. This
	 * interface is used to represent the mapping of both vertices and edges (a single instance map either vertices or
	 * edges), and it should be understood from the context which one is it. In the documentation we use the term
	 * <i>element</i> to refer to either vertex or edge.
	 * <p>
	 * Re-indexing of the vertices (or edges) is a mapping from {@code [0,1,2,...,verticesNum-1]} to
	 * {@code [0,1,2,...,verticesNum-1]}, namely its bijection function.
	 *
	 * @see    IndexGraphBuilder#reIndexAndBuild(boolean, boolean)
	 * @see    ReIndexedGraph
	 * @author Barak Ugav
	 */
	static interface ReIndexingMap {

		/**
		 * Map an element's original index to its re-indexed index.
		 *
		 * @param  orig an element's original index
		 * @return      the element's re-index index
		 */
		int origToReIndexed(int orig);

		/**
		 * Map an element's re-indexed index to its original index.
		 *
		 * @param  reindexed an element's re-indexed index
		 * @return           the element's original index
		 */
		int reIndexedToOrig(int reindexed);

	}

	/**
	 * Create a new builder that builds undirected graphs.
	 *
	 * @return a new empty builder for undirected graphs
	 */
	static IndexGraphBuilder newUndirected() {
		return new IndexGraphBuilderImpl.Undirected();
	}

	/**
	 * Create a new builder that builds directed graphs.
	 *
	 * @return a new empty builder for directed graphs
	 */
	static IndexGraphBuilder newDirected() {
		return new IndexGraphBuilderImpl.Directed();
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, without copying the weights.
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  g a graph
	 * @return   a builder initialized with the given graph vertices and edges, without the original graph
	 *           vertices/edges weights.
	 */
	static IndexGraphBuilder newFrom(IndexGraph g) {
		return newFrom(g, false);
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, with/without copying the weights.
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  g           a graph
	 * @param  copyWeights if {@code true}, the weights of the vertices and edges will be copied to the new graph
	 * @return             a builder initialized with the given graph vertices and edges, with/without the original
	 *                     graph vertices/edges weights.
	 */
	static IndexGraphBuilder newFrom(IndexGraph g, boolean copyWeights) {
		return IndexGraphBuilderImpl.newFrom(g, copyWeights);
	}

}
