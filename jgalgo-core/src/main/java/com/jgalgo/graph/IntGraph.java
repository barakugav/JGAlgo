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
import java.util.Optional;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A discrete graph with vertices and edges.
 *
 * <p>
 * A graph consist of a finite set of vertices \(V\) and edges \(E\). Vertices are some abstract entities, and edges are
 * connections between the vertices, for example vertices can be cities and edges could be the roads between them, or
 * vertices can be people the edges are the relation of "friends". Edges could be directed or undirected. Weights may be
 * assigned to vertices or edges, for example the length of a road might be a weight of an edge. Than, questions such as
 * "what is the shortest path between two cities?" might be answered using graph algorithms.
 *
 * <p>
 * Each edge \(e=(u, v)\) in the graph has a <i>source</i> vertex, \(u\), and a <i>target</i> vertex, \(v\). In
 * undirected graphs the 'source' and 'target' can be switched, as the edge is not directed, and we treat the source and
 * target as interchangeable <i>end points</i>. If an edge \((u,v)\) exist in the graph, we say the vertices \(u\) and
 * \(v\) and <i>neighbors</i>, or <i>adjacent</i>. The edges are usually stored in some list for each vertex, allowing
 * efficient iteration of its edges. The <i>degree</i> of a vertex is the number of its edges. In directed graph, we
 * have both <i>in-degree</i> and <i>out-degree</i>, which are the number of edges going in and out the vertex,
 * respectively.
 *
 * <p>
 * Vertices can be added or removed. When a vertex \(v\) is removed, all the edges with \(v\) as one of their end points
 * are removed as well. Edges can be added as connection to existing vertices, or removed.
 *
 * <p>
 * A directed graph and an undirected graph both implement this interface. In a directed graph, the edges are
 * <i>directed</i>, namely an edge \(e(u, v)\) will be contained in {@code outEdges(u)} and in {@code inEdges(v)} and
 * will not be contained in {@code outEdges(v)} and {@code inEdges(u)}. In an undirected graph, the edges are
 * undirected, namely an edge \(e(u, v)\) will be contained in {@code outEdges(u)}, {@code inEdges(v)},
 * {@code outEdges(v)} and in {@code inEdges(u)}. Also {@link #removeEdgesOf(int)}, {@link #removeInEdgesOf(int)} and
 * {@link #removeOutEdgesOf(int)} are equivalent for the same vertex in an undirected graph. To check if a graph is
 * directed or not, use the {@link #isDirected()} method.
 *
 * <p>
 * Each vertex and edge in the graph is identified by a unique non negative {@code int} ID. The existing vertices and
 * edges of the graph can be retrieved using {@link #vertices()} and {@link #edges()}. Vertices and edges may be created
 * by {@link #addVertex()} and {@link #addEdge(int, int)}, in which case the graph implementation will choose the
 * {@code int} ID and will return it to the user. Alternatively, the methods {@link #addVertex(int)} and
 * {@link #addEdge(int, int, int)} can be used to add new vertices and edges with user chosen identifiers.
 *
 * <p>
 * Weights may be assigned to the graph vertices and/or edges. A <i>weight</i> is some value such as any primitive (for
 * example {@code double}, {@code int} or {@code boolean} flag) or an Object. Multiple different weights can be added to
 * the vertices and/or edges, each is identified by some key. When a new weights type is added to a graph, it is added
 * to <i>all</i> the vertices/edges, with either user provided default weight value, or {@code null} ({@code 0} in case
 * the weight type is primitive). The weights are accessed via the {@link IWeights} container, which can be used to get
 * or set a vertex/edge weight, and can be passed to algorithms as a {@link IWeightFunction} for example. See
 * {@link #addVerticesWeights(String, Class)} and {@link #addEdgesWeights(String, Class)}, or {@link IWeights} for the
 * full weights documentation.
 *
 * <p>
 * Each graph expose an <i>Index</i> view on itself via the {@link #indexGraph()} method. The returned
 * {@link IndexGraph} is a graph in which the identifiers of the vertices are always {@code (0,1,2, ...,verticesNum-1)},
 * and the identifiers of the edges are always {@code (0,1,2, ...,edgesNum-1)}. To maintain this, the index graph
 * implementation may rename existing vertices or edges along the graph lifetime. This rename behavior is less user
 * friendly, but allow for high performance boost as no hash tables are needed, a simple array or bitmap can be used to
 * map each vertex/edge to a value/weight/flag. See {@link IndexGraph} for more information. The {@link IndexGraph}
 * should not be used in scenarios where performance does not matter.
 *
 * <p>
 * The number of vertices and edges can be read via {@code g.vertices().size()} and {@code g.edges().size()}. The out or
 * in degree of a vertex is exposed by {@code g.outEdges(vertex).size()} and {@code g.inEdges(vertex).size()}.
 *
 * <p>
 * The number of vertices, \(|V|\), is usually denoted as \(n\) in algorithms time and space complexities, and
 * similarly, the number of edges, \(|E|\), is usually denoted as \(m\).
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
	 * In contrast to {@link #addVertex()}, in which the implementation chooses ,the new vertex identifier, the user can
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

	@Deprecated
	@Override
	default void addVertex(Integer vertex) {
		addVertex(vertex.intValue());
	}

	/**
	 * Remove a vertex and all its edges from the graph.
	 *
	 * @param  vertex                    the vertex identifier to remove
	 * @throws IndexOutOfBoundsException if {@code vertex} is not a valid vertex identifier
	 */
	void removeVertex(int vertex);

	@Deprecated
	@Override
	default void removeVertex(Integer vertex) {
		removeVertex(vertex.intValue());
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
	 * @param  source                    a source vertex
	 * @return                           all the edges whose source is {@code source}
	 * @throws IndexOutOfBoundsException if {@code source} is not a valid vertex identifier
	 */
	IEdgeSet outEdges(int source);

	@Deprecated
	@Override
	default EdgeSet<Integer, Integer> outEdges(Integer source) {
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
	 * @param  target                    a target vertex
	 * @return                           all the edges whose target is {@code target}
	 * @throws IndexOutOfBoundsException if {@code target} is not a valid vertex identifier
	 */
	IEdgeSet inEdges(int target);

	@Deprecated
	@Override
	default EdgeSet<Integer, Integer> inEdges(Integer target) {
		return inEdges(target.intValue());
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
	 * @param  source                    a source vertex
	 * @param  target                    a target vertex
	 * @return                           id of the edge or {@code -1} if no such edge exists
	 * @throws IndexOutOfBoundsException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	default int getEdge(int source, int target) {
		for (IEdgeIter it = outEdges(source).iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (it.targetInt() == target)
				return e;
		}
		return -1;
	}

	@Deprecated
	@Override
	default Integer getEdge(Integer source, Integer target) {
		int e = getEdge(source.intValue(), target.intValue());
		return e == -1 ? null : Integer.valueOf(e);
	}

	/**
	 * Get the edges whose source is {@code source} and target is {@code target}.
	 *
	 * @param  source                    a source vertex
	 * @param  target                    a target vertex
	 * @return                           all the edges whose source is {@code source} and target is {@code target}
	 * @throws IndexOutOfBoundsException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	IEdgeSet getEdges(int source, int target);

	@Deprecated
	@Override
	default EdgeSet<Integer, Integer> getEdges(Integer source, Integer target) {
		return getEdges(source.intValue(), target.intValue());
	}

	/**
	 * Add a new edge to the graph.
	 *
	 * <p>
	 * The graph implementation will choose a new {@code int} identifier which is not currently used as one of the graph
	 * edges, and will return it as the new edge ID.
	 *
	 * @param  source                    a source vertex
	 * @param  target                    a target vertex
	 * @return                           the new edge identifier
	 * @throws IndexOutOfBoundsException if {@code source} or {@code target} are not valid vertices identifiers
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
	 * @throws IllegalArgumentException if the provided identifier is already used as identifier of one of the graph
	 *                                      edges, or if its negative
	 */
	void addEdge(int source, int target, int edge);

	@Deprecated
	@Override
	default void addEdge(Integer source, Integer target, Integer edge) {
		addEdge(source.intValue(), target.intValue(), edge.intValue());
	}

	/**
	 * Remove an edge from the graph.
	 *
	 * @param  edge                      the edge identifier
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	void removeEdge(int edge);

	@Deprecated
	@Override
	default void removeEdge(Integer edge) {
		removeEdge(edge.intValue());
	}

	/**
	 * Remove all the edges of a vertex.
	 *
	 * @param  vertex                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code vertex} is not a valid vertex identifier
	 */
	default void removeEdgesOf(int vertex) {
		removeOutEdgesOf(vertex);
		removeInEdgesOf(vertex);
	}

	@Deprecated
	@Override
	default void removeEdgesOf(Integer vertex) {
		removeEdgesOf(vertex.intValue());
	}

	/**
	 * Remove all edges whose source is {@code source}.
	 *
	 * @param  source                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code source} is not a valid vertex identifier
	 */
	default void removeOutEdgesOf(int source) {
		for (IEdgeIter eit = outEdges(source).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Deprecated
	@Override
	default void removeOutEdgesOf(Integer vertex) {
		removeOutEdgesOf(vertex.intValue());
	}

	/**
	 * Remove all edges whose target is {@code target}.
	 *
	 * @param  target                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code target} is not a valid vertex identifier
	 */
	default void removeInEdgesOf(int target) {
		for (IEdgeIter eit = inEdges(target).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Deprecated
	@Override
	default void removeInEdgesOf(Integer vertex) {
		removeInEdgesOf(vertex.intValue());
	}

	/**
	 * Reverse an edge by switching its source and target.
	 *
	 * <p>
	 * If the graph is undirected, this method does nothing.
	 *
	 * @param  edge                      an existing edge in the graph
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	void reverseEdge(int edge);

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
	 * @param  edge                      the edge identifier
	 * @return                           the edge source vertex
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	int edgeSource(int edge);

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
	 * @param  edge                      the edge identifier
	 * @return                           the edge target vertex
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	int edgeTarget(int edge);

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
	 * @param  edge                      an edge identifier
	 * @param  endpoint                  one of the edge end-point
	 * @return                           the other end-point of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 * @throws IllegalArgumentException  if {@code endpoint} is not an endpoint of the edge
	 */
	default int edgeEndpoint(int edge, int endpoint) {
		int u = edgeSource(edge);
		int v = edgeTarget(edge);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException("The given vertex (idx=" + endpoint
					+ ") is not an endpoint of the edge (idx=" + u + ", idx=" + v + ")");
		}
	}

	@Deprecated
	@Override
	default Integer edgeEndpoint(Integer edge, Integer endpoint) {
		return Integer.valueOf(edgeEndpoint(edge.intValue(), endpoint.intValue()));
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
	 * @param  <T>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 * @param  key        key of the weights
	 * @return            edges weights of the key, or {@code null} if no container found with the specified key
	 */
	<T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key);

	@Override
	default <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
		return getEdgesIWeights(key);
	}

	@Override
	IndexIntIdMap indexGraphVerticesMap();

	@Override
	IndexIntIdMap indexGraphEdgesMap();

	@Override
	default IntGraph copy() {
		return (IntGraph) Graph.super.copy();
	}

	@Override
	default IntGraph copy(boolean copyWeights) {
		return IntGraphFactory.newFrom(this).newCopyOf(this, copyWeights);
	}

	@Override
	default IntGraph immutableCopy() {
		return IntGraphBuilder.newFrom(this).build();
	}

	@Override
	default IntGraph immutableCopy(boolean copyWeights) {
		IndexIntIdMap viMap = indexGraphVerticesMap();
		IndexIntIdMap eiMap = indexGraphEdgesMap();
		if (isDirected()) {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph =
					GraphCSRDirectedReindexed.newInstance(indexGraph(), copyWeights);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			viMap = vReIndexing.isEmpty() ? viMap
					: IntGraphBuilderImpl.Directed.reIndexedIdMap(viMap, vReIndexing.get());
			eiMap = eReIndexing.isEmpty() ? eiMap
					: IntGraphBuilderImpl.Directed.reIndexedIdMap(eiMap, eReIndexing.get());
			return new IntGraphImpl.Directed(iGraph, viMap, eiMap);
		} else {
			IndexGraph iGraph = new GraphCSRUndirected(indexGraph(), copyWeights);
			return new IntGraphImpl.Undirected(iGraph, viMap, eiMap);
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

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Prefer to pass a IntCollection instead of Collection&lt;Integer&gt; as collections of vertices and edges.
	 */
	@Override
	default IntGraph subGraphCopy(Collection<Integer> vertices, Collection<Integer> edges) {
		return (IntGraph) Graphs.subGraph(this, vertices, edges);
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
		return IntGraphFactory.newUndirected().newGraph();
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
		return IntGraphFactory.newDirected().newGraph();
	}

}
