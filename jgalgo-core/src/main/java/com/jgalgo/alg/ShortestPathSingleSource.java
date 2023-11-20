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

package com.jgalgo.alg;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Single Source Shortest Path algorithm.
 *
 * <p>
 * Given a graph \(G=(V,E)\), and a weight function \(w:E \rightarrow R\), one might ask what is the shortest path from
 * a <i>source</i> vertex to all other vertices in \(V\), where the 'shortest' is defined by comparing the sum of edges
 * weights of each path. A Single Source Shortest Path (SSSP) is able to compute these shortest paths from a source to
 * any other vertex, along with the distances, which are the shortest paths lengths (weights).
 *
 * <p>
 * The most basic single source shortest path (SSSP) algorithms work on graphs with non negative weights, and they are
 * the most efficient, such as Dijkstra algorithm. Negative weights are supported by some implementations of SSSP, and
 * the 'shortest path' is well defined as long as there are no negative cycle in the graph as a path can loop in the
 * cycle and achieve arbitrary small 'length'. When the weights are allowed be negative, algorithms will either find the
 * shortest path to any other vertex, or will find a negative cycle, see {@link NegativeCycleException}. Note that if a
 * negative cycle exists, but it is not reachable from the source, the algorithm may or may not find it, depending on
 * the implementation. To get an algorithm instance that support negative weights, use
 * {@link ShortestPathSingleSource.Builder#setNegativeWeights(boolean)}.
 *
 * <p>
 * A special case of the SSSP problem is on directed graphs that does not contain any cycles, and it could be solved in
 * linear time for any weights types by calculating the topological order of the vertices (see
 * {@link ShortestPathSingleSource.Builder#setDag(boolean)}). Another special case arise when the weight function assign
 * \(1\) to any edges, and the shortest paths could be computed again in linear time using a BFS (see
 * {@link ShortestPathSingleSource.Builder#setCardinality(boolean)}).
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * // Create an undirected graph with three vertices and edges between them
 * Graph<String, Integer> g = Graph.newUndirected();
 * g.addVertex("Berlin");
 * g.addVertex("Leipzig");
 * g.addVertex("Dresden");
 * g.addEdge("Berlin", "Leipzig", 9);
 * g.addEdge("Berlin", "Dresden", 13);
 * g.addEdge("Dresden", "Leipzig", 14);
 *
 * // Assign some weights to the edges
 * WeightsDouble<Integer> w = g.addEdgesWeights("distance-km", double.class);
 * w.set(9, 191.1);
 * w.set(13, 193.3);
 * w.set(14, 121.3);
 *
 * // Calculate the shortest paths from Berlin to all other cities
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result<String, Integer> ssspRes = ssspAlgo.computeShortestPaths(g, w, "Berlin");
 *
 * // Print the shortest path from Berlin to Leipzig
 * System.out.println("Distance from Berlin to Leipzig is: " + ssspRes.distance("Leipzig"));
 * System.out.println("The shortest path from Berlin to Leipzig is:");
 * for (Integer e : ssspRes.getPath("Leipzig").edges()) {
 * 	String u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    ShortestPathAllPairs
 * @see    <a href= "https://en.wikipedia.org/wiki/Shortest_path_problem#Single-source_shortest_paths">Wikipedia</a>
 * @author Barak Ugav
 */
public interface ShortestPathSingleSource {

	/**
	 * Compute the shortest paths from a source to any other vertex in a graph.
	 *
	 * <p>
	 * Given an edge weight function, the length of a path is the weight sum of all edges of the path. The shortest path
	 * from a source vertex to some other vertex is the path with the minimum weight. For cardinality (non weighted)
	 * shortest path, pass {@code null} instead of the weight function {@code w}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link ShortestPathSingleSource.IResult} object will be returned. In that
	 * case, its better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>                    the vertices type
	 * @param  <E>                    the edges type
	 * @param  g                      a graph
	 * @param  w                      an edge weight function
	 * @param  source                 a source vertex
	 * @return                        a result object containing the distances and shortest paths from the source to any
	 *                                other vertex
	 * @throws NegativeCycleException if a negative cycle is detected in the graph. If there is a negative cycle that is
	 *                                    not reachable from the given source, it might not be detected, depending on
	 *                                    the implementation
	 */
	public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
			V source);

	/**
	 * A result object for the {@link ShortestPathSingleSource} problem.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	public static interface Result<V, E> {

		/**
		 * Get the distance to a target vertex.
		 *
		 * @param  target a target vertex in the graph
		 * @return        the sum of the shortest path edges from the source to the target, or
		 *                {@code Double.POSITIVE_INFINITY} if no such path found.
		 */
		public double distance(V target);

		/**
		 * Get shortest path to a target vertex.
		 *
		 * @param  target a target vertex in the graph
		 * @return        the shortest path from the source to the target or {@code null} if no such path found.
		 */
		public Path<V, E> getPath(V target);
	}

	/**
	 * A result object for the {@link ShortestPathSingleSource} problem for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	public static interface IResult extends ShortestPathSingleSource.Result<Integer, Integer> {

		/**
		 * Get the distance to a target vertex.
		 *
		 * @param  target a target vertex in the graph
		 * @return        the sum of the shortest path edges from the source to the target, or
		 *                {@code Double.POSITIVE_INFINITY} if no such path found.
		 */
		public double distance(int target);

		@Deprecated
		@Override
		default double distance(Integer target) {
			return distance(target.intValue());
		}

		/**
		 * Get shortest path to a target vertex.
		 *
		 * @param  target a target vertex in the graph
		 * @return        the shortest path from the source to the target or {@code null} if no such path found.
		 */
		public IPath getPath(int target);

		@Deprecated
		@Override
		default IPath getPath(Integer target) {
			return getPath(target.intValue());
		}
	}

	/**
	 * Create a new shortest path algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathSingleSource} object. The
	 * {@link ShortestPathSingleSource.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ShortestPathSingleSource}
	 */
	static ShortestPathSingleSource newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new single source shortest path algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ShortestPathSingleSource} objects
	 */
	static ShortestPathSingleSource.Builder newBuilder() {
		return new ShortestPathSingleSourceUtils.BuilderImpl();
	}

	/**
	 * A builder for {@link ShortestPathSingleSource} objects.
	 *
	 * @see    ShortestPathSingleSource#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for single source shortest path computation.
		 *
		 * @return a new single source shortest path algorithm
		 */
		ShortestPathSingleSource build();

		/**
		 * Enable/disable integer weights.
		 *
		 * <p>
		 * More efficient and accurate implementations may be supported if the edge weights are known to be integer.
		 *
		 * @param  enable if {@code true}, the built {@link ShortestPathSingleSource} objects will support only integer
		 *                    weights
		 * @return        this builder
		 */
		ShortestPathSingleSource.Builder setIntWeights(boolean enable);

		/**
		 * Enable/disable the support for negative numbers.
		 *
		 * <p>
		 * More efficient and accurate implementations may be supported if its known in advance that all edge weights
		 * will be positive (which is the default).
		 *
		 *
		 * @param  enable if {@code true}, the built {@link ShortestPathSingleSource} objects will support negative
		 *                    numbers
		 * @return        this builder
		 */
		ShortestPathSingleSource.Builder setNegativeWeights(boolean enable);

		/**
		 * Set the maximum distance that should be supported.
		 *
		 * <p>
		 * This method may be used as a hint to choose an {@link ShortestPathSingleSource} implementation.
		 *
		 * @param  maxDistance a maximum distance upper bound on the distance from the source to any vertex
		 * @return             this builder
		 */
		ShortestPathSingleSource.Builder setMaxDistance(double maxDistance);

		/**
		 * Enable/disable the support for directed acyclic graphs (DAG) only.
		 *
		 * <p>
		 * More efficient algorithm may exists if we know in advance all input graphs will be DAG. Note that if this
		 * option is enabled, ONLY directed acyclic graphs will be supported.
		 *
		 * @param  dagGraphs if {@code true}, the built {@link ShortestPathSingleSource} objects will support only
		 *                       directed acyclic graphs
		 * @return           this builder
		 */
		ShortestPathSingleSource.Builder setDag(boolean dagGraphs);

		/**
		 * Enable/disable the support for cardinality shortest paths only.
		 *
		 * <p>
		 * More efficient algorithm may exists for cardinality shortest paths. Note that if this option is enabled, ONLY
		 * cardinality shortest paths will be supported.
		 *
		 * @param  cardinalityWeight if {@code true}, only cardinality shortest paths will be supported by algorithms
		 *                               built by this builder
		 * @return                   this builder
		 */
		ShortestPathSingleSource.Builder setCardinality(boolean cardinalityWeight);

	}

}
