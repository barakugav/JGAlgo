package com.jgalgo;

import java.util.Collection;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A discrete graph with vertices and edges.
 * <p>
 * A graph consist of a finite set of vertices {@code V} and edges {@code E}.
 * Vertices are some objects, and edges are connections between the vertices,
 * for example vertices can be cities and edges could be the roads between them.
 * Edges could be directed or undirected. Weights may be assigned to vertices
 * or edges, for example the length of a road might be a weight of an edge.
 * Than, questions such as "what is the shortest path between two cities?" might
 * be answered using graph algorithms.
 * <p>
 * Each edge {@code e=(u, v)} in the graph has a <i>source</i>, {@code u}, and a
 * <i>target</i> {@code v}. In undirected graphs the 'source' and 'target' can
 * be switched, as the edge is not directed, and we treat the source and target
 * as interchangeable <i>end points</i>. If an edge {@code (u, v)} exist in the
 * graph, we say the vertices {@code u} and {@code v} and <i>neighbors</i>, or
 * <i>adjacent</i>. The edges are usually stored in some list for each vertex,
 * allowing efficient iteration of its edges. The <i>degree</i> of an edges is
 * the number of its edges. In directed graph, we have both <i>in-degree</i> and
 * <i>out-degree</i>, which are the number of edges going in and out the vertex,
 * respectively.
 * <p>
 * Vertices can be added or removed. When a vertex {@code v} is removed, all the
 * edges with {@code v} as one of their end points are removed as well. Edge can
 * be added as connection to existing vertices, or removed.
 * <p>
 * Each vertex in the graph is identified by a unique non negative int ID. The
 * set of vertices in the graph is always {@code (0,1,2, ...,verticesNum-1)}.
 * To maintain this, the graph implementation may rename existing vertices when
 * the user remove a vertex, see {@link #getVerticesIDStrategy()}. Similar to
 * vertices, each edge in the graph is identified by a unique non negative int
 * ID. In contrast to the vertices IDs, it's not specified how the graph
 * implementation assign new IDs to added edges, or if it rename some of them
 * when the user remove an edge, see {@link #getEdgesIDStrategy()}.
 * <p>
 * The number of vertices, {@code |V|}, is usually denoted as {@code n} in
 * algorithms time and space complexities. And similarly, the number of edges,
 * {@code |E|}, is usually denoted as {@code m}.
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * DiGraph g = new GraphArrayDirected();
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
 * SSSP ssspAlgo = new SSSPDijkstra();
 * SSSP.Result ssspRes = ssspAlgo.calcDistances(g, w, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPathTo(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (IntIterator it = ssspRes.getPathTo(v3).iterator(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see DiGraph
 * @see UGraph
 * @author Barak Ugav
 */
public interface Graph {

	/**
	 * Get the set of all vertices of the graph.
	 *
	 * <p>
	 * Each vertex in the graph is identified by a unique non negative int ID,
	 * determined by {@link #getVerticesIDStrategy()}. The returned set is a set of
	 * all these identifiers, and its size is equivalent to the number of vertices
	 * in the graph.
	 *
	 * @return a set containing all IDs of the graph vertices
	 */
	IntSet vertices();

	/**
	 * Get the set of all edges of the graph.
	 *
	 * <p>
	 * Each edge in the graph is identified by a unique non negative int ID,
	 * determined by {@link #getEdgesIDStrategy()}. The returned set is a set of all
	 * these identifiers, and its size is equivalent to the number of edges in the
	 * graph.
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
	 * After removing a vertex, the vertices ID strategy may rename other vertices
	 * identifiers to maintain its invariants, see {@link #getVerticesIDStrategy()}.
	 * Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 * It may be more convenient to remove all edges of a vertex and ignore it,
	 * instead of actually removing it and dealing with IDs renames, but that
	 * depends on the specific use case.
	 *
	 * @see IDStrategy
	 *
	 * @param v the vertex identifier to remove
	 * @throws IndexOutOfBoundsException if {@code v} is not a valid vertex
	 *                                   identifier
	 */
	void removeVertex(int v);

	/**
	 * Get the edges whose source is {@code u}.
	 *
	 * <p>
	 * Get an edge iterator that iterate over all edges whose source is {@code u}.
	 * In case the graph is undirected, the iterator will iterate over edges whose
	 * {@code u} is one of their end points.
	 *
	 * @param u a source vertex
	 * @return an iterator of all the edges whose source is u
	 * @throws IndexOutOfBoundsException if {@code u} is not a valid vertex
	 *                                   identifier
	 */
	EdgeIter edgesOut(int u);

	/**
	 * Get the edges whose target is {@code v}.
	 *
	 * <p>
	 * Get an edge iterator that iterate over all edges whose target is {@code v}.
	 * In case the graph is undirected, the iterator will iterate over edges whose
	 * {@code v} is one of their end points.
	 *
	 * @param v a target vertex
	 * @return an iterator of all the edges whose target is {@code v}
	 * @throws IndexOutOfBoundsException if {@code v} is not a valid vertex
	 *                                   identifier
	 */
	EdgeIter edgesIn(int v);

	/**
	 * Get the edge whose source is {@code u} and target is {@code v}.
	 *
	 * <p>
	 * If the graph is not directed, the return edge is an edge that its end-points
	 * are {@code u} and {@code v}.
	 *
	 * <p>
	 * In case there are multiple (parallel) edges between {@code u} and {@code v},
	 * a single arbitrary one is returned.
	 *
	 * @param u a source vertex
	 * @param v a target vertex
	 * @return id of the edge or {@code -1} if no such edge exists
	 * @throws IndexOutOfBoundsException if {@code u} or {@code v} are not valid
	 *                                   vertices identifiers
	 */
	default int getEdge(int u, int v) {
		for (EdgeIter it = edgesOut(u); it.hasNext();) {
			int e = it.nextInt();
			if (it.v() == v)
				return e;
		}
		return -1;
	}

	/**
	 * Get the edges whose source is {@code u} and target is {@code v}.
	 *
	 * @param u a source vertex
	 * @param v a target vertex
	 * @return an iterator of all the edges whose source is {@code u} and target is
	 *         {@code v}
	 * @throws IndexOutOfBoundsException if {@code u} or {@code v} are not valid
	 *                                   vertices identifiers
	 */
	EdgeIter getEdges(int u, int v);

	/**
	 * Add a new edge to the graph.
	 *
	 * @param u a source vertex
	 * @param v a target vertex
	 * @return the new edge identifier
	 * @throws IndexOutOfBoundsException if {@code u} or {@code v} are not valid
	 *                                   vertices identifiers
	 */
	int addEdge(int u, int v);

	/**
	 * Remove an edge from the graph.
	 *
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges
	 * identifiers to maintain its invariants, see {@link #getEdgesIDStrategy()}.
	 * Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param edge the edge identifier
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid vertex
	 *                                   identifier
	 */
	void removeEdge(int edge);

	/**
	 * Remove all the edges of a vertex.
	 *
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges
	 * identifiers to maintain its invariants, see {@link #getEdgesIDStrategy()}.
	 * Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param u a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code u} is not a valid vertex
	 *                                   identifier
	 */
	default void removeEdgesAll(int u) {
		removeEdgesAllOut(u);
		removeEdgesAllIn(u);
	}

	/**
	 * Remove all edges whose source is {@code u}.
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges
	 * identifiers to maintain its invariants, see {@link #getEdgesIDStrategy()}.
	 * Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param u a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code u} is not a valid vertex
	 *                                   identifier
	 */
	default void removeEdgesAllOut(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Remove all edges whose target is {@code v}.
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges
	 * identifiers to maintain its invariants, see {@link #getEdgesIDStrategy()}.
	 * Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @param v a vertex in the graph
	 * @throws IndexOutOfBoundsException if {@code v} is not a valid vertex
	 *                                   identifier
	 */
	default void removeEdgesAllIn(int v) {
		for (EdgeIter eit = edgesIn(v); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Get the source vertex of an edge.
	 *
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of
	 * the edge, but always other end-point than {@link #edgeTarget(int)} returns.
	 *
	 * @param edge the edge identifier
	 * @return the edge source vertex
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid vertex
	 *                                   identifier
	 */
	public int edgeSource(int edge);

	/**
	 * Get the target vertex of an edge.
	 *
	 * <p>
	 * If the graph is undirected, this function return an arbitrary end-point of
	 * the edge, but always the other end-point than {@link #edgeSource(int)}
	 * returns.
	 *
	 * @param edge the edge identifier
	 * @return the edge target vertex
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid vertex
	 *                                   identifier
	 */
	public int edgeTarget(int edge);

	/**
	 * Get the other end-point of an edge.
	 *
	 * @param edge     an edge identifier
	 * @param endpoint one of the edge end-point
	 * @return the other end-point of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid vertex
	 *                                   identifier
	 * @throws IllegalArgumentException  if {@code endpoint} is not an endpoint of
	 *                                   the edge
	 */
	default int edgeEndpoint(int edge, int endpoint) {
		int u = edgeSource(edge);
		int v = edgeTarget(edge);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException("Given vertex is not an endpoint of the edge");
		}
	}

	/**
	 * Get the out degree of a source vertex.
	 *
	 * @param u a source vertex
	 * @return the number of edges whose source is u
	 * @throws IndexOutOfBoundsException if {@code u} is not a valid vertex
	 *                                   identifier
	 */
	default int degreeOut(int u) {
		int count = 0;
		for (EdgeIter it = edgesOut(u); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	/**
	 * Get the in degree of a target vertex.
	 *
	 * @param v a target vertex
	 * @return the number of edges whose target is v
	 * @throws IndexOutOfBoundsException if {@code v} is not a valid vertex
	 *                                   identifier
	 */
	default int degreeIn(int v) {
		int count = 0;
		for (EdgeIter it = edgesIn(v); it.hasNext();) {
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
	 * Note that this function also clears any weights associated with the vertices
	 * or edges.
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
	 * @param key some key of the weights, could be anything
	 * @return vertices weights of the key, or null if no container found with the
	 *         specified key
	 * @param <V>        The weight data type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public <V, WeightsT extends Weights<V>> WeightsT verticesWeight(Object key);

	/**
	 * Add a new weights container associated with the vertices of this graph.
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
	 * @param key  some key of the weights, could be anything
	 * @param type the type of the weights, used for primitive types weights
	 * @return a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the
	 *                                  same key already exists in the graph
	 * @param <V>        The weight data type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type);

	/**
	 * Add a new weights container associated with the vertices of this graph with
	 * default value.
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
	 * @param key    some key of the weights, could be anything
	 * @param type   the type of the weights, used for primitive types weights
	 * @param defVal default value use for the weights container
	 * @return a new weights container
	 * @throws IllegalArgumentException if a vertices weights container with the
	 *                                  same key already exists in the graph
	 * @param <V>        The weight data type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
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
	 * Get all vertices weights.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @return all vertices weights
	 */
	public Collection<Weights<?>> getVerticesWeights();

	/**
	 * Get the edges weights of some key.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key some key of the weights, could be anything
	 * @return edges weights of the key, or null if no container found with the
	 *         specified key
	 * @param <E>        The weight data type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public <E, WeightsT extends Weights<E>> WeightsT edgesWeight(Object key);

	/**
	 * Add a new weights container associated with the edges of this graph.
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
	 * roadType.set(e1, "Asphalt");
	 * roadType.set(e2, "Gravel");
	 *
	 * Weights.Double roadLengths = g.addEdgesWeights("roadLength", double.class);
	 * lengths.set(e1, 42);
	 * lengths.set(e2, 35);
	 * }</pre>
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key  some key of the weights, could be anything
	 * @param type the type of the weights, used for primitive types weights
	 * @return a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same
	 *                                  key already exists in the graph
	 * @param <E>        The weight data type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type);

	/**
	 * Add a new weights container associated with the edges of this graph with
	 * default value.
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
	 * roadType.set(e1, "Asphalt");
	 * roadType.set(e2, "Gravel");
	 *
	 * assert "Asphalt".equals(names.get(e1))
	 * assert "Gravel".equals(names.get(e2))
	 * assert "Unknown".equals(names.get(e3))
	 * }</pre>
	 *
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @param key    some key of the weights, could be anything
	 * @param type   the type of the weights, used for primitive types weights
	 * @param defVal default value use for the weights container
	 * @return a new weights container
	 * @throws IllegalArgumentException if a edges weights container with the same
	 *                                  key already exists in the graph
	 * @param <E>        The weight data type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
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
	 * Get all edges weights.
	 * <p>
	 * See {@link Weights} for a complete documentation of the weights containers.
	 *
	 * @return all edges weights
	 */
	public Collection<Weights<?>> getEdgesWeights();

	/**
	 * Get the ID strategy of the vertices of the graph.
	 *
	 * <p>
	 * Each vertex in the graph is identified by a unique non negative int ID, which
	 * is determined by some strategy. Only {@link IDStrategy.Continues} is
	 * supported for vertices, which ensure that at all times the vertices IDs are
	 * {@code 0,1,..., verticesNum-1}, and it might rename some vertices when a
	 * vertex is removed to maintain this invariant. This rename can be subscribed
	 * using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @see IDStrategy
	 *
	 * @return the vertices IDs strategy
	 */
	public IDStrategy.Continues getVerticesIDStrategy();

	/**
	 * Get the ID strategy of the edges of the graph.
	 *
	 * <p>
	 * Each edge in the graph is identified by a unique non negative int ID, which
	 * is determined by some strategy. For example, {@link IDStrategy.Continues}
	 * ensure that at all times the edges IDs are {@code 0,1,..., edgesNum-1}, and
	 * it might rename some edges when an edge is removed to maintain this
	 * invariant. This rename can be subscribed using
	 * {@link IDStrategy#addIDSwapListener}. Another option for an ID strategy is
	 * {@link IDStrategy.Fixed} which ensure once an edge is assigned an ID, it will
	 * not change. There might be some performance differences between different ID
	 * strategies.
	 *
	 * @see IDStrategy
	 *
	 * @return the edges IDs strategy
	 */
	public IDStrategy getEdgesIDStrategy();

	/**
	 * Get the {@linkplain GraphCapabilities capabilities} of this graph.
	 *
	 * @return a {@link GraphCapabilities} object describing what this graph support
	 *         and what not.
	 * @see GraphCapabilities
	 */
	public GraphCapabilities getCapabilities();

}
