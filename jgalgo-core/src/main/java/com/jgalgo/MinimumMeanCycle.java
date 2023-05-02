package com.jgalgo;

/**
 * Algorithm that find the cycle with the minimum mean weight.
 * <p>
 * Given a graph \(G\), a cycle in \(G\) is a sequence of edges that
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

	/**
	 * Create a new minimum mean cycle algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumMeanCycle}
	 * object.
	 *
	 * @return a new builder that can build {@link MinimumMeanCycle} objects
	 */
	static MinimumMeanCycle.Builder newBuilder() {
		return MinimumMeanCycleHoward::new;
	}

	/**
	 * A builder for {@link MinimumMeanCycle} objects.
	 *
	 * @see MinimumMeanCycle#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum mean cycle computation.
		 *
		 * @return a new minimum mean cycle algorithm
		 */
		MinimumMeanCycle build();
	}

}
