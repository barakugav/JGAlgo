package com.jgalgo;

/**
 * Single Source Shortest Path algorithm.
 * <p>
 * Given a graph {@code G=(V,E)}, and a weight function {@code w:E->R}, one
 * might ask what is the shortest path from a <i>source</i> vertex {@code s} to
 * all other vertices in {@code V}, where the 'shortest' is defined by comparing
 * the sum of edges weights of each path. A Single Source Shortest Path (SSSP)
 * is able to compute these shortest paths from a source to any other vertex,
 * along with the distances, which are the shortest paths lengths (weights).
 * <p>
 * The most basic SSSP algorithms work on graphs with non negative weights, and
 * they are the most efficient, such as {@link SSSPDijkstra}. Negative weights
 * are supported by some implementations of SSSP, and the 'shortest path' is
 * well defined as long as there are no negative cycle in the graph as a path
 * can loop in the cycle and achieve arbitrary small 'length'.
 * <p>
 * A special case of the SSSP problem is on directed graphs that does not
 * contain any cycles, and it could be solved in linear time for any weights
 * types
 * using {@link SSSPDag}. Another special case arise when the weight function
 * assign {@code 1} to any edges, and the shortest paths could be computed again
 * in linear time using {@link SSSPCardinality}.
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
 * SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);
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
 * @see APSP
 * @author Barak Ugav
 */
public interface SSSP {

	/**
	 * Compute the shortest paths from a source to any other vertex in a graph.
	 *
	 * @param g      a graph
	 * @param w      an edge weight function
	 * @param source a source vertex
	 * @return a result object containing the distances and shortest paths from the
	 *         source to any other vertex
	 */
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source);

	/**
	 * A result object for the {@link SSSP} problem.
	 *
	 * @author Barak Ugav
	 */
	public static interface Result {

		/**
		 * Get the distance to a target vertex.
		 *
		 * @param target a target vertex in the graph
		 * @return the sum of the shortest path edges from the source to the target, or
		 *         {@code Double.POSITIVE_INFINITY} if no such path found.
		 * @throws IllegalStateException if and negative cycle was found and
		 *                               {@link #foundNegativeCycle()} return
		 *                               {@code true}.
		 */
		public double distance(int target);

		/**
		 * Get shortest path to a target vertex.
		 *
		 * @param target a target vertex in the graph
		 * @return the shortest path from the source to the target or {@code null} if no
		 *         such path found.
		 * @throws IllegalStateException if a negative cycle was found and
		 *                               {@link #foundNegativeCycle()} return
		 *                               {@code true}.
		 */
		public Path getPath(int target);

		/**
		 * Check whether a negative cycle was found.
		 * <p>
		 * If a negative cycle was found, the 'shortest paths' are not well defined, as
		 * a path can loop in the cycle and achieve arbitrary small 'length'.
		 *
		 * @return {@code true} if a negative cycle found, else {@code false}.
		 */
		public boolean foundNegativeCycle();

		/**
		 * Get the negative cycle that was found.
		 *
		 * @return the negative cycle that was found.
		 * @throws IllegalStateException if no negative cycle was found and
		 *                               {@link #foundNegativeCycle()} return
		 *                               {@code false}.
		 */
		public Path getNegativeCycle();

	}

}
