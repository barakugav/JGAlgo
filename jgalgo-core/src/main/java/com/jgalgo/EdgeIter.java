package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Iterator used to iterate over edges of a vertex.
 * <p>
 * Each {@code int} returned by {@link #nextInt()} is an ID of an edge iterated by the iterator. The source and target
 * of the last iterated edge are available by {@link #u()} and {@link #v()}.
 *
 * <pre> {@code
 * Graph g = ...;
 * int vertex = ...;
 * for (EdgeIter eit = g.edgesOut(vertex); eit.hasNext();) {
 * 	int e = eit.nextInt();
 * 	int u = eit.u();
 * 	int v = eit.v();
 * 	assert vertex == u;
 * 	System.out.println("Out edge of " + vertex + ": " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface EdgeIter extends IntIterator {

	/**
	 * Get the source vertex of the last returned edge.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the source vertex of the last returned edge
	 */
	int u();

	/**
	 * Get the target vertex of the last returned edge.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the target vertex of the last returned edge
	 */
	int v();

}
