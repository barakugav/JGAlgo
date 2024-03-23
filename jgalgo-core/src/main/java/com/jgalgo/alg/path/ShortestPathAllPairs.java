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

import java.util.Collection;
import com.jgalgo.alg.AlgorithmBuilderBase;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * An algorithm that compute all pairs shortest path (APSP) in a graph.
 *
 * <p>
 * The regular {@link ShortestPathSingleSource} can be used \(n\) times to achieve the same result, but it may be more
 * efficient to use a APSP algorithm in the first place.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface ShortestPathAllPairs {

	/**
	 * Compute the shortest path between each pair of vertices in a graph.
	 *
	 * <p>
	 * Given an edge weight function, the length of a path is the weight sum of all edges of the path. The shortest path
	 * from a source vertex to some other vertex is the path with the minimum weight.
	 *
	 * <p>
	 * To compute the shortest cardinality (non weighted) paths, pass {@code null} instead of the weight function
	 * {@code w}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link ShortestPathAllPairs.IResult} object will be returned. In that
	 * case, its better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>                    the vertices type
	 * @param  <E>                    the edges type
	 * @param  g                      a graph
	 * @param  w                      an edge weight function
	 * @return                        a result object containing information on the shortest path between each pair of
	 *                                vertices
	 * @throws NegativeCycleException if a negative cycle is detected in the graph
	 */
	public <V, E> ShortestPathAllPairs.Result<V, E> computeAllShortestPaths(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Compute the shortest path between each pair of vertices in a given subset of the vertices of the graph.
	 *
	 * <p>
	 * To compute the shortest cardinality (non weighted) paths, pass {@code null} instead of the weight function
	 * {@code w}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link ShortestPathAllPairs.IResult} object will be returned. In that
	 * case, its better to pass a {@link IWeightFunction} as {@code w} and {@link IntCollection} as
	 * {@code verticesSubset} to avoid boxing/unboxing.
	 *
	 * @param  <V>                    the vertices type
	 * @param  <E>                    the edges type
	 * @param  g                      a graph
	 * @param  verticesSubset         a subset of vertices of the graph. All shortest paths will be computed between
	 *                                    each pair of vertices from the subset
	 * @param  w                      as edge weight function
	 * @return                        a result object containing information on the shortest path between each pair of
	 *                                vertices in the subset
	 * @throws NegativeCycleException if a negative cycle is detected in the graph. If there is a negative cycle that is
	 *                                    not reachable from the set of given vertices, it might not be detected,
	 *                                    depending on the implementation
	 */
	public <V, E> ShortestPathAllPairs.Result<V, E> computeSubsetShortestPaths(Graph<V, E> g,
			Collection<V> verticesSubset, WeightFunction<E> w);

	/**
	 * A result object for an {@link ShortestPathAllPairs} algorithm.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	interface Result<V, E> {

		/**
		 * Get the distance of the shortest path between two vertices.
		 *
		 * @param  source                   the source vertex
		 * @param  target                   the target vertex
		 * @return                          the sum of weights of edges in the shortest path from the source to target,
		 *                                  or {@code Double.POSITIVE_INFINITY} if no such path exists
		 * @throws NoSuchVertexException    if {@code source} or {@code target} are not vertices in the graph
		 * @throws IllegalArgumentException if the shortest paths were computed on pairs of vertices from a subset of
		 *                                      the vertices of the graph (rather than all pairs), and {@code source} or
		 *                                      {@code target} are not in the subset
		 */
		public double distance(V source, V target);

		/**
		 * Get the shortest path between vertices.
		 *
		 * @param  source                   the source vertex
		 * @param  target                   the target vertex
		 * @return                          the shortest path from the source to target, or {@code null} if no such path
		 *                                  exists
		 * @throws NoSuchVertexException    if {@code source} or {@code target} are not vertices in the graph
		 * @throws IllegalArgumentException if the shortest paths were computed on pairs of vertices from a subset of
		 *                                      the vertices of the graph (rather than all pairs), and {@code source} or
		 *                                      {@code target} are not in the subset
		 */
		public Path<V, E> getPath(V source, V target);
	}

	/**
	 * A result object for an {@link ShortestPathAllPairs} algorithm for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	interface IResult extends ShortestPathAllPairs.Result<Integer, Integer> {

		/**
		 * Get the distance of the shortest path between two vertices.
		 *
		 * @param  source                   the source vertex
		 * @param  target                   the target vertex
		 * @return                          the sum of weights of edges in the shortest path from the source to target,
		 *                                  or {@code Double.POSITIVE_INFINITY} if no such path exists
		 * @throws NoSuchVertexException    if {@code source} or {@code target} are not vertices in the graph
		 * @throws IllegalArgumentException if the shortest paths were computed on pairs of vertices from a subset of
		 *                                      the vertices of the graph (rather than all pairs), and {@code source} or
		 *                                      {@code target} are not in the subset
		 */
		public double distance(int source, int target);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #distance(int, int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default double distance(Integer source, Integer target) {
			return distance(source.intValue(), target.intValue());
		}

		/**
		 * Get the shortest path between vertices.
		 *
		 * @param  source                   the source vertex
		 * @param  target                   the target vertex
		 * @return                          the shortest path from the source to target, or {@code null} if no such path
		 *                                  exists
		 * @throws NoSuchVertexException    if {@code source} or {@code target} are not vertices in the graph
		 * @throws IllegalArgumentException if the shortest paths were computed on pairs of vertices from a subset of
		 *                                      the vertices of the graph (rather than all pairs), and {@code source} or
		 *                                      {@code target} are not in the subset
		 */
		public IPath getPath(int source, int target);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #getPath(int, int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default IPath getPath(Integer source, Integer target) {
			return getPath(source.intValue(), target.intValue());
		}
	}

	/**
	 * Create a new all-pairs-shortest-paths algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathAllPairs} object. The
	 * {@link ShortestPathAllPairs.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ShortestPathAllPairs}
	 */
	static ShortestPathAllPairs newInstance() {
		return builder().build();
	}

	/**
	 * Create a new all pairs shortest paths algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ShortestPathAllPairs} objects
	 */
	static ShortestPathAllPairs.Builder builder() {
		return new ShortestPathAllPairs.Builder() {
			private boolean cardinalityWeight;
			String impl;

			@Override
			public ShortestPathAllPairs build() {
				if (impl != null) {
					switch (impl) {
						case "cardinality":
							return new ShortestPathAllPairsCardinality();
						case "floyd-warshall":
							return new ShortestPathAllPairsFloydWarshall();
						case "johnson":
							return new ShortestPathAllPairsJohnson();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				ShortestPathAllPairs cardinalityAlgo = new ShortestPathAllPairsCardinality();
				if (cardinalityWeight)
					return cardinalityAlgo;
				return new ShortestPathAllPairs() {
					final ShortestPathAllPairs weightedAlgo = new ShortestPathAllPairsJohnson();

					@Override
					public <V, E> ShortestPathAllPairs.Result<V, E> computeAllShortestPaths(Graph<V, E> g,
							WeightFunction<E> w) {
						if (WeightFunction.isCardinality(w)) {
							return cardinalityAlgo.computeAllShortestPaths(g, null);
						} else {
							return weightedAlgo.computeAllShortestPaths(g, w);
						}
					}

					@Override
					public <V, E> ShortestPathAllPairs.Result<V, E> computeSubsetShortestPaths(Graph<V, E> g,
							Collection<V> verticesSubset, WeightFunction<E> w) {
						if (WeightFunction.isCardinality(w)) {
							return cardinalityAlgo.computeSubsetShortestPaths(g, verticesSubset, null);
						} else {
							return weightedAlgo.computeSubsetShortestPaths(g, verticesSubset, w);
						}
					}
				};
			}

			@Override
			public ShortestPathAllPairs.Builder setCardinality(boolean cardinalityWeight) {
				this.cardinalityWeight = cardinalityWeight;
				return this;
			}

			@Override
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						ShortestPathAllPairs.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link ShortestPathAllPairs} objects.
	 *
	 * @see    ShortestPathAllPairs#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for all pairs shortest paths computation.
		 *
		 * @return a new all pairs shortest paths algorithm
		 */
		ShortestPathAllPairs build();

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
		ShortestPathAllPairs.Builder setCardinality(boolean cardinalityWeight);

	}

}
