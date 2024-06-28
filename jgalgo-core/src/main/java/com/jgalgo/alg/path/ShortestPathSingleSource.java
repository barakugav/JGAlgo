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

package com.jgalgo.alg.path;

import com.jgalgo.alg.AlgorithmBuilderBase;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.JGAlgoUtils;

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
 * {@link ShortestPathSingleSource.Builder#negativeWeights(boolean)}.
 *
 * <p>
 * A special case of the SSSP problem is on directed graphs that does not contain any cycles, and it could be solved in
 * linear time for any weights types by calculating the topological order of the vertices (see
 * {@link ShortestPathSingleSource.Builder#dag(boolean)}). Another special case arise when the weight function assign
 * \(1\) to any edges, and the shortest paths could be computed again in linear time using a BFS (see
 * {@link ShortestPathSingleSource.Builder#cardinality(boolean)}).
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
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
		 * @param  target                a target vertex in the graph
		 * @return                       the sum of the shortest path edges from the source to the target, or
		 *                               {@code Double.POSITIVE_INFINITY} if no such path found
		 * @throws NoSuchVertexException if {@code target} is not a vertex in the graph
		 */
		public double distance(V target);

		/**
		 * Get shortest path to a target vertex.
		 *
		 * @param  target                a target vertex in the graph
		 * @return                       the shortest path from the source to the target or {@code null} if no such path
		 *                               found.
		 * @throws NoSuchVertexException if {@code target} is not a vertex in the graph
		 */
		public Path<V, E> getPath(V target);

		/**
		 * Get the last edge on the shortest path from the source to the given target.
		 *
		 * <p>
		 * The backtrack edge is an in-edge of the given target vertex. The set of all backtrack edges of all vertices
		 * define the shortest path tree of the source, and each shortest path can be constructed from them.
		 *
		 * @param  target a target vertex in the graph
		 * @return        the backtrack edge, the last edge on the shortest path from the source to th given target, or
		 *                {@code null} if there is no path to the target or the target is the source
		 */
		public E backtrackEdge(V target);

		/**
		 * Get the shortest path tree.
		 *
		 * <p>
		 * The shortest path tree is constructed from the vertices and edges used by any shortest path. It contains only
		 * the vertices reachable from the source, and for each vertex other than the source the graph will contains the
		 * edge that was used to reach it (see {@link #backtrackEdge(Object)}). If there are \(k\) reachable vertices,
		 * the graph will contain \(k-1\) edges.
		 *
		 * <p>
		 * The returned graph will be directed if the original graph is directed. In such case, the tree is directed
		 * from the source to the other vertices. To control the directionality of the returned graph, use
		 * {@link #shortestPathTree(boolean)}.
		 *
		 * @return undirected shortest path tree
		 */
		default Graph<V, E> shortestPathTree() {
			return shortestPathTree(graph().isDirected());
		}

		/**
		 * Get the shortest path tree, optionally directed or undirected.
		 *
		 * <p>
		 * The shortest path tree is constructed from the vertices and edges used by any shortest path. It contains only
		 * the vertices reachable from the source, and for each vertex other than the source the graph will contains the
		 * edge that was used to reach it (see {@link #backtrackEdge(Object)}). If there are \(k\) reachable vertices,
		 * the graph will contain \(k-1\) edges.
		 *
		 * @param  directed if {@code true} the returned tree will be directed. If the original graph was undirected and
		 *                      a directed tree is created, the edges in the tree will be directed from the source
		 *                      towards the other vertices
		 * @return          un/directed shortest path tree
		 */
		default Graph<V, E> shortestPathTree(boolean directed) {
			Graph<V, E> g = graph();
			GraphBuilder<V, E> b = GraphBuilder.newInstance(directed);
			final V source = source();
			b.addVertex(source);
			for (V v : g.vertices())
				if (backtrackEdge(v) != null)
					b.addVertex(v);
			for (V v : g.vertices()) {
				E e = backtrackEdge(v);
				if (e != null)
					b.addEdge(g.edgeEndpoint(e, v), v, e);
			}
			return b.build();
		}

		/**
		 * Get the source vertex from which all shortest paths were computed from.
		 *
		 * @return the source vertex
		 */
		public V source();

		/**
		 * Get the graph on which the shortest paths were computed on.
		 *
		 * @return the graph on which the shortest paths were computed on.
		 */
		public Graph<V, E> graph();
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
		 * @param  target                a target vertex in the graph
		 * @return                       the sum of the shortest path edges from the source to the target, or
		 *                               {@code Double.POSITIVE_INFINITY} if no such path found.
		 * @throws NoSuchVertexException if {@code target} is not a vertex in the graph
		 */
		public double distance(int target);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #distance(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default double distance(Integer target) {
			return distance(target.intValue());
		}

		/**
		 * Get shortest path to a target vertex.
		 *
		 * @param  target                a target vertex in the graph
		 * @return                       the shortest path from the source to the target or {@code null} if no such path
		 *                               found
		 * @throws NoSuchVertexException if {@code target} is not a vertex in the graph
		 */
		public IPath getPath(int target);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #getPath(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default IPath getPath(Integer target) {
			return getPath(target.intValue());
		}

		/**
		 * Get the last edge on the shortest path from the source to the given target.
		 *
		 * <p>
		 * The backtrack edge is an in-edge of the given target vertex. The set of all backtrack edges of all vertices
		 * define the shortest path tree of the source, and each shortest path can be constructed from them.
		 *
		 * @param  target a target vertex in the graph
		 * @return        the backtrack edge, the last edge on the shortest path from the source to th given target, or
		 *                {@code -1} if there is no path to the target or the target is the source
		 */
		public int backtrackEdge(int target);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #backtrackEdge(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer backtrackEdge(Integer target) {
			int e = backtrackEdge(target.intValue());
			return e < 0 ? null : Integer.valueOf(e);
		}

		@Override
		default IntGraph shortestPathTree() {
			return (IntGraph) Result.super.shortestPathTree();
		}

		@Override
		default IntGraph shortestPathTree(boolean directed) {
			IntGraph g = graph();
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			final int source = sourceInt();
			b.addVertex(source);
			for (int v : g.vertices())
				if (backtrackEdge(v) >= 0)
					b.addVertex(v);
			for (int v : g.vertices()) {
				int e = backtrackEdge(v);
				if (e >= 0)
					b.addEdge(g.edgeEndpoint(e, v), v, e);
			}
			return b.build();
		}

		/**
		 * Get the source vertex from which all shortest paths were computed from.
		 *
		 * @return the source vertex
		 */
		public int sourceInt();

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #sourceInt()} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer source() {
			return Integer.valueOf(sourceInt());
		}

		@Override
		public IntGraph graph();
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
		return builder().build();
	}

	/**
	 * Create a new single source shortest path algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ShortestPathSingleSource} objects
	 */
	static ShortestPathSingleSource.Builder builder() {
		return new ShortestPathSingleSource.Builder() {

			private boolean intWeights;
			private boolean negativeWeights;
			private double maxDistance = Double.POSITIVE_INFINITY;
			private boolean dagGraphs;
			private boolean cardinalityWeight;

			@Override
			public ShortestPathSingleSource build() {
				if (cardinalityWeight)
					return new ShortestPathSingleSourceCardinality();
				if (dagGraphs)
					return new ShortestPathSingleSourceDag();
				if (negativeWeights) {
					if (intWeights) {
						return new ShortestPathSingleSourceGoldberg();
					} else {
						return new ShortestPathSingleSourceBellmanFord();
					}
				} else {
					final ShortestPathSingleSourceDijkstra ssspDijkstra = new ShortestPathSingleSourceDijkstra();

					if (intWeights && maxDistance < Integer.MAX_VALUE) {
						return new ShortestPathSingleSourceAbstract() {
							private final ShortestPathSingleSourceDial ssspDial = new ShortestPathSingleSourceDial();
							private final int maxDistance0 = (int) maxDistance;

							@Override
							public ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g,
									IWeightFunction w, int source) {
								final int n = g.vertices().size(), m = g.edges().size();
								int dialWork = n + m + maxDistance0;
								int dijkstraWork = m + n * JGAlgoUtils.log2ceil(n);
								if (dialWork < dijkstraWork) {
									return ssspDial
											.computeShortestPaths(g, (IWeightFunctionInt) w, source, maxDistance0);
								} else {
									return ssspDijkstra.computeShortestPaths(g, w, source);
								}
							}
						};
					}
					return ssspDijkstra;
				}
			}

			@Override
			public ShortestPathSingleSource.Builder integerWeights(boolean enable) {
				intWeights = enable;
				return this;
			}

			@Override
			public ShortestPathSingleSource.Builder negativeWeights(boolean enable) {
				negativeWeights = enable;
				return this;
			}

			@Override
			public ShortestPathSingleSource.Builder maxDistance(double maxDistance) {
				this.maxDistance = maxDistance;
				return this;
			}

			@Override
			public ShortestPathSingleSource.Builder dag(boolean dagGraphs) {
				this.dagGraphs = dagGraphs;
				return this;
			}

			@Override
			public ShortestPathSingleSource.Builder cardinality(boolean cardinalityWeight) {
				this.cardinalityWeight = cardinalityWeight;
				return this;
			}
		};
	}

	/**
	 * A builder for {@link ShortestPathSingleSource} objects.
	 *
	 * @see    ShortestPathSingleSource#builder()
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
		ShortestPathSingleSource.Builder integerWeights(boolean enable);

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
		ShortestPathSingleSource.Builder negativeWeights(boolean enable);

		/**
		 * Set the maximum distance that should be supported.
		 *
		 * <p>
		 * This method may be used as a hint to choose an {@link ShortestPathSingleSource} implementation.
		 *
		 * @param  maxDistance a maximum distance upper bound on the distance from the source to any vertex
		 * @return             this builder
		 */
		ShortestPathSingleSource.Builder maxDistance(double maxDistance);

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
		ShortestPathSingleSource.Builder dag(boolean dagGraphs);

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
		ShortestPathSingleSource.Builder cardinality(boolean cardinalityWeight);

	}

}
