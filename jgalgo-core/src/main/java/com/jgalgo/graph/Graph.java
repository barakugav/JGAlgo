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
import java.util.Set;

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
 * A directed graph and an undirected graph are both implemented by this interface. In a directed graph, the edges are
 * <i>directed</i>, namely an edge \(e=(u, v)\) will be contained in {@code outEdges(u)} and in {@code inEdges(v)} and
 * will not be contained in {@code outEdges(v)} and {@code inEdges(u)}. In an undirected graph, the edges are
 * undirected, namely an edge \(e=\{u, v\}\) will be contained in {@code outEdges(u)}, {@code inEdges(v)},
 * {@code outEdges(v)} and in {@code inEdges(u)}. Also {@link #removeEdgesOf(Object)}, {@link #removeInEdgesOf(Object)}
 * and {@link #removeOutEdgesOf(Object)} are equivalent for the same vertex in an undirected graph. To check if a graph
 * is directed or not, use the {@link #isDirected()} method.
 *
 * <p>
 * Each vertex and edge in the graph is identified by a unique non null hashable object. The existing vertices and edges
 * of the graph can be retrieved using {@link #vertices()} and {@link #edges()}. Vertices and edges may be added by
 * {@link #addVertex(Object)} and {@link #addEdge(Object, Object, Object)}.
 *
 * <p>
 * Weights may be assigned to the graph vertices and/or edges. A <i>weight</i> is some value such as any primitive (for
 * example {@code double}, {@code int} or {@code boolean} flag) or an Object. Multiple different weights can be added to
 * the vertices and/or edges, each is identified by some string key. When a new weights type is added to a graph, it is
 * added to <i>all</i> the vertices/edges, with either user provided default weight value, or {@code null} ({@code 0} in
 * case the weight type is primitive). The weights are accessed via the {@link Weights} container, which can be used to
 * get or set a vertex/edge weight, and can be passed to algorithms as a {@link WeightFunction} for example. See
 * {@link #addVerticesWeights(String, Class)} and {@link #addEdgesWeights(String, Class)}, or {@link Weights} for the
 * full weights documentation.
 *
 * <p>
 * Each graph expose an <i>Index</i> view on itself via the {@link #indexGraph()} method. The returned
 * {@link IndexGraph} is a graph in which the identifiers of the vertices are always {@code (0,1,2, ...,verticesNum-1)},
 * and the identifiers of the edges are always {@code (0,1,2, ...,edgesNum-1)}. To maintain this, the index graph
 * implementation may rename existing vertices or edges along the graph lifetime. This rename behavior is less user
 * friendly, but allow for high performance boost as no hash tables are needed, a simple array or bitmap can be used to
 * map each vertex/edge to a value/weight/flag. The index graph returned by {@link #indexGraph()} should not be modified
 * directly by adding/removing vertices/edges/weights, use the enclosing graph instead. See {@link IndexGraph} for more
 * information. The {@link IndexGraph} should not be used in scenarios where performance does not matter.
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
 * default implementation. For more control over the graph details, see {@link GraphFactory}. To construct an immutable
 * graph, use {@link GraphBuilder}.
 *
 * <pre> {@code
 * // Create an undirected graph with three vertices and edges between them
 * Graph<String, Integer> g = Graph.newUndirected();
 * g.addVertex("Berlin");
 * g.addVertex("Leipzig");
 * g.addVertex("Dresden");
 * g.addEdge("Berlin", "Leipzig", 9);
 * g.addEdge("Berlin", "Dresden", 13);
 * g.addEdge("Dresden", "Leipzig", 14);
 *
 * // Assign some weights to the edges
 * WeightsDouble<Integer> w = g.addEdgesWeights("distance-km", double.class);
 * w.set(9, 191.1);
 * w.set(13, 193.3);
 * w.set(14, 121.3);
 *
 * // Calculate the shortest paths from Berlin to all other cities
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result<String, Integer> ssspRes = ssspAlgo.computeShortestPaths(g, w, "Berlin");
 *
 * // Print the shortest path from Berlin to Leipzig
 * System.out.println("Distance from Berlin to Leipzig is: " + ssspRes.distance("Leipzig"));
 * System.out.println("The shortest path from Berlin to Leipzig is:");
 * for (Integer e : ssspRes.getPath("Leipzig").edges()) {
 * 	String u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        GraphFactory
 * @see        GraphBuilder
 * @see        IndexGraph
 * @author     Barak Ugav
 */
public interface Graph<V, E> {

	/**
	 * Get the set of all vertices of the graph.
	 *
	 * <p>
	 * Each vertex in the graph is identified by a unique non null hashable object and the returned set is a set of all
	 * these identifiers.
	 *
	 * <p>
	 * The Graph object does not expose an explicit method to get the number of vertices, but it can accessed using this
	 * method by {@code g.vertices().size()}.
	 *
	 * @return a set containing all vertices of the graph
	 */
	Set<V> vertices();

	/**
	 * Get the set of all edges of the graph.
	 *
	 * <p>
	 * Each edge in the graph is identified by a unique non null hashable object, and the returned set is a set of all
	 * these identifiers.
	 *
	 * <p>
	 * The Graph object does not expose an explicit method to get the number of edges, but it can accessed using this
	 * method by {@code g.edges().size()}.
	 *
	 * @return a set containing all edges of the graph
	 */
	Set<E> edges();

	/**
	 * Add a new vertex to the graph.
	 *
	 * <p>
	 * A vertex can be any non null hashable object, namely it must implement {@link Object#hashCode()} and
	 * {@link Object#equals(Object)} methods. The set of graph vertices must not contain duplications, therefore the
	 * provided identifier must not be currently used as one of the graph vertices IDs.
	 *
	 * @param vertex new vertex
	 */
	void addVertex(V vertex);

	/**
	 * Remove a vertex and all its edges from the graph.
	 *
	 * @param  vertex                the vertex identifier to remove
	 * @throws NoSuchVertexException if {@code vertex} is not a valid vertex identifier
	 */
	void removeVertex(V vertex);

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
	EdgeSet<V, E> outEdges(V source);

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
	EdgeSet<V, E> inEdges(V target);

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
	 * @return                       id of the edge or {@code null} if no such edge exists
	 * @throws NoSuchVertexException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	default E getEdge(V source, V target) {
		for (EdgeIter<V, E> it = outEdges(source).iterator(); it.hasNext();) {
			E e = it.next();
			if (target.equals(it.target()))
				return e;
		}
		return null;
	}

	/**
	 * Get the edges whose source is {@code source} and target is {@code target}.
	 *
	 * @param  source                a source vertex
	 * @param  target                a target vertex
	 * @return                       all the edges whose source is {@code source} and target is {@code target}
	 * @throws NoSuchVertexException if {@code source} or {@code target} are not valid vertices identifiers
	 */
	EdgeSet<V, E> getEdges(V source, V target);

	/**
	 * Add a new edge to the graph.
	 *
	 * <p>
	 * If the graph does not support parallel edges, and an edge between {@code source} and {@code target} already
	 * exists, an exception will be raised. If the graph does not support self edges, and {@code source} and
	 * {@code target} are the same vertex, an exception will be raised.
	 *
	 * <p>
	 * The edge identifier must be unique and non null.
	 *
	 * @param source a source vertex
	 * @param target a target vertex
	 * @param edge   a new edge identifier
	 */
	void addEdge(V source, V target, E edge);

	/**
	 * Remove an edge from the graph.
	 *
	 * @param  edge                the edge to remove
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge identifier
	 */
	void removeEdge(E edge);

	/**
	 * Remove all the edges of a vertex.
	 *
	 * @param  vertex                a vertex in the graph
	 * @throws NoSuchVertexException if {@code vertex} is not a valid vertex identifier
	 */
	default void removeEdgesOf(V vertex) {
		removeOutEdgesOf(vertex);
		removeInEdgesOf(vertex);
	}

	/**
	 * Remove all edges whose source is {@code source}.
	 *
	 * @param  source                a vertex in the graph
	 * @throws NoSuchVertexException if {@code source} is not a valid vertex identifier
	 */
	default void removeOutEdgesOf(V source) {
		for (EdgeIter<V, E> eit = outEdges(source).iterator(); eit.hasNext();) {
			eit.next();
			eit.remove();
		}
	}

	/**
	 * Remove all edges whose target is {@code target}.
	 *
	 * @param  target                a vertex in the graph
	 * @throws NoSuchVertexException if {@code target} is not a valid vertex identifier
	 */
	default void removeInEdgesOf(V target) {
		for (EdgeIter<V, E> eit = inEdges(target).iterator(); eit.hasNext();) {
			eit.next();
			eit.remove();
		}
	}

	/**
	 * Reverse an edge by switching its source and target.
	 *
	 * <p>
	 * If the graph is undirected, this method does nothing.
	 *
	 * @param  edge                an existing edge in the graph
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge identifier
	 */
	void reverseEdge(E edge);

	/**
	 * Get the source vertex of an edge.
	 *
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of the edge, but always other end-point
	 * than {@link #edgeTarget(Object)} returns.
	 *
	 * @param  edge                the edge identifier
	 * @return                     the edge source vertex
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge identifier
	 */
	V edgeSource(E edge);

	/**
	 * Get the target vertex of an edge.
	 *
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of the edge, but always the other
	 * end-point than {@link #edgeSource(Object)} returns.
	 *
	 * @param  edge                the edge identifier
	 * @return                     the edge target vertex
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge identifier
	 */
	V edgeTarget(E edge);

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
	 * @throws IllegalArgumentException if {@code endpoint} is not an endpoint of the edge
	 */
	V edgeEndpoint(E edge, V endpoint);

	/**
	 * Clear the graph completely by removing all vertices and edges.
	 *
	 * <p>
	 * This function might be used to reuse an already allocated graph object.
	 *
	 * <p>
	 * Note that this function also clears any weights associated with the vertices or edges.
	 */
	void clear();

	/**
	 * Remove all the edges from the graph.
	 *
	 * <p>
	 * Note that this function also clears any weights associated with the edges.
	 */
	void clearEdges();

	/**
	 * Get the vertices weights of some key.
	 *
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
	 * Add a new weights container associated with the vertices of this graph.
	 *
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated (when vertices
	 * are added or removed). To create an external weights container, for example in cases the graph is a user input
	 * and we are not allowed to modify it, use {@link Weights#createExternalVerticesWeights(Graph, Class)}.
	 *
	 * <pre> {@code
	 * Graph<String, Int> g = ...;
	 * g.newVertex("Alice");
	 * g.newVertex("Bob");
	 *
	 * Weights<String> names = g.addVerticesWeights("surname", String.class);
	 * names.set("Alice", "Miller");
	 * names.set("Bob", "Jones");
	 *
	 * WeightsInt ages = g.addVerticesWeights("age", int.class);
	 * ages.set("Alice", 42);
	 * ages.set("Bob", 35);
	 * }</pre>
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
	 * Add a new weights container associated with the vertices of this graph with default value.
	 *
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated. To create an
	 * external weights container, for example in cases the graph is a user input we are not allowed to modify it, use
	 * {@link Weights#createExternalVerticesWeights(Graph, Class, Object)}.
	 *
	 * <pre> {@code
	 * Graph<String, Int> g = ...;
	 * g.newVertex("Alice");
	 * g.newVertex("Bob");
	 * g.newVertex("Charlie");
	 *
	 * Weights<String> names = g.addVerticesWeights("name", String.class, "Unknown");
	 * names.set("Alice", "Miller");
	 * names.set("Bob", "Jones");
	 *
	 * assert "Miller".equals(names.get("Alice"))
	 * assert "Jones".equals(names.get("Bob"))
	 * assert "Unknown".equals(names.get("Charlie"))
	 * }</pre>
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
	 * Remove a weight type associated with the vertices of the graph.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key the key of the weights
	 */
	void removeVerticesWeights(String key);

	/**
	 * Get the keys of all the associated vertices weights.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
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
	 * Add a new weights container associated with the edges of this graph.
	 *
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated. To create an
	 * external weights container, for example in cases the graph is a user input you are not allowed to modify it, use
	 * {@link Weights#createExternalEdgesWeights(Graph, Class)}.
	 *
	 * <pre> {@code
	 * Graph<String, Integer> g = ...;
	 * g.addVertex("Berlin");
	 * g.addVertex("Leipzig");
	 * g.addVertex("Dresden");
	 * g.addEdge("Berlin", "Leipzig", 9);
	 * g.addEdge("Berlin", "Dresden", 13);
	 *
	 * Weights<String> roadTypes = g.addEdgesWeights("roadType", String.class);
	 * roadTypes.set(9, "Asphalt");
	 * roadTypes.set(13, "Gravel");
	 *
	 * WeightsDouble roadLengths = g.addEdgesWeights("roadLength", double.class);
	 * roadLengths.set(9, 42);
	 * roadLengths.set(13, 35);
	 * }</pre>
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
	 * Add a new weights container associated with the edges of this graph with default value.
	 *
	 * <p>
	 * The created weights will be bounded to this graph, and will be updated when the graph is updated. To create an
	 * external weights container, for example in cases the graph is a user input we are not allowed to modify it, use
	 * {@link Weights#createExternalEdgesWeights(Graph, Class, Object)}.
	 *
	 * <pre> {@code
	 * Graph<String, Integer> g = ...;
	 * g.addVertex("Berlin");
	 * g.addVertex("Leipzig");
	 * g.addVertex("Dresden");
	 * g.addEdge("Berlin", "Leipzig", 9);
	 * g.addEdge("Berlin", "Dresden", 13);
	 * g.addEdge("Dresden", "Leipzig", 14);
	 *
	 * Weights<String> roadTypes = g.addEdgesWeights("roadType", String.class, "Unknown");
	 * roadTypes.set(9, "Asphalt");
	 * roadTypes.set(13, "Gravel");
	 *
	 * assert "Asphalt".equals(names.get(9))
	 * assert "Gravel".equals(names.get(13))
	 * assert "Unknown".equals(names.get(14))
	 * }</pre>
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
	 * Remove a weight type associated with the edges of the graph.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key the key of the weights
	 */
	void removeEdgesWeights(String key);

	/**
	 * Get the keys of all the associated edges weights.
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated edges weights
	 */
	Set<String> getEdgesWeightsKeys();

	/**
	 * Checks whether the graph is directed.
	 *
	 * @return {@code true} if the graph is directed, else {@code false}.
	 */
	boolean isDirected();

	/**
	 * Checks whether self edges are supported.
	 *
	 * <p>
	 * Self edges are edges with the same source and target, namely a vertex with an edge to itself.
	 *
	 * @return {@code true} if the graph support self edges, else {@code false}.
	 */
	boolean isAllowSelfEdges();

	/**
	 * Checks whether parallel edges are supported.
	 *
	 * <p>
	 * Parallel edges are multiple edges with identical source and target.
	 *
	 * @return {@code true} if the graph support parallel edges, else {@code false}.
	 */
	boolean isAllowParallelEdges();

	/**
	 * Get an Index graph view of this graph.
	 *
	 * <p>
	 * The returned {@link IndexGraph} is a graph in which the identifiers of the vertices are always
	 * {@code (0,1,2, ...,verticesNum-1)}, and the identifiers of the edges are always {@code (0,1,2, ...,edgesNum-1)}.
	 * To maintain this, the index graph implementation may rename existing vertices or edges along the graph lifetime.
	 * This rename behavior is less user friendly, but allow for high performance boost as no hash tables are needed, a
	 * simple array or bitmap can be used to map each vertex/edge to a value/weight/flag. See {@link IndexGraph} for
	 * more information. The {@link IndexGraph} should not be used in scenarios where performance does not matter.
	 *
	 * <p>
	 * The returned graph is a view, namely a graph that will contain the same vertices and edges (with different
	 * {@code int} identifiers), and the same associated weights, that is automatically updated when the original graph
	 * is updated, but not visa versa. The index graph returned <b>should not be modified directly</b> by
	 * adding/removing vertices/edges/weights, use the enclosing graph instead.
	 *
	 * <p>
	 * If this graph is an Index graph, this method returns this graph.
	 *
	 * @return an {@link IndexGraph} view of this graph
	 */
	IndexGraph indexGraph();

	/**
	 * Get the index-id vertices mapping of this graph.
	 *
	 * <p>
	 * A regular graph contains vertices and edges which are identified by a fixed {@code int} IDs. An
	 * {@link IndexGraph} view is provided by the {@link #indexGraph()} method, which is a graph in which all methods
	 * are accessed with <b>indices</b> rather than fixed IDs. This method expose the mapping between the indices and
	 * the fixed IDs of the graph vertices.
	 *
	 * <p>
	 * Note that the mapping may change during the graph lifetime, as vertices are added and removed from the graph, and
	 * a regular graph IDs are fixed, while a index graph indices are always {@code (0,1,2, ...,verticesNum-1)}. The
	 * returned mapping object will be updated automatically in such cases.
	 *
	 * @return a mapping that map vertices IDs to vertices indices
	 */
	IndexIdMap<V> indexGraphVerticesMap();

	/**
	 * Get the index-id edges mapping of this graph.
	 *
	 * <p>
	 * A regular graph contains vertices and edges which are identified by a fixed {@code int} IDs. An
	 * {@link IndexGraph} view is provided by the {@link #indexGraph()} method, which is a graph in which all methods
	 * are accessed with <b>indices</b> rather than fixed IDs. This method expose the mapping between the indices and
	 * the fixed IDs of the graph edges.
	 *
	 * <p>
	 * Note that the mapping may change during the graph lifetime, as edges are added and removed from the graph, and a
	 * regular graph IDs are fixed, while a index graph indices are always {@code (0,1,2, ...,edgesNum-1)}. The returned
	 * mapping object will be updated automatically in such cases.
	 *
	 * @return a mapping that map edges IDs to edges indices
	 */
	IndexIdMap<E> indexGraphEdgesMap();

	/**
	 * Create a copy of this graph, with the same vertices and edges, without copying weights.
	 *
	 * <p>
	 * An identical copy of this graph will be created, with the same vertices, edges, capabilities (inclusive) such as
	 * self edges and parallel edges support, without copying the vertices/edges weights. The returned graph will always
	 * be modifiable, with no side affects on the original graph.
	 *
	 * @return an identical copy of this graph, with the same vertices and edges, without this graph weights
	 */
	default Graph<V, E> copy() {
		return copy(false, false);
	}

	/**
	 * Create a copy of this graph, with the same vertices and edges, with/without copying weights.
	 *
	 * <p>
	 * An identical copy of this graph will be created, with the same vertices, edges, capabilities (inclusive) such as
	 * self edges and parallel edges support, with/without copying the vertices/edges weights. The returned graph will
	 * always be modifiable, with no side affects on the original graph.
	 *
	 * <p>
	 * Note that although {@code g.equals(g.copy())} is always {@code true} if {@code copyWeights} is {@code true},
	 * there is no guarantee that {@code g.indexGraph().equals(g.copy().indexGraph())}. Namely, when the graph is
	 * copied, new indices may be assigned to the vertices and edges.
	 *
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied to the new graph
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied to the new graph
	 * @return                     an identical copy of the given graph, with the same vertices and edges, with/without
	 *                             this graph weights
	 */
	default Graph<V, E> copy(boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return GraphFactory.newFrom(this).newCopyOf(this, copyVerticesWeights, copyEdgesWeights);
	}

	/**
	 * Create an immutable copy of this graph, with the same vertices and edges, without copying weights.
	 *
	 * <p>
	 * An identical copy of this graph will be created, with the same vertices and edges, without copying the
	 * vertices/edges weights. The returned graph will be immutable, and no vertices/edges/weights can be added or
	 * removed from it.
	 *
	 * <p>
	 * A more compact and efficient representation may be used for the graph, if its known that it will not be changed
	 * in the future. It may be more efficient to create an immutable copy of a graph and pass the copy to algorithms
	 * instead of using the original graph.
	 *
	 * <p>
	 * Note that although {@code g.equals(g.immutableCopy())} is always {@code true}, there is no guarantee that
	 * {@code g.indexGraph().equals(g.immutableCopy().indexGraph())}. Namely, when the graph is copied, new indices may
	 * be assigned to the vertices and edges.
	 *
	 * @return an immutable copy of this graph, with the same vertices and edges, without this graph weights
	 */
	default Graph<V, E> immutableCopy() {
		return GraphBuilder.newFrom(this).build();
	}

	/**
	 * Create an immutable copy of this graph, with the same vertices and edges, with/without copying weights.
	 *
	 * <p>
	 * An identical copy of this graph will be created, with the same vertices and edges, with/without copying the
	 * vertices/edges weights. The returned graph will be immutable, and no vertices/edges/weights can be added or
	 * removed from it.
	 *
	 * <p>
	 * A more compact and efficient representation may be used for the graph, if its known that it will not be changed
	 * in the future. It may be more efficient to create an immutable copy of a graph and pass the copy to algorithms
	 * instead of using the original graph.
	 *
	 * <p>
	 * Note that although {@code g.equals(g.immutableCopy())} is always {@code true} if {@code copyWeights} is
	 * {@code true}, there is no guarantee that {@code g.indexGraph().equals(g.immutableCopy().indexGraph())}. Namely,
	 * when the graph is copied, new indices may be assigned to the vertices and edges.
	 *
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied to the new graph
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied to the new graph
	 * @return                     an immutable copy of this graph, with the same vertices and edges, with/without this
	 *                             graph weights
	 */
	default Graph<V, E> immutableCopy(boolean copyVerticesWeights, boolean copyEdgesWeights) {
		IndexIdMap<V> viMap = indexGraphVerticesMap();
		IndexIdMap<E> eiMap = indexGraphEdgesMap();
		if (isDirected()) {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph =
					GraphCsrDirectedReindexed.newInstance(indexGraph(), copyVerticesWeights, copyEdgesWeights);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			return new GraphImpl<>(iGraph, viMap, eiMap, vReIndexing.orElse(null), eReIndexing.orElse(null));
		} else {
			IndexGraph iGraph = new GraphCsrUndirected(indexGraph(), copyVerticesWeights, copyEdgesWeights);
			return new GraphImpl<>(iGraph, viMap, eiMap, null, null);
		}
	}

	/**
	 * Get an immutable view of this graph.
	 *
	 * <p>
	 * This method return a view of this graph, namely a Graph that contains the same vertices, edges and weights, that
	 * is automatically updated when the original graph is updated. The view is immutable, namely all operations that
	 * modify the graph will throw {@link UnsupportedOperationException}.
	 *
	 * @return an immutable view of this graph
	 */
	default Graph<V, E> immutableView() {
		return Graphs.immutableView(this);
	}

	/**
	 * Get a reversed view of this graph.
	 *
	 * <p>
	 * This method return a view of this graph, namely a Graph that contains the same vertices, edges and weights, that
	 * is automatically updated when the original graph is updated and vice versa. The view is reversed, namely each
	 * source and target vertices of each edge are swapped.
	 *
	 * <p>
	 * Note that modifying the returned view will change the original graph.
	 *
	 * @return a reversed view of this graph
	 */
	default Graph<V, E> reverseView() {
		return Graphs.reverseView(this);
	}

	/**
	 * Get an undirected view of this (directed) graph.
	 *
	 * <p>
	 * This method return a view of this graph, namely a Graph that contains the same vertices, edges and weights, that
	 * is automatically updated when the original graph is updated and vice versa. The view is undirected, namely each
	 * directed edge \((u,v)\) will exist in all the sets {@code g.outEdges(u)}, {@code g.inEdges(u)},
	 * {@code g.outEdges(v)} and {@code g.inEdges(u)}. The view will contain the same number of edges as this graph.
	 *
	 * <p>
	 * The returned view will return {@code true} for {@link #isAllowParallelEdges()} even if the original graph <b>does
	 * not</b> support parallel edges. This is because the original graph could have both \((u,v)\) in \((v,u)\) without
	 * violating the parallel edges constraint, but the view will treat them as parallel edges as the direction is
	 * 'forgotten'.
	 *
	 * <p>
	 * If this graph is undirected, this function return the graph itself.
	 *
	 * @return an undirected view of this graph
	 */
	default Graph<V, E> undirectedView() {
		return Graphs.undirectedView(this);
	}

	/**
	 * Create a new graph that is a subgraph of this graph.
	 *
	 * <p>
	 * If {@code edges} is {@code null}, then the created graph will be an induced subgraph of this graph, namely an
	 * induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' = \{\{u,v\}
	 * \mid u,v \in V', \{u,v\} \in E\}\). {@code vertices} must not be {@code null} in this case.
	 *
	 * <p>
	 * If {@code vertices} is {@code null}, then {@code edges} must not be {@code null}, and the sub graph will contain
	 * all the vertices which are either a source or a target of an edge in {@code edges}.
	 *
	 * <p>
	 * The created graph will have the same type (directed/undirected) as this graph. The vertices and edges of the
	 * created graph will be a subset of the vertices and edges of this graph.
	 *
	 * <p>
	 * The weights of both vertices and edges will not be copied to the new sub graph. For more flexible sub graph
	 * creation, see {@link Graphs#subGraph(Graph, Collection, Collection, boolean, boolean)}.
	 *
	 * @param  vertices             the vertices of the sub graph, if {@code null} then {@code edges} must not be
	 *                                  {@code null} and the vertices of the sub graph will be all the vertices which
	 *                                  are either a source or a target of an edge in {@code edges}
	 * @param  edges                the edges of the sub graph, if {@code null} then {@code vertices} must not be
	 *                                  {@code null} and the sub graph will be an induced subgraph of this graph
	 * @return                      a new graph that is a subgraph of this graph
	 * @throws NullPointerException if both {@code vertices} and {@code edges} are {@code null}
	 */
	default Graph<V, E> subGraphCopy(Collection<V> vertices, Collection<E> edges) {
		return Graphs.subGraph(this, vertices, edges);
	}

	/**
	 * Create a new undirected empty graph.
	 *
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link GraphFactory}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new undirected empty graph
	 */
	static <V, E> Graph<V, E> newUndirected() {
		return GraphFactory.<V, E>newUndirected().newGraph();
	}

	/**
	 * Create a new directed empty graph.
	 *
	 * <p>
	 * The returned graph will be implemented using the default implementation. For more control over the graph details,
	 * see {@link GraphFactory}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new directed empty graph
	 */
	static <V, E> Graph<V, E> newDirected() {
		return GraphFactory.<V, E>newDirected().newGraph();
	}

}
