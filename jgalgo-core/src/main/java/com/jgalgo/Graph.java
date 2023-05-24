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
 *
 * <p>
 * A graph consist of a finite set of vertices \(V\) and edges \(E\). Vertices are some objects, and edges are
 * connections between the vertices, for example vertices can be cities and edges could be the roads between them. Edges
 * could be directed or undirected. Weights may be assigned to vertices or edges, for example the length of a road might
 * be a weight of an edge. Than, questions such as "what is the shortest path between two cities?" might be answered
 * using graph algorithms.
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
 * <i>directed</i> namely an edge \(e(u, v)\) will appear in the iteration of {@code edgesOut(u)} and {@code edgesIn(v)}
 * and will not appear in the iteration of {@code edgesOut(v)} and {@code edgesIn(u)}. In an undirected graph, the edges
 * are undirected, namely an edge \(e(u, v)\) will appear in the iteration of {@code edgesOut(u)}, {@code edgesIn(v)},
 * {@code edgesOut(v)} and {@code edgesIn(u)}. Also {@link #edgesOut(int)} and {@link #edgesIn(int)} are equivalent for
 * the same vertex, same for {@link #degreeIn(int)} and {@link #degreeOut(int)}, and similarly
 * {@link #removeEdgesOf(int)}, {@link #removeEdgesInOf(int)} and {@link #removeEdgesOutOf(int)}. To check if a graph is
 * directed or not, use the {@link #getCapabilities()} method.
 * <p>
 * Each vertex in the graph is identified by a unique non negative int ID. The set of vertices in the graph is always
 * {@code (0,1,2, ...,verticesNum-1)}. To maintain this, the graph implementation may rename existing vertices when the
 * user remove a vertex, see {@link #getVerticesIDStrategy()}. Similar to vertices, each edge in the graph is identified
 * by a unique non negative int ID. In contrast to the vertices IDs, it's not specified how the graph implementation
 * assign new IDs to added edges, or if it rename some of them when the user remove an edge, see
 * {@link #getEdgesIDStrategy()}.
 * <p>
 * The number of vertices, \(|V|\), is usually denoted as \(n\) in algorithms time and space complexities. And
 * similarly, the number of edges, \(|E|\), is usually denoted as \(m\).
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * Graph g = GraphBuilder.newDirected().build();
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
 * SSSP ssspAlgo = SSSP.newBuilder().build();
 * SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (IntIterator it = ssspRes.getPath(v3).iterator(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    GraphBuilder
 * @see    GraphCapabilities
 * @author Barak Ugav
 */
public interface Graph {

	/**
	 * Get the set of all vertices of the graph.
	 *
	 * <p>
	 * Each vertex in the graph is identified by a unique non negative int ID, determined by
	 * {@link #getVerticesIDStrategy()}. The returned set is a set of all these identifiers, and its size is equivalent
	 * to the number of vertices in the graph.
	 *
	 * @return a set containing all IDs of the graph vertices
	 */
	IntSet vertices();

	/**
	 * Get the set of all edges of the graph.
	 *
	 * <p>
	 * Each edge in the graph is identified by a unique non negative int ID, determined by
	 * {@link #getEdgesIDStrategy()}. The returned set is a set of all these identifiers, and its size is equivalent to
	 * the number of edges in the graph.
	 *
	 * @return a set containing all IDs of the graph edges
	 */
	IntSet edges();

	/**
	 * Add a new vertex to the graph.
	 *
	 * @return the new vertex identifier
	 */
	int addVertex();

	/**
	 * Remove a vertex and all its edges from the graph.
	 *
	 * <p>
	 * After removing a vertex, the vertices ID strategy may rename other vertices identifiers to maintain its
	 * invariants, see {@link #getVerticesIDStrategy()}. Theses renames can be subscribed using
	 * {@link IDStrategy#addIDSwapListener}. It may be more convenient to remove all edges of a vertex and ignore it,
	 * instead of actually removing it and dealing with IDs renames, but that depends on the specific use case.
	 *
	 * @see                              IDStrategy
	 * @param  vertex                    the vertex identifier to remove
	 * @throws IndexOutOfBoundsException if \(v\) is not a valid vertex identifier
	 */
	void removeVertex(int vertex);

	/**
	 * Get the edges whose source is \(u\).
	 *
	 * <p>
	 * Get an edge iterator that iterate over all edges whose source is \(u\). In case the graph is undirected, the
	 * iterator will iterate over edges whose \(u\) is one of their end points.
	 *
	 * @param  source                    a source vertex
	 * @return                           an iterator of all the edges whose source is u
	 * @throws IndexOutOfBoundsException if \(u\) is not a valid vertex identifier
	 */
	EdgeIter edgesOut(int source);

	/**
	 * Get the edges whose target is \(v\).
	 *
	 * <p>
	 * Get an edge iterator that iterate over all edges whose target is \(v\). In case the graph is undirected, the
	 * iterator will iterate over edges whose \(v\) is one of their end points.
	 *
	 * @param  target                    a target vertex
	 * @return                           an iterator of all the edges whose target is \(v\)
	 * @throws IndexOutOfBoundsException if \(v\) is not a valid vertex identifier
	 */
	EdgeIter edgesIn(int target);

	/**
	 * Get the edge whose source is \(u\) and target is \(v\).
	 *
	 * <p>
	 * If the graph is not directed, the return edge is an edge that its end-points are \(u\) and \(v\).
	 *
	 * <p>
	 * In case there are multiple (parallel) edges between \(u\) and \(v\), a single arbitrary one is returned.
	 *
	 * @param  source                    a source vertex
	 * @param  target                    a target vertex
	 * @return                           id of the edge or {@code -1} if no such edge exists
	 * @throws IndexOutOfBoundsException if \(u\) or \(v\) are not valid vertices identifiers
	 */
	default int getEdge(int source, int target) {
		for (EdgeIter it = edgesOut(source); it.hasNext();) {
			int e = it.nextInt();
			if (it.target() == target)
				return e;
		}
		return -1;
	}

	/**
	 * Get the edges whose source is \(u\) and target is \(v\).
	 *
	 * @param  source                    a source vertex
	 * @param  target                    a target vertex
	 * @return                           an iterator of all the edges whose source is \(u\) and target is \(v\)
	 * @throws IndexOutOfBoundsException if \(u\) or \(v\) are not valid vertices identifiers
	 */
	EdgeIter getEdges(int source, int target);

	/**
	 * Add a new edge to the graph.
	 *
	 * @param  source                    a source vertex
	 * @param  target                    a target vertex
	 * @return                           the new edge identifier
	 * @throws IndexOutOfBoundsException if \(u\) or \(v\) are not valid vertices identifiers
	 */
	int addEdge(int source, int target);

	/**
	 * Remove an edge from the graph.
	 *
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIDStrategy()}. Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param  edge                      the edge identifier
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	void removeEdge(int edge);

	/**
	 * Remove all the edges of a vertex.
	 *
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIDStrategy()}. Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param  vertex                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if \(u\) is not a valid vertex identifier
	 */
	default void removeEdgesOf(int vertex) {
		removeEdgesOutOf(vertex);
		removeEdgesInOf(vertex);
	}

	/**
	 * Remove all edges whose source is \(u\).
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIDStrategy()}. Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param  source                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if \(u\) is not a valid vertex identifier
	 */
	default void removeEdgesOutOf(int source) {
		for (EdgeIter eit = edgesOut(source); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Remove all edges whose target is \(v\).
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIDStrategy()}. Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param  target                    a vertex in the graph
	 * @throws IndexOutOfBoundsException if \(v\) is not a valid vertex identifier
	 */
	default void removeEdgesInOf(int target) {
		for (EdgeIter eit = edgesIn(target); eit.hasNext();) {
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
	public void reverseEdge(int edge);

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
	public int edgeSource(int edge);

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
	public int edgeTarget(int edge);

	/**
	 * Get the other end-point of an edge.
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
	 * Get the out degree of a source vertex.
	 *
	 * @param  source                    a source vertex
	 * @return                           the number of edges whose source is u
	 * @throws IndexOutOfBoundsException if \(u\) is not a valid vertex identifier
	 */
	default int degreeOut(int source) {
		int count = 0;
		for (EdgeIter it = edgesOut(source); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	/**
	 * Get the in degree of a target vertex.
	 *
	 * @param  target                    a target vertex
	 * @return                           the number of edges whose target is v
	 * @throws IndexOutOfBoundsException if \(v\) is not a valid vertex identifier
	 */
	default int degreeIn(int target) {
		int count = 0;
		for (EdgeIter it = edgesIn(target); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	/**
	 * Clear the graph completely by removing all vertices and edges.
	 *
	 * <p>
	 * This function might be used to reuse an already allocated graph object.
	 * <p>
	 * Note that this function also clears any weights associated with the vertices or edges.
	 */
	public void clear();

	/**
	 * Remove all the edges from the graph.
	 *
	 * <p>
	 * Note that this function also clears any weights associated with the edges.
	 */
	public void clearEdges();

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
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key);

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
	 *
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
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type);

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
	 *
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
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal);

	/**
	 * Remove a weight type associated with the vertices of the graph.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key the key of the weights
	 */
	public void removeVerticesWeights(Object key);

	/**
	 * Get the keys of all the associated vertices weights.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated vertices weights
	 */
	public Set<Object> getVerticesWeightKeys();

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
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key);

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
	 *
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
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type);

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
	 *
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
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal);

	/**
	 * Remove a weight type associated with the edges of the graph.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key the key of the weights
	 */
	public void removeEdgesWeights(Object key);

	/**
	 * Get the keys of all the associated edges weights.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @return the keys of all the associated edges weights
	 */
	public Set<Object> getEdgesWeightsKeys();

	/**
	 * Get the ID strategy of the vertices of the graph.
	 *
	 * <p>
	 * Each vertex in the graph is identified by a unique non negative int ID, which is determined by some strategy.
	 * Only {@link IDStrategy.Continues} is supported for vertices, which ensure that at all times the vertices IDs are
	 * {@code 0,1,..., verticesNum-1}, and it might rename some vertices when a vertex is removed to maintain this
	 * invariant. This rename can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @see    IDStrategy
	 *
	 * @return the vertices IDs strategy
	 */
	public IDStrategy.Continues getVerticesIDStrategy();

	/**
	 * Get the ID strategy of the edges of the graph.
	 *
	 * <p>
	 * Each edge in the graph is identified by a unique non negative int ID, which is determined by some strategy. For
	 * example, {@link IDStrategy.Continues} ensure that at all times the edges IDs are {@code 0,1,..., edgesNum-1}, and
	 * it might rename some edges when an edge is removed to maintain this invariant. This rename can be subscribed
	 * using {@link IDStrategy#addIDSwapListener}. Another option for an ID strategy is {@link IDStrategy.Fixed} which
	 * ensure once an edge is assigned an ID, it will not change. There might be some performance differences between
	 * different ID strategies.
	 *
	 * @see    IDStrategy
	 *
	 * @return the edges IDs strategy
	 */
	public IDStrategy getEdgesIDStrategy();

	/**
	 * Get the {@linkplain GraphCapabilities capabilities} of this graph.
	 *
	 * @return a {@link GraphCapabilities} object describing what this graph support and what not.
	 * @see    GraphCapabilities
	 */
	public GraphCapabilities getCapabilities();

	/**
	 * Create a copy of this graph.
	 * <p>
	 * An identical copy of this graph will be created, with the same vertices, edges, weights and capabilities.
	 *
	 * @return an identical copy of this graph
	 */
	public Graph copy();

}
