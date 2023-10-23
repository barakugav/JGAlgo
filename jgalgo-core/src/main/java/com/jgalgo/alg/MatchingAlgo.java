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
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Maximum/minimum matching algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), a matching is a sub set of edges \(M\) such that any vertex in \(V\) has at most one
 * adjacent edge in \(M\). A maximum cardinality matching is a matching with the maximum <b>number</b> of edges in
 * \(M\). A maximum/minimum weighted matching is a matching with the maximum/minimum edges weight sum with respect to
 * some weight function. A perfect maximum/minimum weighted matching is a matching with the maximum/minimum edges weight
 * sum out of all the matchings in which each vertex has an adjacent matched edge. Note that the weight of a perfect
 * maximum matching is smaller or equal to the weight of a maximum weight matching.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MatchingAlgo {

	/**
	 * Compute the maximum matching of unweighted undirected graph.
	 *
	 * @param  g                        an undirected graph
	 * @return                          the computed matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	Matching computeMaximumCardinalityMatching(Graph g);

	/**
	 * Compute the maximum weighted matching of a weighted undirected graph.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	Matching computeMaximumWeightedMatching(Graph g, WeightFunction w);

	/**
	 * Compute the minimum weighted matching of a weighted undirected graph.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	Matching computeMinimumWeightedMatching(Graph g, WeightFunction w);

	/**
	 * Compute the maximum perfect matching of a weighted undirected graph.
	 * <p>
	 * A perfect matching in which each vertex has an adjacent matched edge is assumed to exist in the input graph, and
	 * if no such matching exist the behavior is undefined.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed perfect matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	Matching computeMaximumWeightedPerfectMatching(Graph g, WeightFunction w);

	/**
	 * Compute the minimum perfect matching of a weighted undirected graph.
	 * <p>
	 * A perfect matching in which each vertex has an adjacent matched edge is assumed to exist in the input graph, and
	 * if no such matching exist the behavior is undefined.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed perfect matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	Matching computeMinimumWeightedPerfectMatching(Graph g, WeightFunction w);

	/**
	 * Create a new matching algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MatchingAlgo} object. The {@link MatchingAlgo.Builder}
	 * might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MatchingAlgo}
	 */
	static MatchingAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new matching algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MatchingAlgo} objects
	 */
	static MatchingAlgo.Builder newBuilder() {
		return new MatchingAlgo.Builder() {

			boolean cardinality = false;
			boolean isBipartite = false;
			String impl;

			@Override
			public MatchingAlgo build() {
				if (impl != null) {
					switch (impl) {
						case "cardinality-bipartite-hopcroft-karp":
							return new MatchingCardinalityBipartiteHopcroftKarp();
						case "cardinality-gabow-1976":
							return new MatchingCardinalityGabow1976();
						case "bipartite-hungarian-method":
							return new MatchingWeightedBipartiteHungarianMethod();
						case "bipartite-sssp":
							return new MatchingWeightedBipartiteSSSP();
						case "gabow-1990":
							return new MatchingWeightedGabow1990();
						case "gabow-1990-simpler":
							return new MatchingWeightedGabow1990Simpler();
						case "blossom-v":
							return new MatchingWeightedBlossomV();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				final MatchingAlgo cardinalityAlgo = isBipartite ? new MatchingCardinalityBipartiteHopcroftKarp()
						: new MatchingCardinalityGabow1976();
				if (cardinality) {
					return cardinalityAlgo;
				} else {
					final MatchingAlgo weightedAlgo = new MatchingWeightedBlossomV();
					return new MatchingAlgo() {

						@Override
						public Matching computeMaximumCardinalityMatching(Graph g) {
							return cardinalityAlgo.computeMaximumCardinalityMatching(g);
						}

						@Override
						public Matching computeMaximumWeightedMatching(Graph g, WeightFunction w) {
							boolean isCardinality = w == null || w == WeightFunction.CardinalityWeightFunction;
							return isCardinality ? cardinalityAlgo.computeMaximumCardinalityMatching(g)
									: weightedAlgo.computeMaximumWeightedMatching(g, w);
						}

						@Override
						public Matching computeMinimumWeightedMatching(Graph g, WeightFunction w) {
							boolean isCardinality = w == null || w == WeightFunction.CardinalityWeightFunction;
							return isCardinality ? new Matchings.MatchingImpl(g.indexGraph(), IntLists.emptyList())
									: weightedAlgo.computeMinimumWeightedMatching(g, w);
						}

						@Override
						public Matching computeMaximumWeightedPerfectMatching(Graph g, WeightFunction w) {
							boolean isCardinality = w == null || w == WeightFunction.CardinalityWeightFunction;
							return isCardinality ? cardinalityAlgo.computeMaximumCardinalityMatching(g)
									: weightedAlgo.computeMaximumWeightedPerfectMatching(g, w);
						}

						@Override
						public Matching computeMinimumWeightedPerfectMatching(Graph g, WeightFunction w) {
							boolean isCardinality = w == null || w == WeightFunction.CardinalityWeightFunction;
							return isCardinality ? cardinalityAlgo.computeMaximumCardinalityMatching(g)
									: weightedAlgo.computeMinimumWeightedPerfectMatching(g, w);
						}

					};
				}
			}

			@Override
			public MatchingAlgo.Builder setBipartite(boolean bipartite) {
				isBipartite = bipartite;
				return this;
			}

			@Override
			public MatchingAlgo.Builder setCardinality(boolean cardinality) {
				this.cardinality = cardinality;
				return this;
			}

			@Override
			public MatchingAlgo.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MatchingAlgo} objects.
	 *
	 * @see    MatchingAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new matching algorithm object.
		 *
		 * @return a new matching algorithm
		 */
		MatchingAlgo build();

		/**
		 * Set whether the matching algorithms built by this builder should only support bipartite graphs.
		 * <p>
		 * If the input graphs are known to be bipartite, simpler or more efficient algorithm may exists.
		 *
		 * @param  bipartite if {@code true}, the created matching algorithms will support bipartite graphs only
		 * @return           this builder
		 */
		MatchingAlgo.Builder setBipartite(boolean bipartite);

		/**
		 * Set whether the matching algorithms built by this builder should support only maximum cardinality matching.
		 * <p>
		 * For cardinality weights, simpler or more efficient algorithm may exists.
		 *
		 * @param  cardinality if {@code true}, the created matching algorithms will support maximum cardinality
		 *                         matching only
		 * @return             this builder
		 */
		MatchingAlgo.Builder setCardinality(boolean cardinality);

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default MatchingAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
