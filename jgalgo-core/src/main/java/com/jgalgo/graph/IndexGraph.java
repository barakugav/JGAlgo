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
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A graph whose vertices and edges identifiers are indices.
 *
 * <p>
 * The {@link Graph} interface provide addition, removal and querying of vertices and edges, all using some hashable
 * identifiers. These identifiers are fixed, and once a vertex or edge is assigned an ID, it will not change during the
 * graph lifetime. On the other hand, an <i>Index</i> graph is a {@link IntGraph} object in which the vertices and edges
 * identifiers of the graph are <b>always</b> {@code (0,1,2, ...,verticesNum-1)} and {@code (0,1,2, ...,edgesNum-1)}.
 *
 * <p>
 * The index graph invariants allow for a great performance boost, as a simple array or bitmap can be used to associate
 * a value/weight/flag with each vertex/edge. But it does come with a cost: to maintain the invariants, implementations
 * may need to rename existing vertices or edges during the graph lifetime. These renames can be subscribed-to using
 * {@link #addVertexRemoveListener(IndexRemoveListener)} and {@link #addEdgeRemoveListener(IndexRemoveListener)}.
 *
 * <p>
 * An index graph may be obtained as a view from a regular {@link Graph} using {@link Graph#indexGraph()}, or it can be
 * created on its own using {@link IndexGraphFactory}. In cases where no removal of vertices or edges is required, and
 * there is no need to use pre-defined IDs, there is no drawback of using the {@link IndexGraph} as a regular
 * {@link IntGraph}, as it will expose an identical functionality while providing better performance.
 *
 * <p>
 * If an {@link IndexGraph} is obtained from a regular {@link Graph} (or {@link IntGraph}) using
 * {@link Graph#indexGraph()}, the {@link IndexGraph} should not be modified directly. Vertices/edges/weights should be
 * added/removed only via the original fixed identifiers graph.
 *
 * <p>
 * All graph algorithms implementations should operation on Index graphs only, for best performance. If a regular
 * {@link Graph} is provided to an algorithm, the Index graph should be retrieved using {@link Graph#indexGraph()}, the
 * algorithm expensive logic should operate on the returned Index graph and finally the result should be transformed
 * back to the regular graph IDs. The mapping from a regular graph IDs to indices and vice versa is provided by
 * {@link IndexIdMap}, which can be accessed using {@link Graph#indexGraphVerticesMap()} and
 * {@link Graph#indexGraphEdgesMap()}.
 *
 * <p>
 * To create a new empty index graph, use {@link #newUndirected()} or {@link #newDirected()}. The returned graph will
 * use the default implementation. For more control over the graph details, see {@link IndexGraphFactory}. To construct
 * an immutable index graph, use {@link IndexGraphBuilder}.
 *
 * @see    IndexIdMap
 * @author Barak Ugav
 */
public interface IndexGraph extends IntGraph {

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * In an Index graph, the set of vertices are always {@code (0,1,2, ...,verticesNum-1)}.
	 */
	@Override
	IntSet vertices();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * In an Index graph, the set of edges are always {@code (0,1,2, ...,edgesNum-1)}.
	 */
	@Override
	IntSet edges();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The vertex created by this method will be assigned the next available index, {@code verticesNum}. For example, if
	 * the graph currently contains the vertices {@code 0,1,2}, the next vertex added will be {@code 3}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	int addVertexInt();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Index graphs vertices IDs are always {@code (0,1,2, ...,verticesNum-1)} therefore the only vertex ID that can be
	 * added is {@code verticesNum}. For any other vertex passed to this method, an exception will be thrown. If
	 * {@code verticesNum} is passed, this method is equivalent to {@link #addVertexInt()}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 *
	 * @throws     IllegalArgumentException if {@code vertex} is not {@code verticesNum}
	 * @deprecated                          use {@link #addVertexInt()} instead
	 */
	@Deprecated
	@Override
	default void addVertex(int vertex) {
		if (vertex != vertices().size())
			throw new IllegalArgumentException("Only vertex ID " + vertices().size() + " can be added");
		addVertexInt();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Index graphs vertices IDs are always {@code (0,1,2, ...,verticesNum-1)} therefore the only vertices that can be
	 * added are {@code (verticesNum,verticesNum+1,verticesNum+2, ...)}. For any other vertices passed to this method,
	 * an exception will be thrown.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void addVertices(Collection<? extends Integer> vertices);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * After removing a vertex, the graph implementation may swap and rename vertices to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addVertexRemoveListener(IndexRemoveListener)}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void removeVertex(int vertex);

	/**
	 * Unsupported operation.
	 *
	 * @throws     UnsupportedOperationException always
	 * @deprecated                               unsupported operation
	 */
	@Deprecated
	@Override
	default void renameVertex(int vertex, int newId) {
		throw new UnsupportedOperationException("Index graphs do not support vertex rename");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The edge created by this method will be assigned the next available index, {@code edgesNum}. For example, if the
	 * graph currently contains the edges {@code 0,1,2}, the next edge added will be {@code 3}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	int addEdge(int source, int target);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Index graphs edges IDs are always {@code (0,1,2, ...,edgesNum-1)} therefore the only edge ID that can be added is
	 * {@code edgesNum}. For any other edge passed to this method, an exception will be thrown. If {@code edgesNum} is
	 * passed, this method is equivalent to {@link #addEdge(int, int)}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 *
	 * @throws     IllegalArgumentException if {@code edge} is not {@code edgesNum}
	 * @deprecated                          use {@link #addEdge(int, int)} instead
	 */
	@Deprecated
	@Override
	default void addEdge(int source, int target, int edge) {
		if (edge != edges().size())
			throw new IllegalArgumentException("Only edge ID " + edges().size() + " can be added");
		addEdge(source, target);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Index graphs edges IDs are always {@code (0,1,2, ...,edgesNum-1)} therefore the only edges that can be added are
	 * {@code (edgesNum,edgesNum+1,edgesNum+2, ...)}. For any other edges passed to this method, an exception will be
	 * thrown. If there is no need to keep the identifiers of the edges, consider using
	 * {@link #addEdgesReassignIds(IEdgeSet)}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges);

	/**
	 * Add multiple edges to the graph and re-assign ids for them.
	 *
	 * <p>
	 * The {@link IEdgeSet} passed to this method contains the endpoints (sources and targets) of the edges, see
	 * {@link EdgeSet#iterator()}, {@link EdgeIter#source()}, {@link EdgeIter#target()}. The identifiers of the edges,
	 * which are also accessible via {@link IEdgeSet} are ignored, and new identifiers (indices) are assigned to the
	 * added edges. An {@link IEdgeSet} can be obtained from one of the methods of another {@link IntGraph}, or using
	 * {@link IEdgeSet#of(IntSet, IntGraph)}.
	 *
	 * <p>
	 * The identifiers assigned to the newly added edges are {@code (edgesNum,edgesNum+1,edgesNum+2, ...)} matching the
	 * iteration order of the provided set. This method different than {@link #addEdges(EdgeSet)} in a similar way that
	 * {@link #addEdge(int, int)} is different than {@link #addEdge(int, int, int)}.
	 *
	 * <p>
	 * If the graph does not support self edges and one of the added edges have the same vertex as source and target, an
	 * exception will be thrown. If the graph does not support parallel edges, and one of the added edges have the same
	 * source and target as one of the existing edges in the graph, or if two of the added edges have the same source
	 * and target, an exception will be thrown.
	 *
	 * <p>
	 * In the following snippet, a maximum cardinality matching is computed on a graph, and a new graph containing only
	 * the matching edges is created. It would be wrong to use {@link #addEdges(EdgeSet)} in this example, as there is
	 * no guarantee that the added edges ids are {@code (0, 1, 2, ...)}, which is a requirement to maintain the index
	 * graph invariants.
	 *
	 * <pre> {@code
	 * IndexGraph g = ...;
	 * IntSet matching = (IntSet) MatchingAlgo.newInstance().computeMaximumMatching(g, null).edges();
	 *
	 * IndexGraph matchingGraph = IndexGraph.newUndirected();
	 * matchingGraph.addVertices(g.vertices());
	 * matchingGraph.addEdgesReassignIds(IEdgeSet.of(matching, g));
	 * }</pre>
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 *
	 * @param  edges the set of edges to add. Only the endpoints of the edges is considered, while the edges identifiers
	 *                   are ignored.
	 * @return       the set of newly edge identifiers added to the graph,
	 *               {@code (edgesNum,edgesNum+1,edgesNum+2, ...)}. The edges are assigned the indices in the order they
	 *               are iterated in the given set
	 */
	IntSet addEdgesReassignIds(IEdgeSet edges);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeRemoveListener(IndexRemoveListener)}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void removeEdge(int edge);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeRemoveListener(IndexRemoveListener)}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void removeEdgesOf(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeRemoveListener(IndexRemoveListener)}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void removeOutEdgesOf(int source);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * After removing an edge, the graph implementation may swap and rename edges to maintain its invariants. Theses
	 * renames can be subscribed using {@link #addEdgeRemoveListener(IndexRemoveListener)}.
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void removeInEdgesOf(int target);

	/**
	 * Unsupported operation.
	 *
	 * @throws     UnsupportedOperationException always
	 * @deprecated                               unsupported operation
	 */
	@Deprecated
	@Override
	default void renameEdge(int edge, int newId) {
		throw new UnsupportedOperationException("Index graphs do not support edge rename");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void clear();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void clearEdges();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The vertex builder returned by this method always assign the next available index, {@code verticesNum}, given the
	 * current set of vertices {@code (0,1,2, ...,verticesNum-1)}. For example, if the graph currently contains the
	 * vertices {@code 0,1,2}, the next vertex added will be {@code 3}. The builder simply returns the current vertices
	 * set size, without validating that the set is actually {@code (0,1,2, ...,verticesNum-1)}.
	 */
	@Override
	default IdBuilderInt vertexBuilder() {
		return Graphs.IndexGraphIdBuilder;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The edge builder returned by this method always assign the next available index, {@code edgesNum}, given the
	 * current set of edges {@code (0,1,2, ...,edgesNum-1)}. For example, if the graph currently contains the edges
	 * {@code 0,1,2}, the next edge added will be {@code 3}. The builder simply returns the current edges set size,
	 * without validating that the set is actually {@code (0,1,2, ...,edgesNum-1)}.
	 */
	@Override
	default IdBuilderInt edgeBuilder() {
		return Graphs.IndexGraphIdBuilder;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	default <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type) {
		return IntGraph.super.addVerticesWeights(key, type);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	<T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type, T defVal);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void removeVerticesWeights(String key);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	default <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type) {
		return IntGraph.super.addEdgesWeights(key, type);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	<T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If this index graph object was obtained from a regular {@link Graph} using {@link Graph#indexGraph()}, this
	 * method should not be called. Use the original graph instead.
	 */
	@Override
	void removeEdgesWeights(String key);

	/**
	 * Adds a listener that will be called each time a vertex remove or swap is performed.
	 *
	 * <p>
	 * An {@link IndexGraph} may rename vertices during its lifetime to maintain the invariant that all vertices are
	 * identified by {@code 0,1,2,...,verticesNum-1}. This method can be used to track these changes, by registering a
	 * listener that will be invoked each time such a rename is performed.
	 *
	 * <p>
	 * If a vertex is removed with the last index ({@code verticesNum-1}), the vertex can simply be removed. Otherwise,
	 * the vertex will be swapped with the last vertex and then removed. In both cases, the listener will be called.
	 *
	 * @param listener a remove listener that will be called each time a vertex remove or swap is performed
	 */
	void addVertexRemoveListener(IndexRemoveListener listener);

	/**
	 * Removes a vertex remove listener.
	 *
	 * <p>
	 * After a listener was added using {@link #addVertexRemoveListener(IndexRemoveListener)}, this method can remove
	 * it.
	 *
	 * @param listener a remove listener that should be removed
	 */
	void removeVertexRemoveListener(IndexRemoveListener listener);

	/**
	 * Adds a listener that will be called each time a edge swap is performed.
	 *
	 * <p>
	 * An {@link IndexGraph} may rename edges during its lifetime to maintain the invariant that all edges are
	 * identified by {@code 0,1,2,...,edgesNum-1}. This method can be used to track these changes, by registering a
	 * listener that will be invoked each time such a rename is performed.
	 *
	 * <p>
	 * If an edge is removed with the last index ({@code edgesNum-1}), the edge can simply be removed. Otherwise, the
	 * edge will be swapped with the last edge and then removed. In both cases, the listener will be called.
	 *
	 * @param listener a remove listener that will be called each time a edge remove or swap is performed
	 */
	void addEdgeRemoveListener(IndexRemoveListener listener);

	/**
	 * Removes an edge remove listener.
	 *
	 * <p>
	 * After a listener was added using {@link #addEdgeRemoveListener(IndexRemoveListener)}, this method can remove it.
	 *
	 * @param listener a remove listener that should be removed
	 */
	void removeEdgeRemoveListener(IndexRemoveListener listener);

	/**
	 * The index graph of an {@link IndexGraph} is itself.
	 *
	 * @return     this graph
	 * @deprecated this function will always return the same graph, no reason to call it
	 */
	@Deprecated
	@Override
	default IndexGraph indexGraph() {
		return this;
	}

	/**
	 * The IDs and indices of an {@link IndexGraph} are the same.
	 *
	 * @deprecated this function will always return the identity mapping, no reason to call it
	 */
	@Deprecated
	@Override
	IndexIntIdMap indexGraphVerticesMap();

	/**
	 * The IDs and indices of an {@link IndexGraph} are the same.
	 *
	 * @deprecated this function will always return the identity mapping, no reason to call it
	 */
	@Deprecated
	@Override
	IndexIntIdMap indexGraphEdgesMap();

	@Override
	default IndexGraph copy() {
		return (IndexGraph) IntGraph.super.copy();
	}

	@Override
	default IndexGraph copy(boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return (IndexGraph) IntGraph.super.copy(copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	default IndexGraph immutableCopy() {
		return immutableCopy(false, false);
	}

	@Override
	default IndexGraph immutableCopy(boolean copyVerticesWeights, boolean copyEdgesWeights) {
		if (isDirected()) {
			return new GraphCsrDirected(this, copyVerticesWeights, copyEdgesWeights);
		} else {
			return new GraphCsrUndirected(this, copyVerticesWeights, copyEdgesWeights);
		}
	}

	@Override
	default IndexGraph immutableView() {
		return (IndexGraph) IntGraph.super.immutableView();
	}

	@Override
	default IndexGraph reverseView() {
		return (IndexGraph) IntGraph.super.reverseView();
	}

	@Override
	default IndexGraph undirectedView() {
		return (IndexGraph) IntGraph.super.undirectedView();
	}

	/**
	 * Create a new undirected empty index graph.
	 *
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link IndexGraphFactory}.
	 *
	 * @return a new undirected empty index graph
	 */
	static IndexGraph newUndirected() {
		return IndexGraphFactory.undirected().newGraph();
	}

	/**
	 * Create a new directed empty index graph.
	 *
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link IndexGraphFactory}.
	 *
	 * @return a new directed empty index graph
	 */
	static IndexGraph newDirected() {
		return IndexGraphFactory.directed().newGraph();
	}

}
