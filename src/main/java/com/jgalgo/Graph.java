package com.jgalgo;

import java.util.Collection;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

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
	 * Theses renames can be subscribed using
	 * {@link com.jgalgo.IDStrategy#addIDSwapListener}. It may be more convenient to
	 * remove all edges of a vertex and ignore it, instead of actually removing it
	 * and dealing with IDs renames, but that depends on the specific use case.
	 *
	 * @see com.jgalgo.IDStrategy
	 *
	 * @param v the vertex identifier to remove
	 */
	void removeVertex(int v);

	/**
	 * Get the edges of a vertex u.
	 *
	 * <p>
	 * In case the graph is directed, this function returns the edges which u is
	 * their source vertex.
	 *
	 * @param u a source vertex
	 * @return an iterator of all the edges whose source is u
	 */
	EdgeIter edgesOut(int u);

	EdgeIter edgesIn(int v);

	/**
	 * Get the edge whose source is u and target is v.
	 *
	 * <p>
	 * If the graph is not directed, the return edge is an edge that its end-points
	 * are u,v.
	 *
	 * <p>
	 * In case there are multiple (parallel) edges between u and v, a single
	 * arbitrary one is returned.
	 *
	 * @param u a source vertex
	 * @param v a target vertex
	 * @return id of the edge or -1 if no such edge exists
	 */
	default int getEdge(int u, int v) {
		for (EdgeIter it = edgesOut(u); it.hasNext();) {
			int e = it.nextInt();
			if (it.v() == v)
				return e;
		}
		return -1;
	}

	default EdgeIter getEdges(int u, int v) {
		IntList edges = new IntArrayList();
		for (EdgeIter it = edgesOut(u); it.hasNext();) {
			int e = it.nextInt();
			if (it.v() == v)
				edges.add(e);
		}
		return new EdgeIter() {

			IntIterator it = edges.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int nextInt() {
				return it.nextInt();
			}

			@Override
			public int u() {
				return u;
			}

			@Override
			public int v() {
				return v;
			}
		};
	}

	/**
	 * Add a new edge to the graph.
	 *
	 * @param u a source vertex
	 * @param v a target vertex
	 * @return the new edge identifier
	 */
	int addEdge(int u, int v);

	/**
	 * Remove an edge from the graph.
	 *
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges
	 * identifiers to maintain its invariants, see {@link #getEdgesIDStrategy()}.
	 * Theses renames can be subscribed using
	 * {@link com.jgalgo.IDStrategy#addIDSwapListener}.
	 *
	 * @see com.jgalgo.IDStrategy
	 *
	 * @param edge the edge identifier
	 */
	void removeEdge(int edge);

	/**
	 * Remove all the edges of a vertex u.
	 *
	 * <p>
	 * If the graph is directed, both the in and out edges of the vertex are
	 * removed.
	 *
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges
	 * identifiers to maintain its invariants, see {@link #getEdgesIDStrategy()}.
	 * Theses renames can be subscribed using
	 * {@link com.jgalgo.IDStrategy#addIDSwapListener}.
	 *
	 * @see com.jgalgo.IDStrategy
	 *
	 * @param u a vertex in the graph
	 */
	default void removeEdgesAll(int u) {
		removeEdgesAllOut(u);
		removeEdgesAllIn(u);
	}

	default void removeEdgesAllOut(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

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
	 * the edge, but always the other end-point than {@link #edgeTarget(int)}
	 * returns.
	 *
	 * @param edge the edge identifier
	 * @return the edge source vertex
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
	 */
	public int edgeTarget(int edge);

	/**
	 * Get the other end-point of an edge.
	 *
	 * @param edge     the edge identifier
	 * @param endpoint one of the edge end-point
	 * @return the other end-point of the edge
	 */
	default int edgeEndpoint(int edge, int endpoint) {
		int u = edgeSource(edge);
		int v = edgeTarget(edge);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Get the out degree of a source vertex
	 *
	 * @param u a source vertex
	 * @return the number of edges whose source is u
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
	 * Get the out degree of a target vertex
	 *
	 * @param v a target vertex
	 * @return the number of edges whose target is v
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
	 * This function might be used to reuse an already allocated graph object
	 */
	public void clear();

	/**
	 * Remove all the edges from the graph.
	 *
	 * <p>
	 * Note that this function also clears any weights associated with removed
	 * edges.
	 */
	public void clearEdges();

	// TODO remove vertex
	// TODO documentation

	/**
	 * Get the vertices weights of some key.
	 *
	 * @param <V>        The weight type
	 * @param <WeightsT> the weights container
	 * @param key        some key of the weights, could be anything
	 * @return vertices weights of the key
	 */
	public <V, WeightsT extends Weights<V>> WeightsT verticesWeight(Object key);

	public Weights.Factory addVerticesWeight(Object key);

	/**
	 * Get the keys of all the associated vertices weights.
	 *
	 * @return the keys of all the associated vertices weights
	 */
	public Set<Object> getVerticesWeightKeys();

	/**
	 * Get all vertices weights.
	 *
	 * @return all vertices weights
	 */
	public Collection<Weights<?>> getVerticesWeights();

	/**
	 * Remove a weight type from the vertices of the graph.
	 *
	 * @param key the key of the weights
	 */
	public void removeVerticesWeights(Object key);

	/**
	 * Get the edges weights of some key.
	 *
	 * @param <E>        The weight type
	 * @param <WeightsT> the weights container
	 * @param key        some key of the weights, could be anything
	 * @return edges weights of the key
	 */
	public <E, WeightsT extends Weights<E>> WeightsT edgesWeight(Object key);

	public Weights.Factory addEdgesWeight(Object key);

	/**
	 * Get the keys of all the associated edges weights.
	 *
	 * @return the keys of all the associated edges weights
	 */
	public Set<Object> getEdgesWeightsKeys();

	/**
	 * Remove a weight type from the edges of the graph.
	 *
	 * @param key the key of the weights
	 */
	public void removeEdgesWeights(Object key);

	/**
	 * Get all edges weights.
	 *
	 * @return all edges weights
	 */
	public Collection<Weights<?>> getEdgesWeights();

	/**
	 * Get ID strategy of the vertices of the graph.
	 *
	 * <p>
	 * Each vertex in the graph is identified by a unique non negative int ID, which
	 * is determined by some strategy. Only {@link com.jgalgo.IDStrategy.Continues}
	 * is supported for vertices, which ensure that at all times the vertices IDs
	 * are {@code 0,1,..., verticesNum-1}, and it might rename some vertices when a
	 * vertex is removed to maintain this invariant. This rename can be subscribed
	 * using {@link com.jgalgo.IDStrategy#addIDSwapListener}.
	 *
	 * @see com.jgalgo.IDStrategy
	 *
	 * @return the vertices IDs strategy
	 */
	public IDStrategy.Continues getVerticesIDStrategy();

	/**
	 * Get the set of all edges of the graph.
	 *
	 * <p>
	 * Each edge in the graph is identified by a unique non negative int ID, which
	 * is determined by some strategy. For example,
	 * {@link com.jgalgo.IDStrategy.Continues} ensure that at all times the edges
	 * IDs are {@code 0,1,..., edgesNum-1}, and it might rename some edges when an
	 * edge is removed to maintain this invariant. This rename can be subscribed
	 * using {@link com.jgalgo.IDStrategy#addIDSwapListener}. Another option for an
	 * ID strategy is {@link com.jgalgo.IDStrategy.Fixed} which ensure once an edge
	 * is assigned an ID, it will not change. There might be some performance
	 * differences between different ID strategies.
	 *
	 * @see com.jgalgo.IDStrategy
	 *
	 * @return the edges IDs strategy
	 */
	public IDStrategy getEdgesIDStrategy();

	public GraphCapabilities getCapabilities();

}
