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
import java.util.Map;
import java.util.Optional;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A discrete graph with {@code int} vertices and edges.
 *
 * <p>
 * This interface is a specification of {@link Graph} for graphs with {@code int} vertices and edges, similarly how
 * {@link Int2IntMap} is a specification of {@link Map} for maps with {@code int} keys and values. Methods that accept a
 * primitive {@code int} as an identifier are provided, and the original ones that accept a {@link Integer} are
 * deprecated. Specific {@link IEdgeSet} and {@link IEdgeIter} are returned for edges queries, avoiding boxing of
 * integers. Similarly, the {@link IWeights} interface is used for weights containers, which accept primitive
 * {@code int} as identifiers.
 *
 * <p>
 * Each vertex and edge in the graph is identified by a unique non negative {@code int} identifier. Vertices and edges
 * may be created by {@link #addVertex()} and {@link #addEdge(int, int)}, in which case the graph implementation will
 * choose the {@code int} identifier and will return it to the user. Alternatively, the methods {@link #addVertex(int)}
 * and {@link #addEdge(int, int, int)} (similar the regular {@link Graph} methods) can be used to add new vertices and
 * edges with user chosen identifiers.
 *
 * <p>
 * Implementations of this interface are more efficient than the generic {@link Graph} interface, and should be used for
 * better performance if its needed. For even better performance, consider using {@link IndexGraph}, which does violate
 * the {@link Graph} interface as its vertices and edges may change during the lifetime of the graph and therefore less
 * user friendly, but is even more efficient.
 *
 * <p>
 * To create a new empty graph, use {@link #newUndirected()} or {@link #newDirected()}. The returned graph will use the
 * default implementation. For more control over the graph details, see {@link IntGraphFactory}. To construct an
 * immutable graph, use {@link IntGraphBuilder}.
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * IntGraph g = IntGraph.newDirected();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int e1 = g.addEdge(v1, v2);
 * int e2 = g.addEdge(v2, v3);
 * int e3 = g.addEdge(v1, v3);
 *
 * // Assign some weights to the edges
 * IWeightsDouble weights = g.addEdgesWeights("weightsKey", double.class);
 * weights.set(e1, 1.2);
 * weights.set(e2, 3.1);
 * weights.set(e3, 15.1);
 * IWeightFunction weightFunc = weights;
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, weightFunc, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).edges().equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (int e : ssspRes.getPath(v3).edges()) {
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    IntGraphFactory
 * @see    IntGraphBuilder
 * @see    IndexGraph
 * @author Barak Ugav
 */
public interface IntGraph extends Graph<Integer, Integer> {

	@Override
	IntSet vertices();

	@Override
	IntSet edges();

	/**
	 * Add a new vertex to the graph.
	 *
	 * <p>
	 * The graph implementation will choose a new {@code int} identifier which is not currently used as one of the graph
	 * edges, and will return it as the new vertex ID.
	 *
	 * @return the new vertex identifier
	 */
	int addVertex();

	/**
	 * Add a new vertex to the graph with user chosen ID.
	 *
	 * <p>
	 * In contrast to {@link #addVertex()}, in which the implementation chooses the new vertex identifier, the user can
	 * specified what {@code int} ID the new vertex should be assigned. The set of graph vertices must not contain
	 * duplications, therefore the provided identifier must not be currently used as one of the graph vertices IDs.
	 *
	 * <p>
	 * Note that vertices IDs must be non negative.
	 *
	 * @param  vertex                   a user chosen identifier for the new vertex
	 * @throws IllegalArgumentException if the provided identifier is already used as identifier of one of the graph
	 *                                      vertices, or if its negative
	 */
	void addVertex(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #addVertex(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void addVertex(Integer vertex) {
		addVertex(vertex.intValue());
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
	 * Remove a vertex and all its edges from the graph.
	 *
	 * @param  vertex                the vertex identifier to remove
	 * @throws NoSuchVertexException if {@code vertex} is not a valid vertex identifier
	 */
	void removeVertex(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #removeVertex(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void removeVertex(Integer vertex) {
		removeVertex(vertex.intValue());
	}

	/**
	 * Set a new identifier for an existing vertex.
	 *
	 * <p>
	 * This method changes the identifier of an existing vertex, while keeping the edges connecting to it, along with
	 * the weights associated with it.
	 *
	 * @param  vertex                   an existing vertex in the graph
	 * @param  newId                    the new vertex identifier
	 * @throws NoSuchVertexException    if {@code vertex} is not a valid vertex identifier
	 * @throws IllegalArgumentException if {@code newId} is already in the graph or if {@code newId} is {@code null}
	 */
	void renameVertex(int vertex, int newId);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #renameVertex(int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void renameVertex(Integer vertex, Integer newId) {
		renameVertex(vertex.intValue(), newId.intValue());
	}

	/**
	 * Get the edges whose source is {@code source}.
	 *
	 * <p>
	 * In case the graph is undirected, the set will contain all edges whose {@code source} is one of their end points.
	 *
	 * <p>
	 * The graph object does not expose an explicit method to get the (out) degree of a vertex, but it can accessed
	 * using this method by {@code g.outEdges(vertex).size()}.
	 *
	 * @param  source                a source vertex
	 * @return                       all the edges whose source is {@code source}
	 * @throws NoSuchVertexException if {@code source} is not a valid vertex identifier
	 */
	IEdgeSet outEdges(int source);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #outEdges(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default IEdgeSet outEdges(Integer source) {
		return outEdges(source.intValue());
	}

	/**
	 * Get the edges whose target is {@code target}.
	 *
	 * <p>
	 * In case the graph is undirected, the set will contain all edges whose {@code target} is one of their end points.
	 *
	 * <p>
	 * The graph object does not expose an explicit method to get the (in) degree of a vertex, but it can accessed using
	 * this method by {@code g.inEdges(vertex).size()}.
	 *
	 * @param  target                a target vertex
	 * @return                       all the edges whose target is {@code target}
	 * @throws NoSuchVertexException if {@code target} is not a valid vertex identifier
	 */
	IEdgeSet inEdges(int target);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #inEdges(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default IEdgeSet inEdges(Integer target) {
		return inEdges(target.intValue());
	}

	/**
	 * Check whether the graph contains an edge with the given source and target.
	 *
	 * <p>
	 * If the graph is undirected, the method will return {@code true} if there is an edge whose end-points are
	 * {@code source} and {@code target}, regardless of which is the source and which is the target.
	 *
	 * @param  source                the source vertex
	 * @param  target                the target vertex
	 * @return                       {@code true} if the graph contains an edge with the given source and target,
	 *                               {@code false} otherwise
	 * @throws NoSuchVertexException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	default boolean containsEdge(int source, int target) {
		return getEdge(source, target) != -1;
	}

	@Deprecated
	@Override
	default boolean containsEdge(Integer source, Integer target) {
		return getEdge(source.intValue(), target.intValue()) != -1;
	}

	/**
	 * Get the edge whose source is {@code source} and target is {@code target}.
	 *
	 * <p>
	 * If the graph is not directed, the return edge is an edge that its end-points are {@code source} and
	 * {@code target}.
	 *
	 * <p>
	 * In case there are multiple (parallel) edges between {@code source} and {@code target}, a single arbitrary one is
	 * returned.
	 *
	 * @param  source                a source vertex
	 * @param  target                a target vertex
	 * @return                       id of the edge or {@code -1} if no such edge exists
	 * @throws NoSuchVertexException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	default int getEdge(int source, int target) {
		for (IEdgeIter it = outEdges(source).iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (it.targetInt() == target)
				return e;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #getEdge(int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer getEdge(Integer source, Integer target) {
		int e = getEdge(source.intValue(), target.intValue());
		return e == -1 ? null : Integer.valueOf(e);
	}

	/**
	 * Get the edges whose source is {@code source} and target is {@code target}.
	 *
	 * @param  source                a source vertex
	 * @param  target                a target vertex
	 * @return                       all the edges whose source is {@code source} and target is {@code target}
	 * @throws NoSuchVertexException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	IEdgeSet getEdges(int source, int target);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #getEdges(int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default IEdgeSet getEdges(Integer source, Integer target) {
		return getEdges(source.intValue(), target.intValue());
	}

	/**
	 * Add a new edge to the graph.
	 *
	 * <p>
	 * The graph implementation will choose a new {@code int} identifier which is not currently used as one of the graph
	 * edges, and will return it as the new edge ID.
	 *
	 * @param  source                   a source vertex
	 * @param  target                   a target vertex
	 * @return                          the new edge identifier
	 * @throws IllegalArgumentException if the graph does not support parallel edges and an edge between {@code source}
	 *                                      and {@code target} already exists or if the graph does not support self
	 *                                      edges and {@code source} and {@code target} are the same vertex
	 * @throws NoSuchVertexException    if {@code source} or {@code target} are not valid vertices identifiers
	 */
	int addEdge(int source, int target);

	/**
	 * Add a new edge to the graph with user chosen ID.
	 *
	 * <p>
	 * In contrast to {@link #addEdge(int, int)}, in which the implementation chooses the new edge identifier, the user
	 * can specified what {@code int} ID the new edge should be assigned. The set of graph edges must not contain
	 * duplications, therefore the provided identifier must not be currently used as one of the graph edges IDs.
	 *
	 * @param  source                   a source vertex
	 * @param  target                   a target vertex
	 * @param  edge                     a user chosen identifier for the new edge
	 * @throws IllegalArgumentException if {@code edge} is already in the graph, or if {@code edge} is {@code null} or
	 *                                      if the graph does not support parallel edges and an edge between
	 *                                      {@code source} and {@code target} already exists or if the graph does not
	 *                                      support self edges and {@code source} and {@code target} are the same vertex
	 * @throws IllegalArgumentException if the provided identifier is already used as identifier of one of the graph
	 *                                      edges, or if its negative
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
	 * {@inheritDoc}
	 *
	 * <p>
	 * Prefer to pass {@link IEdgeSet} instead of {@link EdgeSet}&lt;{@link Integer}, {@link Integer}&gt; as set of
	 * edges. See {@link IEdgeSet#of(IntSet, IntGraph)}.
	 */
	@Override
	void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges);

	/**
	 * Remove an edge from the graph.
	 *
	 * @param  edge                the edge identifier
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge identifier
	 */
	void removeEdge(int edge);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #removeEdge(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void removeEdge(Integer edge) {
		removeEdge(edge.intValue());
	}

	/**
	 * Remove all the edges of a vertex.
	 *
	 * @param  vertex                a vertex in the graph
	 * @throws NoSuchVertexException if {@code vertex} is not a valid vertex identifier
	 */
	default void removeEdgesOf(int vertex) {
		removeOutEdgesOf(vertex);
		removeInEdgesOf(vertex);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #removeEdgesOf(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void removeEdgesOf(Integer vertex) {
		removeEdgesOf(vertex.intValue());
	}

	/**
	 * Remove all edges whose source is {@code source}.
	 *
	 * @param  source                a vertex in the graph
	 * @throws NoSuchVertexException if {@code source} is not a valid vertex identifier
	 */
	default void removeOutEdgesOf(int source) {
		for (IEdgeIter eit = outEdges(source).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #removeOutEdgesOf(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void removeOutEdgesOf(Integer vertex) {
		removeOutEdgesOf(vertex.intValue());
	}

	/**
	 * Remove all edges whose target is {@code target}.
	 *
	 * @param  target                a vertex in the graph
	 * @throws NoSuchVertexException if {@code target} is not a valid vertex identifier
	 */
	default void removeInEdgesOf(int target) {
		for (IEdgeIter eit = inEdges(target).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #removeInEdgesOf(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void removeInEdgesOf(Integer vertex) {
		removeInEdgesOf(vertex.intValue());
	}

	/**
	 * Set a new identifier for an existing edge.
	 *
	 * <p>
	 * This method changes the identifier of an existing edge, while keeping the source and target of the edge, along
	 * with the weights associated with it.
	 *
	 * @param  edge                     an existing edge in the graph
	 * @param  newId                    the new edge identifier
	 * @throws NoSuchEdgeException      if {@code edge} is not a valid edge identifier
	 * @throws IllegalArgumentException if {@code newId} is already in the graph or if {@code newId} is negative
	 */
	void renameEdge(int edge, int newId);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #renameEdge(int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void renameEdge(Integer edge, Integer newId) {
		renameEdge(edge.intValue(), newId.intValue());
	}

	/**
	 * Move an existing edge to new source and target vertices.
	 *
	 * <p>
	 * This method changes the source and target of an existing edge, while keeping the identifier of the edge and the
	 * weights associated with it.
	 *
	 * @param  edge                     an existing edge in the graph
	 * @param  newSource                the new source vertex
	 * @param  newTarget                the new target vertex
	 * @throws NoSuchEdgeException      if {@code edge} is not a valid edge identifier
	 * @throws NoSuchVertexException    if {@code newSource} or {@code newTarget} are not valid vertices identifiers
	 * @throws IllegalArgumentException if {@code newSource} and {@code newTarget} are the same vertex and the graph
	 *                                      does not support self edges, or if the graph does not support parallel edges
	 *                                      and an edge between {@code newSource} and {@code newTarget} already exists
	 */
	void moveEdge(int edge, int newSource, int newTarget);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #moveEdge(int, int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void moveEdge(Integer edge, Integer newSource, Integer newTarget) {
		moveEdge(edge.intValue(), newSource.intValue(), newTarget.intValue());
	}

	/**
	 * Reverse an edge by switching its source and target.
	 *
	 * @param  edge                     an existing edge in the graph
	 * @throws NoSuchEdgeException      if {@code edge} is not a valid edge identifier
	 * @throws IllegalArgumentException if the graph does not support parallel edges and another edge which is the
	 *                                      reverse of {@code edge} already exists in the graph
	 */
	default void reverseEdge(int edge) {
		moveEdge(edge, edgeTarget(edge), edgeSource(edge));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #reverseEdge(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void reverseEdge(Integer edge) {
		reverseEdge(edge.intValue());
	}

	/**
	 * Get the source vertex of an edge.
	 *
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of the edge, but always other end-point
	 * than {@link #edgeTarget(int)} returns.
	 *
	 * @param  edge                the edge identifier
	 * @return                     the edge source vertex
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge identifier
	 */
	int edgeSource(int edge);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #edgeSource(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer edgeSource(Integer edge) {
		return Integer.valueOf(edgeSource(edge.intValue()));
	}

	/**
	 * Get the target vertex of an edge.
	 *
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of the edge, but always the other
	 * end-point than {@link #edgeSource(int)} returns.
	 *
	 * @param  edge                the edge identifier
	 * @return                     the edge target vertex
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge identifier
	 */
	int edgeTarget(int edge);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #edgeTarget(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer edgeTarget(Integer edge) {
		return Integer.valueOf(edgeTarget(edge.intValue()));
	}

	/**
	 * Get the other end-point of an edge.
	 *
	 * <p>
	 * Given an edge \((u,v)\) and a vertex \(w\), assuming \(w\) is an endpoint of the edge, namely that \(w\) is
	 * either \(u\) or \(v\), the method will return the <i>other</i> endpoint which is not \(w\). If \(w=u\) the method
	 * will return \(v\), if \(w=v\) the method will return \(u\).
	 *
	 * @param  edge                     an edge identifier
	 * @param  endpoint                 one of the edge end-point
	 * @return                          the other end-point of the edge
	 * @throws NoSuchEdgeException      if {@code edge} is not a valid edge identifier
	 * @throws NoSuchVertexException    if {@code endpoint} is not a valid vertex identifier
	 * @throws IllegalArgumentException if {@code endpoint} is not an endpoint of the edge
	 */
	int edgeEndpoint(int edge, int endpoint);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #edgeEndpoint(int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer edgeEndpoint(Integer edge, Integer endpoint) {
		return Integer.valueOf(edgeEndpoint(edge.intValue(), endpoint.intValue()));
	}

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
	IndexIntIdMap indexGraphVerticesMap();

	@Override
	IndexIntIdMap indexGraphEdgesMap();

	@Override
	default IntGraph copy() {
		return (IntGraph) Graph.super.copy();
	}

	@Override
	default IntGraph copy(boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return (IntGraph) Graph.super.copy(copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	default IntGraph immutableCopy() {
		return IntGraphBuilder.newCopyOf(this).build();
	}

	@Override
	default IntGraph immutableCopy(boolean copyVerticesWeights, boolean copyEdgesWeights) {
		IndexIntIdMap viMap = indexGraphVerticesMap();
		IndexIntIdMap eiMap = indexGraphEdgesMap();
		if (isDirected()) {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph =
					GraphCsrDirectedReindexed.newInstance(indexGraph(), copyVerticesWeights, copyEdgesWeights);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			return new IntGraphImpl(iGraph, viMap, eiMap, vReIndexing.orElse(null), eReIndexing.orElse(null));
		} else {
			IndexGraph iGraph = new GraphCsrUndirected(indexGraph(), copyVerticesWeights, copyEdgesWeights);
			return new IntGraphImpl(iGraph, viMap, eiMap, null, null);
		}
	}

	@Override
	default IntGraph immutableView() {
		return (IntGraph) Graph.super.immutableView();
	}

	@Override
	default IntGraph reverseView() {
		return (IntGraph) Graph.super.reverseView();
	}

	@Override
	default IntGraph undirectedView() {
		return (IntGraph) Graph.super.undirectedView();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Prefer to pass a IntCollection instead of Collection&lt;Integer&gt; as collections of vertices and edges.
	 */
	@Override
	default IntGraph subGraphCopy(Collection<Integer> vertices, Collection<Integer> edges) {
		return (IntGraph) Graph.super.subGraphCopy(vertices, edges);
	}

	/**
	 * Create a new undirected empty int graph.
	 *
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link IntGraphFactory}.
	 *
	 * @return a new undirected empty graph
	 */
	static IntGraph newUndirected() {
		return IntGraphFactory.undirected().newGraph();
	}

	/**
	 * Create a new directed empty int graph.
	 *
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link IntGraphFactory}.
	 *
	 * @return a new directed empty graph
	 */
	static IntGraph newDirected() {
		return IntGraphFactory.directed().newGraph();
	}

}
