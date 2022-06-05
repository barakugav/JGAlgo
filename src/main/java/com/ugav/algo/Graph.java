package com.ugav.algo;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface Graph<E> {

	/**
	 * Get the number of vertices in the graph
	 *
	 * The vertices indices are 1, 2, ...
	 *
	 * @return number of vertices
	 */
	public int vertices();

	/**
	 * Get the edges of the graph
	 *
	 * @return view of all the edges in the graph
	 */
	public Collection<Edge<E>> edges();

	/**
	 * Get the edges whose source is u
	 *
	 * @param u a source vertex
	 * @return iterator that iterate over the edges whose source is u
	 */
	public EdgeIterator<E> edges(int u);

	/**
	 * Get an edge from u to v
	 *
	 * @param u source vertex
	 * @param v target vertex
	 * @return an edge from u to v, arbitrary one if multiple exists, null if non
	 *         exists
	 */
	public Edge<E> getEdge(int u, int v);

	/**
	 * Check if an edge exists from u to v
	 *
	 * @param u source vertex
	 * @param v target vertex
	 * @return true if an edge exists from u to v
	 */
	public boolean hasEdge(int u, int v);

	/**
	 * Checks if the graph is directed
	 *
	 * @return true if the graph is directed
	 */
	public boolean isDirected();

	/**
	 * Create a new vertex
	 *
	 * @return the new vertex index
	 */
	public int newVertex();

	/**
	 * Create a new edge
	 *
	 * @param u source vertex
	 * @param v target vertex
	 * @return the new created edge
	 */
	public Edge<E> addEdge(int u, int v);

	/**
	 * Add a new edge
	 *
	 * Actually use the same edge object. This function can be usefully if the edge
	 * holds additional properties. This function isn't supported on all graphs
	 * implementations
	 *
	 * @param e the edge to add
	 */
	public void addEdge(Edge<E> e);

	/**
	 * Remove an edge from the graph
	 *
	 * @param e the edge to remove
	 */
	public void removeEdge(Edge<E> e);

	/**
	 * Remove all the edges going out of a given vertex
	 *
	 * @param u a vertex index
	 */
	public void removeEdgesOut(int u);

	/**
	 * Clears the whole graph
	 *
	 * removes all vertices and edges
	 */
	public void clear();

	public static enum DirectedType {
		Directed, Undirected
	}

	public static interface Edge<E> {

		/**
		 * Get the source of the edge
		 *
		 * @return the source of the edge
		 */
		public int u();

		/**
		 * Get the target of the edge
		 *
		 * @return the target of the edge
		 */
		public int v();

		/**
		 * Get the value of the edge
		 *
		 * @return the value of the edge
		 */
		public E val();

		/**
		 * Set the value of the edge
		 *
		 * @param v new value
		 */
		public void val(E v);

		/**
		 * Get the twin edge of this edge
		 *
		 * The twin edge is only defined in undirected graphs, and its the edge which
		 * the source and target are flipped from this edge. The value of the two twin
		 * edges is always the same.
		 *
		 * @return the twin edge
		 */
		public Edge<E> twin();

	}

	public static interface EdgeIterator<E> extends Iterator<Edge<E>> {

		/**
		 * Pick at the next edge without advancing the iterator
		 *
		 * @return the next edge in the iterations
		 * @throws NoSuchElementException if hasNext() returned false
		 */
		public Edge<E> pickNext();

	}

	@FunctionalInterface
	public static interface WeightFunction<E> {

		public double weight(Edge<E> e);

	}

	@FunctionalInterface
	public static interface WeightFunctionInt<E> extends WeightFunction<E> {

		@Override
		default double weight(Edge<E> e) {
			return weightInt(e);
		}

		public int weightInt(Edge<E> e);

	}

}
