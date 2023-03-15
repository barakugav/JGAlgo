package com.ugav.algo;

import java.util.Collection;
import java.util.Iterator;

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
	public Iterator<Edge<E>> edges(int u);

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
	 * @throws UnsupportedOperationException if the operation is not supported
	 */
	public void addEdge(Edge<E> e);

	/**
	 * Remove an edge from the graph
	 *
	 * @param e the edge to remove
	 */
	public void removeEdge(Edge<E> e);

	/**
	 * Clears the whole graph
	 *
	 * removes all vertices and edges
	 */
	public void clear();

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
		 * Get the data of the edge
		 *
		 * @return the data of the edge
		 */
		public E data();

		/**
		 * Set the data of the edge
		 *
		 * @param data new data value
		 */
		public void setData(E data);

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

	public static interface Undirected<E> extends Graph<E> {

	}

	public static interface Directed<E> extends Graph<E> {

		/**
		 * Get the edges whose source is u
		 */
		@Override
		default Iterator<Edge<E>> edges(int u) {
			return edgesOut(u);
		}

		/**
		 * Get the edges whose source is u
		 *
		 * @param u a source vertex
		 * @return iterator that iterate over the edges whose source is u
		 */
		public Iterator<Edge<E>> edgesOut(int u);

		/**
		 * Get the edges whose target is v
		 *
		 * @param v a target vertex
		 * @return iterator that iterate over the edges whose target is v
		 */
		public Iterator<Edge<E>> edgesIn(int v);

		/**
		 * Remove all the edges going out of a given vertex
		 *
		 * @param u a source vertex
		 */
		default void removeEdgesOut(int u) {
			for (Iterator<Edge<E>> it = edgesOut(u); it.hasNext();) {
				it.next();
				it.remove();
			}
		}

		/**
		 * Remove all the edges going into a given vertex
		 *
		 * @param v a target vertex
		 */
		default void removeEdgesIn(int v) {
			for (Iterator<Edge<E>> it = edgesIn(v); it.hasNext();) {
				it.next();
				it.remove();
			}
		}

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
