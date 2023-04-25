package com.jgalgo;

/**
 * Algorithm that find the cycle with the minimum mean weight.
 * <p>
 * Given a graph {@code G}, a cycle in {@code G} is a sequence of edges that
 * form a path, and its first edge source is also its last edge target. Given an
 * edge weight function, we can define for each such cycle its mean weight, by
 * summing its edges weights and dividing by its length (the number of edges in
 * the cycle). Algorithms implementing this interface find the cycle with the
 * minimum mean weight among all the cycles in the given graph.
 *
 * @author Barak Ugav
 */
public interface MinimumMeanCycle {

	/**
	 * Compute the minimum mean cycle in a graph.
	 *
	 * @param g a graph
	 * @param w an edge weight function
	 * @return the cycle with the minimum mean weight in the graph, or {@code null}
	 *         if no cycles were found
	 */
	Path computeMinimumMeanCycle(Graph g, EdgeWeightFunc w);

}
