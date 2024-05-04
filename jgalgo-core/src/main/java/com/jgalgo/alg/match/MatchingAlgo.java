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

package com.jgalgo.alg.match;

import java.util.function.Supplier;
import com.jgalgo.alg.AlgorithmBuilderBase;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Maximum/minimum matching algorithm.
 *
 * <p>
 * Given a graph \(G=(V,E)\), a matching is a sub set of edges \(M\) such that any vertex in \(V\) has at most one
 * adjacent edge in \(M\). A maximum cardinality matching is a matching with the maximum <b>number</b> of edges in
 * \(M\). A maximum/minimum weighted matching is a matching with the maximum/minimum edges weight sum with respect to
 * some weight function. A perfect maximum/minimum weighted matching is a matching with the maximum/minimum edges weight
 * sum out of all the matchings in which each vertex has an adjacent matched edge. Note that the weight of a perfect
 * maximum matching is smaller or equal to the weight of a maximum weight matching.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MatchingAlgo {

	/**
	 * Compute the maximum weighted matching of a weighted undirected graph.
	 *
	 * <p>
	 * To compute the maximum cardinality (non weighted) matching, pass {@code null} instead of the weight function
	 * {@code w}.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IMatching}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	<V, E> Matching<V, E> computeMaximumMatching(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Compute the minimum weighted matching of a weighted undirected graph.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IMatching}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	<V, E> Matching<V, E> computeMinimumMatching(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Compute the maximum perfect matching of a weighted undirected graph.
	 *
	 * <p>
	 * A perfect matching in which each vertex has an adjacent matched edge is assumed to exist in the input graph, and
	 * if no such matching exist the behavior is undefined.
	 *
	 * <p>
	 * To compute the maximum cardinality (non weighted) perfect matching, pass {@code null} instead of the weight
	 * function {@code w}.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IMatching}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed perfect matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	<V, E> Matching<V, E> computeMaximumPerfectMatching(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Compute the minimum perfect matching of a weighted undirected graph.
	 *
	 * <p>
	 * A perfect matching in which each vertex has an adjacent matched edge is assumed to exist in the input graph, and
	 * if no such matching exist the behavior is undefined.
	 *
	 * <p>
	 * To compute the maximum cardinality (non weighted) matching, pass {@code null} instead of the weight function
	 * {@code w}. Note that this is equivalent to {@link #computeMaximumPerfectMatching(Graph, WeightFunction)}.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IMatching}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed perfect matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	<V, E> Matching<V, E> computeMinimumPerfectMatching(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Create a new matching algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MatchingAlgo} object. The {@link MatchingAlgo.Builder}
	 * might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MatchingAlgo}
	 */
	static MatchingAlgo newInstance() {
		return builder().build();
	}

	/**
	 * Create a new matching algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MatchingAlgo} objects
	 */
	static MatchingAlgo.Builder builder() {
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
							return new MatchingWeightedBipartiteSssp2();
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
				Supplier<MatchingAlgo> cardinalityGeneralAlgo = MatchingCardinalityGabow1976::new;
				Supplier<MatchingAlgo> cardinalityBipartiteAlgo = MatchingCardinalityBipartiteHopcroftKarp::new;
				Supplier<MatchingAlgo> weightedGeneralAlgo = MatchingWeightedBlossomV::new;
				Supplier<MatchingAlgo> weightedBipartiteAlgo = weightedGeneralAlgo;

				if (cardinality) {
					if (isBipartite) {
						return cardinalityBipartiteAlgo.get();

					} else {
						MatchingAlgo a = cardinalityGeneralAlgo.get();
						MatchingAlgo b = cardinalityBipartiteAlgo.get();
						return new Matchings.SuperImpl(a, b, a, b);

					}
				} else {
					if (isBipartite) {
						MatchingAlgo a = cardinalityBipartiteAlgo.get();
						MatchingAlgo b = weightedBipartiteAlgo.get();
						return new Matchings.SuperImpl(a, a, b, b);

					} else {
						MatchingAlgo a = cardinalityGeneralAlgo.get();
						MatchingAlgo b = cardinalityBipartiteAlgo.get();
						MatchingAlgo c = weightedGeneralAlgo.get();
						MatchingAlgo d = weightedBipartiteAlgo.get();
						return new Matchings.SuperImpl(a, b, c, d);
					}
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
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						MatchingAlgo.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link MatchingAlgo} objects.
	 *
	 * @see    MatchingAlgo#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new matching algorithm object.
		 *
		 * @return a new matching algorithm
		 */
		MatchingAlgo build();

		/**
		 * Set whether the matching algorithms built by this builder should only support bipartite graphs.
		 *
		 * <p>
		 * If the input graphs are known to be bipartite, simpler or more efficient algorithm may exists.
		 *
		 * @param  bipartite if {@code true}, the created matching algorithms will support bipartite graphs only
		 * @return           this builder
		 */
		MatchingAlgo.Builder setBipartite(boolean bipartite);

		/**
		 * Set whether the matching algorithms built by this builder should support only maximum cardinality matching.
		 *
		 * <p>
		 * For cardinality weights, simpler or more efficient algorithm may exists.
		 *
		 * @param  cardinality if {@code true}, the created matching algorithms will support maximum cardinality
		 *                         matching only
		 * @return             this builder
		 */
		MatchingAlgo.Builder setCardinality(boolean cardinality);
	}

}
