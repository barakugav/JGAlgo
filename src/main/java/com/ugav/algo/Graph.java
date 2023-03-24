package com.ugav.algo;

import java.util.Collection;
import java.util.Set;

import com.ugav.algo.IDStrategy.IDSwapListener;

import it.unimi.dsi.fastutil.ints.IntSet;

public interface Graph {

	public IntSet vertices();

	public IntSet edges();

	/**
	 * Add a new vertex to the graph.
	 *
	 * @return the new vertex identifier
	 */
	public int addVertex();

	/**
	 * Get the edges of a vertex u.
	 * <p>
	 * In case the graph is directed, this function returns the edges which u is
	 * their source vertex.
	 *
	 * @param u a source vertex
	 * @return an iterator of all the edges whose source is u
	 */
	public EdgeIter edges(int u);

	/**
	 * Get the edge whose source is u and target is v.
	 * <p>
	 * If the graph is not directed, the return edge is an edge that its end-points
	 * are u,v
	 *
	 * @param u a source vertex
	 * @param v a target vertex
	 * @return id of the edge or -1 if no such edge exists
	 */
	default int getEdge(int u, int v) {
		for (EdgeIter it = edges(u); it.hasNext();) {
			int e = it.nextInt();
			if (it.v() == v)
				return e;
		}
		return -1;
	}

	/**
	 * Add a new edge to the graph.
	 * <p>
	 *
	 * @param u a source vertex
	 * @param v a target vertex
	 * @return the new edge identifier
	 */
	public int addEdge(int u, int v);

	/**
	 * Remove an edge from the graph.
	 * <p>
	 * After removing the edge, the graph implementation may rename the other edges
	 * identifier to maintain edges IDs 0, 1, ..., edges().size() - 1. To keep track
	 * of these renames, one can a listener using
	 * {@link #addIDSwapListener(IDSwapListener)}.
	 *
	 * @param edge the edge identifier
	 */
	public void removeEdge(int edge);

	/**
	 * Remove all the edges of a vertex u.
	 * <p>
	 * If the graph is directed, both the in and out edges of the vertex are
	 * removed. Note that this function may change the identifiers of other edges.
	 * see {@link #addIDSwapListener(IDSwapListener)}.
	 *
	 * @param u a vertex in the graph
	 */
	default void removeEdgesAll(int u) {
		for (EdgeIter eit = edges(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Get the source vertex of an edge.
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
	 * Get the degree of a vertex, the number of its edges.
	 * <p>
	 * If the graph is directed, this function return the number of edges whose u is
	 * either their source or target.
	 *
	 * @param u a vertex
	 * @return the number of edges whose u is their end-point
	 */
	default int degree(int u) {
		int count = 0;
		for (EdgeIter it = edges(u); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	/**
	 * Clear the graph completely by removing all vertices and edges.
	 * <p>
	 * This function might be used to reuse an already allocated graph object
	 */
	public void clear();

	/**
	 * Remove all the edges from the graph.
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

	/**
	 * Get the keys of all the associated edges weights.
	 *
	 * @return the keys of all the associated edges weights
	 */
	public Set<Object> getEdgesWeightsKeys();

	public void removeEdgesWeights(Object key);

	/**
	 * Get all edges weights.
	 *
	 * @return all edges weights
	 */
	public Collection<Weights<?>> getEdgesWeights();

	public IDStrategy getVerticesIDStrategy();

	public IDStrategy getEdgesIDStrategy();

}
