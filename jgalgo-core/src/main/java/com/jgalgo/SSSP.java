/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

/**
 * Single Source Shortest Path algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), and a weight function \(w:E \rightarrow R\), one might ask what is the shortest path from
 * a <i>source</i> vertex to all other vertices in \(V\), where the 'shortest' is defined by comparing the sum of edges
 * weights of each path. A Single Source Shortest Path (SSSP) is able to compute these shortest paths from a source to
 * any other vertex, along with the distances, which are the shortest paths lengths (weights).
 * <p>
 * The most basic SSSP algorithms work on graphs with non negative weights, and they are the most efficient, such as
 * {@link SSSPDijkstra}. Negative weights are supported by some implementations of SSSP, and the 'shortest path' is well
 * defined as long as there are no negative cycle in the graph as a path can loop in the cycle and achieve arbitrary
 * small 'length'.
 * <p>
 * A special case of the SSSP problem is on directed graphs that does not contain any cycles, and it could be solved in
 * linear time for any weights types using {@link SSSPDag}. Another special case arise when the weight function assign
 * \(1\) to any edges, and the shortest paths could be computed again in linear time using {@link SSSPCardinality}.
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * Graph g = GraphBuilder.newDirected().build();
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
 * SSSP ssspAlgo = SSSP.newBuilder().build();
 * SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (IntIterator it = ssspRes.getPath(v3).iterator(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    APSP
 * @see    <a href= "https://en.wikipedia.org/wiki/Shortest_path_problem#Single-source_shortest_paths">Wikipedia</a>
 * @author Barak Ugav
 */
public interface SSSP {

	/**
	 * Compute the shortest paths from a source to any other vertex in a graph.
	 * <p>
	 * Given an edge weight function, the length of a path is the weight sum of all edges of the path. The shortest path
	 * from a source vertex to some other vertex is the path with the minimum weight.
	 *
	 * @param  g      a graph
	 * @param  w      an edge weight function
	 * @param  source a source vertex
	 * @return        a result object containing the distances and shortest paths from the source to any other vertex
	 */
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source);

	/**
	 * Compute the cardinality shortest paths from a source to any other vertex in a graph.
	 * <p>
	 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex
	 * to some other vertex is the path with the minimum number of edges.
	 *
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @return        a result object containing the distances and cardinality shortest paths from the source to any
	 *                other vertex
	 */
	default SSSP.Result computeCardinalityShortestPaths(Graph g, int source) {
		return computeShortestPaths(g, EdgeWeightFunc.CardinalityEdgeWeightFunction, source);
	}

	/**
	 * A result object for the {@link SSSP} problem.
	 *
	 * @author Barak Ugav
	 */
	public static interface Result {

		/**
		 * Get the distance to a target vertex.
		 *
		 * @param  target                a target vertex in the graph
		 * @return                       the sum of the shortest path edges from the source to the target, or
		 *                               {@code Double.POSITIVE_INFINITY} if no such path found.
		 * @throws IllegalStateException if and negative cycle was found and {@link #foundNegativeCycle()} return
		 *                                   {@code true}.
		 */
		public double distance(int target);

		/**
		 * Get shortest path to a target vertex.
		 *
		 * @param  target                a target vertex in the graph
		 * @return                       the shortest path from the source to the target or {@code null} if no such path
		 *                               found.
		 * @throws IllegalStateException if a negative cycle was found and {@link #foundNegativeCycle()} return
		 *                                   {@code true}.
		 */
		public Path getPath(int target);

		/**
		 * Check whether a negative cycle was found.
		 * <p>
		 * If a negative cycle was found, the 'shortest paths' are not well defined, as a path can loop in the cycle and
		 * achieve arbitrary small 'length'.
		 *
		 * @return {@code true} if a negative cycle found, else {@code false}.
		 */
		public boolean foundNegativeCycle();

		/**
		 * Get the negative cycle that was found.
		 *
		 * @return                       the negative cycle that was found.
		 * @throws IllegalStateException if no negative cycle was found and {@link #foundNegativeCycle()} return
		 *                                   {@code false}.
		 */
		public Path getNegativeCycle();

	}

	/**
	 * Create a new single source shortest path algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link SSSP} object.
	 *
	 * @return a new builder that can build {@link SSSP} objects
	 */
	static SSSP.Builder newBuilder() {
		return new SSSPBuilderImpl();
	}

	/**
	 * A builder for {@link SSSP} objects.
	 *
	 * @see    SSSP#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<SSSP.Builder> {

		/**
		 * Create a new algorithm object for single source shortest path computation.
		 *
		 * @return a new single source shortest path algorithm
		 */
		SSSP build();

		/**
		 * Enable/disable integer weights.
		 * <p>
		 * More efficient and accurate implementations may be supported if the edge weights are known to be integer.
		 *
		 * @param  enable if {@code true}, the built {@link SSSP} objects will support only integer weights
		 * @return        this builder
		 */
		SSSP.Builder setIntWeights(boolean enable);

		/**
		 * Enable/disable the support for negative numbers.
		 * <p>
		 * More efficient and accurate implementations may be supported if its known in advance that all edge weights
		 * will be positive (which is the default).
		 *
		 *
		 * @param  enable if {@code true}, the built {@link SSSP} objects will support negative numbers
		 * @return        this builder
		 */
		SSSP.Builder setNegativeWeights(boolean enable);

		/**
		 * Set the minimum weight that should be supported.
		 * <p>
		 * This method may be used as a hint to choose an {@link SSSP} implementation.
		 *
		 * @param  minWeight a minimum weight lower bound on all edge weights
		 * @return           this builder
		 */
		SSSP.Builder setMinWeight(double minWeight);

		/**
		 * Set the maximum weight that should be supported.
		 * <p>
		 * This method may be used as a hint to choose an {@link SSSP} implementation.
		 *
		 * @param  maxWeight a maximum weight upper bound on all edge weights
		 * @return           this builder
		 */
		SSSP.Builder setMaxWeight(double maxWeight);

		/**
		 * Set the maximum distance that should be supported.
		 * <p>
		 * This method may be used as a hint to choose an {@link SSSP} implementation.
		 *
		 * @param  maxDistance a maximum distance upper bound on the distance from the source to any vertex
		 * @return             this builder
		 */
		SSSP.Builder setMaxDistance(double maxDistance);

		/**
		 * Enable/disable the support for directed acyclic graphs (DAG) only.
		 * <p>
		 * More efficient algorithm may exists if we know in advance all input graphs will be DAG.
		 *
		 * @param  dagGraphs if {@code true}, the built {@link SSSP} objects will support only directed acyclic graphs
		 * @return           this builder
		 */
		SSSP.Builder setDag(boolean dagGraphs);

	}

}
