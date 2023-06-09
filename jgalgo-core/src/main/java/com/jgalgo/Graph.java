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

import java.util.Set;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A discrete graph with vertices and edges.
 * <p>
 * A graph consist of a finite set of vertices \(V\) and edges \(E\). Vertices are some abstract entities, and edges are
 * connections between the vertices, for example vertices can be cities and edges could be the roads between them, or
 * vertices can be people the edges are the relation of "friends". Edges could be directed or undirected. Weights may be
 * assigned to vertices or edges, for example the length of a road might be a weight of an edge. Than, questions such as
 * "what is the shortest path between two cities?" might be answered using graph algorithms.
 * <p>
 * Each edge \(e=(u, v)\) in the graph has a <i>source</i>, \(u\), and a <i>target</i> \(v\). In undirected graphs the
 * 'source' and 'target' can be switched, as the edge is not directed, and we treat the source and target as
 * interchangeable <i>end points</i>. If an edge \((u,v)\) exist in the graph, we say the vertices \(u\) and \(v\) and
 * <i>neighbors</i>, or <i>adjacent</i>. The edges are usually stored in some list for each vertex, allowing efficient
 * iteration of its edges. The <i>degree</i> of a vertex is the number of its edges. In directed graph, we have both
 * <i>in-degree</i> and <i>out-degree</i>, which are the number of edges going in and out the vertex, respectively.
 * <p>
 * Vertices can be added or removed. When a vertex \(v\) is removed, all the edges with \(v\) as one of their end points
 * are removed as well. Edges can be added as connection to existing vertices, or removed.
 * <p>
 * A directed graph and an undirected graph both implement this interface. In a directed graph, the edges are
 * <i>directed</i> namely an edge \(e(u, v)\) will be contained in {@code outEdges(u)} and in {@code inEdges(v)} and
 * will not be contained in {@code outEdges(v)} and {@code inEdges(u)}. In an undirected graph, the edges are
 * undirected, namely an edge \(e(u, v)\) will be contained in {@code outEdges(u)}, {@code inEdges(v)},
 * {@code outEdges(v)} and in {@code inEdges(u)}. Also {@link #removeEdgesOf(int)}, {@link #removeInEdgesOf(int)} and
 * {@link #removeOutEdgesOf(int)} are equivalent for the same vertex. To check if a graph is directed or not, use the
 * {@link #getCapabilities()} method.
 * <p>
 * Each vertex and edge in the graph is identified by a unique non negative {@code int} ID. For example.
 * {@link #vertices()} returns an {@link IntSet} of all the {@code int} IDs of the vertices of the graph. This allow for
 * a more efficient graph implementations, rather then creating objects for vertices and edges. Vertices and edges may
 * be created by {@link #addVertex()} and {@link #addEdge(int, int)}, in which case the graph implementation will choose
 * the {@code int} ID and will return it to the user. Alternativaly, the methods {@link #addVertex(int)} and
 * {@link #addEdge(int, int, int)} can be used to add new vertices and edges with user chosen identifiers.
 * <p>
 * Weights may be assigned to the graph vertices and/or edges. A <i>weight</i> is some value such as {@code double}
 * primitive, {@code boolean} flag or an arbitrary Object. Multiple different weights can be added to the vertices
 * and/or edges, each is identified by some key. When a new weights type is added to a graph, its added to all
 * vertices/edges with either user provided default weight value, or null (0 in case the weight type is primitive). The
 * weights are accessed via the {@link Weights} container, which can be used to get or set an vertex/edge weight, and
 * can be passed to algorithms as {@link WeightFunction} for example. See {@link #addVerticesWeights(Object, Class)} and
 * {@link #addEdgesWeights(Object, Class)}, or {@link Weights} for the full weights documentation.
 * <p>
 * Each graph expose an <i>Index</i> view on itself via the {@link #indexGraph()} method. The returned
 * {@link IndexGraph} is a graph in which the identifiers of the vertices are always {@code (0,1,2, ...,verticesNum-1)},
 * and the identifiers of the edges are always {@code (0,1,2, ...,edgesNum-1)}. To maintain this, the index graph
 * implementation may rename existing vertices or edges along the graph lifetime. This rename behavior is less user
 * friendly, but allow for high performance boost as no hash tables are needed, a simple array or bitmap can be used to
 * map each vertex/edge to a value/weight/flag. See {@link IndexGraph} for more information. The {@link IndexGraph}
 * should not be used in scenarios where performance does not matter.
 * <p>
 * The number of vertices, \(|V|\), is usually denoted as \(n\) in algorithms time and space complexities, and
 * similarly, the number of edges, \(|E|\), is usually denoted as \(m\).
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * Graph g = Graph.newBuilderDirected().build();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int e1 = g.addEdge(v1, v2);
 * int e2 = g.addEdge(v2, v3);
 * int e3 = g.addEdge(v1, v3);
 *
 * // Assign some weights to the edges
 * Weights.Double w = g.addEdgesWeights("weightsKey", double.class);
 * w.set(e1, 1.2);
 * w.set(e2, 3.1);
 * w.set(e3, 15.1);
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newBuilder().build();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (int e : ssspRes.getPath(v3)) {
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    Graph.Builder
 * @see    GraphCapabilities
 * @see    IndexGraph
 * @author Barak Ugav
 */
public interface Graph {

	/**
	 * Get the set of all vertices of the graph.
	 * <p>
	 * Each vertex in the graph is identified by a unique non negative integer ID and the returned set is a set of all
	 * these identifiers.
	 * <p>
	 * The Graph object does not expose an explicit method to get the number of vertices, but it can accessed using this
	 * method by {@code g.vertices().size()}.
	 *
	 * @return a set containing all IDs of the graph vertices
	 */
	IntSet vertices();

	/**
	 * Get the set of all edges of the graph.
	 * <p>
	 * Each edge in the graph is identified by a unique non negative integer ID, and the returned set is a set of all
	 * these identifiers.
	 * <p>
	 * The Graph object does not expose an explicit method to get the number of edges, but it can accessed using this
	 * method by {@code g.edges().size()}.
	 *
	 * @return a set containing all IDs of the graph edges
	 */
	IntSet edges();

	/**
	 * Add a new vertex to the graph.
	 * <p>
	 * The graph implementation will choose a new {@code int} identifier which is not currently used as one of the graph
	 * edges, and will return it as the new vertex ID.
	 *
	 * @return the new vertex identifier
	 */
	int addVertex();

	/**
	 * Add a new vertex to the graph with user chosen ID.
	 * <p>
	 * In contrast to {@link #addVertex()}, in which the implementation chooses ,the new vertex identifier, the user can
	 * specified what {@code int} ID the new vertex should be assigned. The set of graph vertices must not contain
	 * duplications, therefore the provided identifier must not be currently used as one of the graph vertices IDs.
	 * <p>
	 * Note that vertices IDs must be non negative.
	 *
	 * @param  vertex                   a user chosen identifier for the new vertex
	 * @throws IllegalArgumentException if the provided identifier is already used as identifier of one of the graph
	 *                                      vertices, or if its negative
	 */
	void addVertex(int vertex);

	/**
	 * Remove a vertex and all its edges from the graph.
	 *
	 * @param  vertex                    the vertex identifier to remove
	 * @throws IndexOutOfBoundsException if {@code vertex} is not a valid vertex identifier
	 */
	void removeVertex(int vertex);

	/**
	 * Get the edges whose source is {@code source}.
	 * <p>
	 * In case the graph is undirected, the set will contain all edges whose {@code source} is one of their end points.
	 * <p>
	 * The Graph object does not expose an explicit method to get the (out) degree of a vertex, but it can accessed
	 * using this method by {@code g.outEdges(vertex).size()}.
	 *
	 * @param  source                    a source vertex
	 * @return                           all the edges whose source is {@code source}
	 * @throws IndexOutOfBoundsException if {@code source} is not a valid vertex identifier
	 */
	EdgeSet outEdges(int source);

	/**
	 * Get the edges whose target is {@code target}.
	 * <p>
	 * In case the graph is undirected, the set will contain all edges whose {@code target} is one of their end points.
	 * <p>
	 * The Graph object does not expose an explicit method to get the (in) degree of a vertex, but it can accessed using
	 * this method by {@code g.inEdges(vertex).size()}.
	 *
	 * @param  target                    a target vertex
	 * @return                           all the edges whose target is {@code target}
	 * @throws IndexOutOfBoundsException if {@code target} is not a valid vertex identifier
	 */
	EdgeSet inEdges(int target);

	/**
	 * Get the edge whose source is {@code source} and target is {@code target}.
	 * <p>
	 * If the graph is not directed, the return edge is an edge that its end-points are {@code source} and
	 * {@code target}.
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
		for (EdgeIter it = outEdges(source).iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (it.target() == target)
				return e;
		}
		return -1;
	}

	/**
	 * Get the edges whose source is {@code source} and target is {@code target}.
	 *
	 * @param  source                    a source vertex
	 * @param  target                    a target vertex
	 * @return                           all the edges whose source is {@code source} and target is {@code target}
	 * @throws IndexOutOfBoundsException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	EdgeSet getEdges(int source, int target);

	/**
	 * Add a new edge to the graph.
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

	/**
	 * Remove an edge from the graph.
	 *
	 * @param  edge                      the edge identifier
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	void removeEdge(int edge);

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

	/**
	 * Remove all edges whose source is {@code source}.
	 *
	 * @param  source                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code source} is not a valid vertex identifier
	 */
	default void removeOutEdgesOf(int source) {
		for (EdgeIter eit = outEdges(source).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Remove all edges whose target is {@code target}.
	 *
	 * @param  target                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code target} is not a valid vertex identifier
	 */
	default void removeInEdgesOf(int target) {
		for (EdgeIter eit = inEdges(target).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Reverse an edge by switching its source and target.
	 * <p>
	 * If the graph is undirected, this method does nothing.
	 *
	 * @param  edge                      an existing edge in the graph
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	void reverseEdge(int edge);

	/**
	 * Get the source vertex of an edge.
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of the edge, but always other end-point
	 * than {@link #edgeTarget(int)} returns.
	 *
	 * @param  edge                      the edge identifier
	 * @return                           the edge source vertex
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	int edgeSource(int edge);

	/**
	 * Get the target vertex of an edge.
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of the edge, but always the other
	 * end-point than {@link #edgeSource(int)} returns.
	 *
	 * @param  edge                      the edge identifier
	 * @return                           the edge target vertex
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	int edgeTarget(int edge);

	/**
	 * Get the other end-point of an edge.
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
			throw new IllegalArgumentException(
					"The given vertex (" + endpoint + ") is not an endpoint of the edge (" + u + ", " + v + ")");
		}
	}

	/**
	 * Clear the graph completely by removing all vertices and edges.
	 * <p>
	 * This function might be used to reuse an already allocated graph object.
	 * <p>
	 * Note that this function also clears any weights associated with the vertices or edges.
	 */
	void clear();

	/**
	 * Remove all the edges from the graph.
	 * <p>
	 * Note that this function also clears any weights associated with the edges.
	 */
	void clearEdges();

	/**
	 * Get the vertices weights of some key.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key        some key of the weights, could be anything
	 * @return            vertices weights of the key, or null if no container found with the specified key
	 * @param  <V>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types
	 */
	<V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key);

	/**
	 * Add a new weights container associated with the vertices of this graph.
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated. To create an
	 * external weights container, for example in cases the graph is a user input we are not allowed to modify it, use
	 * {@link Weights#createExternalVerticesWeights(Graph, Class)}.
	 *
	 * <pre> {@code
	 * Graph g = ...;
	 * int v1 = g.newVertex();
	 * int v2 = g.newVertex();
	 *
	 * Weights<String> names = g.addVerticesWeights("name", String.class);
	 * names.set(v1, "Alice");
	 * names.set(v2, "Bob");
	 *
	 * Weights.Int ages = g.addVerticesWeights("age", int.class);
	 * ages.set(v1, 42);
	 * ages.set(v2, 35);
	 * }</pre>
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      some key of the weights, could be anything
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 * @param  <V>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types
	 */
	default <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
		return addVerticesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the vertices of this graph with default value.
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated. To create an
	 * external weights container, for example in cases the graph is a user input we are not allowed to modify it, use
	 * {@link Weights#createExternalVerticesWeights(Graph, Class, Object)}.
	 *
	 * <pre> {@code
	 * Graph g = ...;
	 * int v1 = g.newVertex();
	 * int v2 = g.newVertex();
	 * int v3 = g.newVertex();
	 *
	 * Weights<String> names = g.addVerticesWeights("name", String.class, "Unknown");
	 * names.set(v1, "Alice");
	 * names.set(v2, "Bob");
	 *
	 * assert "Alice".equals(names.get(v1))
	 * assert "Bob".equals(names.get(v2))
	 * assert "Unknown".equals(names.get(v3))
	 * }</pre>
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      some key of the weights, could be anything
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the same key already exists in the graph
	 * @param  <V>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types
	 */
	<V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal);

	/**
	 * Remove a weight type associated with the vertices of the graph.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key the key of the weights
	 */
	void removeVerticesWeights(Object key);

	/**
	 * Get the keys of all the associated vertices weights.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated vertices weights
	 */
	Set<Object> getVerticesWeightsKeys();

	/**
	 * Get the edges weights of some key.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key        some key of the weights, could be anything
	 * @return            edges weights of the key, or null if no container found with the specified key
	 * @param  <E>        The weight data type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types
	 */
	<E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key);

	/**
	 * Add a new weights container associated with the edges of this graph.
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated. To create an
	 * external weights container, for example in cases the graph is a user input you are not allowed to modify it, use
	 * {@link Weights#createExternalEdgesWeights(Graph, Class)}.
	 *
	 * <pre> {@code
	 * Graph g = ...;
	 * int v1 = g.addVertex();
	 * int v2 = g.addVertex();
	 * int v3 = g.addVertex();
	 * int e1 = g.addEdge(v1, v2);
	 * int e2 = g.addEdge(v2, v3);
	 *
	 * Weights<String> roadTypes = g.addEdgesWeights("roadType", String.class);
	 * roadTypes.set(e1, "Asphalt");
	 * roadTypes.set(e2, "Gravel");
	 *
	 * Weights.Double roadLengths = g.addEdgesWeights("roadLength", double.class);
	 * roadLengths.set(e1, 42);
	 * roadLengths.set(e2, 35);
	 * }</pre>
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      some key of the weights, could be anything
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 * @param  <E>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types
	 */
	default <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
		return addEdgesWeights(key, type, null);
	}

	/**
	 * Add a new weights container associated with the edges of this graph with default value.
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated. To create an
	 * external weights container, for example in cases the graph is a user input we are not allowed to modify it, use
	 * {@link Weights#createExternalEdgesWeights(Graph, Class, Object)}.
	 *
	 * <pre> {@code
	 * Graph g = ...;
	 * int v1 = g.addVertex();
	 * int v2 = g.addVertex();
	 * int v3 = g.addVertex();
	 * int e1 = g.addEdge(v1, v2);
	 * int e2 = g.addEdge(v2, v3);
	 * int e3 = g.addEdge(v1, v3);
	 *
	 * Weights<String> roadTypes = g.addEdgesWeights("roadType", String.class, "Unknown");
	 * roadTypes.set(e1, "Asphalt");
	 * roadTypes.set(e2, "Gravel");
	 *
	 * assert "Asphalt".equals(names.get(e1))
	 * assert "Gravel".equals(names.get(e2))
	 * assert "Unknown".equals(names.get(e3))
	 * }</pre>
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param  key                      some key of the weights, could be anything
	 * @param  type                     the type of the weights, used for primitive types weights
	 * @param  defVal                   default value use for the weights container
	 * @return                          a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same key already exists in the graph
	 * @param  <E>                      The weight data type
	 * @param  <WeightsT>               the weights container, used to avoid casts of containers of primitive types
	 */
	<E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal);

	/**
	 * Remove a weight type associated with the edges of the graph.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key the key of the weights
	 */
	void removeEdgesWeights(Object key);

	/**
	 * Get the keys of all the associated edges weights.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated edges weights
	 */
	Set<Object> getEdgesWeightsKeys();

	/**
	 * Get the {@linkplain GraphCapabilities capabilities} of this graph.
	 *
	 * @return a {@link GraphCapabilities} object describing what this graph support and what not.
	 * @see    GraphCapabilities
	 */
	GraphCapabilities getCapabilities();

	/**
	 * Get an Index graph view of this graph.
	 * <p>
	 * The returned {@link IndexGraph} is a graph in which the identifiers of the vertices are always
	 * {@code (0,1,2, ...,verticesNum-1)}, and the identifiers of the edges are always {@code (0,1,2, ...,edgesNum-1)}.
	 * To maintain this, the index graph implementation may rename existing vertices or edges along the graph lifetime.
	 * This rename behavior is less user friendly, but allow for high performance boost as no hash tables are needed, a
	 * simple array or bitmap can be used to map each vertex/edge to a value/weight/flag. See {@link IndexGraph} for
	 * more information. The {@link IndexGraph} should not be used in scenarios where performance does not matter.
	 * <p>
	 * The returned graph is a view, namely a graph that will contain the same vertices and edges (with different
	 * {@code int} identifiers), and the same associated weights, that is automatically updated when the original graph
	 * is updated and visa versa.
	 * <p>
	 * If this graph is an Index graph, this method returns this graph.
	 *
	 * @return an {@link IndexGraph} view of this graph
	 */
	IndexGraph indexGraph();

	/**
	 * Get the index-id vertices mapping of this graph.
	 * <p>
	 * A regular graph contains vertices and edges which are identified by a fixed {@code int} IDs. An
	 * {@link IndexGraph} view is provided by the {@link #indexGraph()} method, which is a graph in which all methods
	 * are accessed with <b>indices</b> rather than fixed IDs. This method expose the mapping between the indices and
	 * the fixed IDs of the graph vertices.
	 * <p>
	 * Note that the mapping may change during the graph lifetime, as vertices are added and removed from the graph, and
	 * a regular graph IDs are fixed, while a index graph indices are always {@code (0,1,2, ...,verticesNum-1)}. The
	 * returned mapping object will be updated automatically in such cases.
	 *
	 * @return a mapping that map vertices IDs to vertices indices
	 */
	IndexIdMap indexGraphVerticesMap();

	/**
	 * Get the index-id edges mapping of this graph.
	 * <p>
	 * A regular graph contains vertices and edges which are identified by a fixed {@code int} IDs. An
	 * {@link IndexGraph} view is provided by the {@link #indexGraph()} method, which is a graph in which all methods
	 * are accessed with <b>indices</b> rather than fixed IDs. This method expose the mapping between the indices and
	 * the fixed IDs of the graph edges.
	 * <p>
	 * Note that the mapping may change during the graph lifetime, as edges are added and removed from the graph, and a
	 * regular graph IDs are fixed, while a index graph indices are always {@code (0,1,2, ...,edgesNum-1)}. The returned
	 * mapping object will be updated automatically in such cases.
	 *
	 * @return a mapping that map edges IDs to edges indices
	 */
	IndexIdMap indexGraphEdgesMap();

	/**
	 * Create a copy of this graph.
	 * <p>
	 * An identical copy of this graph will be created, with the same vertices, edges, weights and capabilities.
	 *
	 * @return an identical copy of this graph
	 */
	Graph copy();

	/**
	 * Get an unmodifiable view of this graph.
	 * <p>
	 * This method return a view of this graph, namely a Graph that contains the same vertices, edges and weights, that
	 * is automatically updated when the original graph is updated. The view is unmodifiable, namely all operations that
	 * modify the graph will throw {@link UnsupportedOperationException}.
	 *
	 * @return an unmodifiable view of this graph
	 */
	default Graph unmodifiableView() {
		return Graphs.unmodifiableView(this);
	}

	/**
	 * Get a reversed view of this graph.
	 * <p>
	 * This method return a view of this graph, namely a Graph that contains the same vertices, edges and weights, that
	 * is automatically updated when the original graph is updated and visa versa. The view is reversed, namely each
	 * source and target vertices of each edge are swapped.
	 * <p>
	 * Note that modifying the returned view will change the original graph.
	 *
	 * @return a reversed view of this graph
	 */
	default Graph reverseView() {
		return Graphs.reverseView(this);
	}

	/**
	 * Create an undirected graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new undirected graph.
	 *
	 * @return a new builder that can build undirected graphs
	 */
	static Graph.Builder newBuilderUndirected() {
		return new GraphImpl.Builder(false);
	}

	/**
	 * Create a directed graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new directed graph.
	 *
	 * @return a new builder that can build directed graphs
	 */
	static Graph.Builder newBuilderDirected() {
		return new GraphImpl.Builder(true);
	}

	/**
	 * A builder for {@link Graph} objects.
	 *
	 * @see    Graph#newBuilderDirected()
	 * @see    Graph#newBuilderUndirected()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<Graph.Builder> {

		/**
		 * Create a new empty graph.
		 *
		 * @return a new graph with the builder options
		 */
		Graph build();

		/**
		 * Determine if graphs built by this builder should be directed or not.
		 *
		 * @param  directed if {@code true}, graphs built by this builder will be directed
		 * @return          this builder
		 */
		Graph.Builder setDirected(boolean directed);

		/**
		 * Set the expected number of vertices that will exist in the graph.
		 *
		 * @param  expectedVerticesNum the expected number of vertices in the graph
		 * @return                     this builder
		 */
		Graph.Builder expectedVerticesNum(int expectedVerticesNum);

		/**
		 * Set the expected number of edges that will exist in the graph.
		 *
		 * @param  expectedEdgesNum the expected number of edges in the graph
		 * @return                  this builder
		 */
		Graph.Builder expectedEdgesNum(int expectedEdgesNum);

		/**
		 * Add a hint to this builder.
		 * <p>
		 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
		 *
		 * @param  hint the hint to add
		 * @return      this builder
		 */
		Graph.Builder addHint(Graph.Builder.Hint hint);

		/**
		 * Remove a hint from this builder.
		 * <p>
		 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
		 *
		 * @param  hint the hint to remove
		 * @return      this builder
		 */
		Graph.Builder removeHint(Graph.Builder.Hint hint);

		/**
		 * Hints for a graph builder.
		 * <p>
		 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
		 *
		 * @author Barak Ugav
		 */
		static enum Hint {
			/** The graph should support fast edge removal via {@link Graph#removeEdge(int)} */
			FastEdgeRemoval,
			/** The graph should support fast edge lookup via {@link Graph#getEdge(int, int)} */
			FastEdgeLookup,
		}

	}

}
