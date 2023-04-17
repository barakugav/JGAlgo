package com.jgalgo;

/**
 * A discrete directed graph with vertices and edges.
 * <p>
 * An extension to the {@link Graph} interface, where edges are directed, namely
 * an edge {@code e(u, v)} will appear in the iteration of {@code edgesOut(u)}
 * and {@code edgesIn(v)} and will not appear in the iteration of
 * {@code edgesOut(v)} and {@code edgesIn(u)}.
 *
 * @see UGraph
 * @author Barak Ugav
 */
public interface DiGraph extends Graph {

	/**
	 * Reverse an edge by switching its source and target.
	 *
	 * @param edge an existing edge in the graph
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge
	 *                                   identifier
	 */
	public void reverseEdge(int edge);

}
