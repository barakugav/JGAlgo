package com.jgalgo;

/**
 * An algorithm that compute all pairs shortest path (APSP) in a graph.
 * <p>
 * The regular {@link SSSP} can be used multiple times to achieve the same
 * result, but it may be more efficient to use a APSP algorithm in the first
 * place.
 *
 * @author Barak Ugav
 */
public interface APSP {

	/**
	 * Compute all shortest paths between all pairs of vertices in a graph.
	 *
	 * @param g a graph
	 * @param w an edge weight function
	 * @return a result object containing information on the shortest path between
	 *         each two pair of vertices
	 */
	public APSP.Result computeAllShortestPaths(Graph g, EdgeWeightFunc w);

	/**
	 * A result object for an {@link APSP} algorithm.
	 *
	 * @author Barak Ugav
	 */
	interface Result {

		/**
		 * Get the distance of the shortest path between two vertices.
		 *
		 * @param source the source vertex
		 * @param target the target vertex
		 * @return the sum of weights of edges in the shortest path from the source to
		 *         target, or {@code Double.POSITIVE_INFINITY} if no such path exists
		 * @throws IllegalArgumentException if a negative cycle found. See
		 *                                  {@link foundNegativeCycle}
		 */
		public double distance(int source, int target);

		/**
		 * Get the shortest path between vertices.
		 *
		 * @param source the source vertex
		 * @param target the target vertex
		 * @return the shortest path from the source to target, or {@code null} if no
		 *         such path exists
		 * @throws IllegalArgumentException if a negative cycle found. See
		 *                                  {@link foundNegativeCycle}
		 */
		public Path getPath(int source, int target);

		/**
		 * Check whether a negative cycle was found.
		 * <p>
		 * If a negative cycle was found, there is no unique shortest paths, as the
		 * paths weight could be arbitrary small by going through the cycle multiple
		 * times.
		 *
		 * @return {@code true} if a negative cycle was found
		 */
		public boolean foundNegativeCycle();

		/**
		 * Get the negative cycle that was found.
		 *
		 * @return the negative cycle that was found.
		 * @throws IllegalArgumentException if a negative cycle was found. See
		 *                                  {@link foundNegativeCycle}
		 */
		public Path getNegativeCycle();
	}

	/**
	 * Create a new all pairs shortest paths algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link APSP} object.
	 *
	 * @return a new builder that can build {@link APSP} objects
	 */
	static APSP.Builder newBuilder() {
		return APSPJohnson::new;
	}

	/**
	 * A builder for {@link APSP} objects.
	 *
	 * @see APSP#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for all pairs shortest paths computation.
		 *
		 * @return a new all pairs shortest paths algorithm
		 */
		APSP build();
	}

}
