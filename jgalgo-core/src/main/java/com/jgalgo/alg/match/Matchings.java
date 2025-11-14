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

import java.util.Objects;
import com.jgalgo.alg.bipartite.BipartiteGraphs;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;

class Matchings {

	private Matchings() {}

	static class SuperImpl implements MatchingAlgo {

		private final MatchingAlgo cardinalityGeneralAlgo;
		private final MatchingAlgo cardinalityBipartiteAlgo;
		private final MatchingAlgo weightedGeneralAlgo;
		private final MatchingAlgo weightedBipartiteAlgo;

		SuperImpl(MatchingAlgo cardinalityGeneralAlgo, MatchingAlgo cardinalityBipartiteAlgo,
				MatchingAlgo weightedGeneralAlgo, MatchingAlgo weightedBipartiteAlgo) {
			this.cardinalityGeneralAlgo = Objects.requireNonNull(cardinalityGeneralAlgo);
			this.cardinalityBipartiteAlgo = Objects.requireNonNull(cardinalityBipartiteAlgo);
			this.weightedGeneralAlgo = Objects.requireNonNull(weightedGeneralAlgo);
			this.weightedBipartiteAlgo = Objects.requireNonNull(weightedBipartiteAlgo);
		}

		@Override
		public <V, E> Matching<V, E> computeMaximumMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMaximumMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMaximumMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMaximumMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMaximumMatching(g, w);
				}
			}
		}

		@Override
		public <V, E> Matching<V, E> computeMinimumMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMinimumMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMinimumMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMinimumMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMinimumMatching(g, w);
				}
			}
		}

		@Override
		public <V, E> Matching<V, E> computeMaximumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMaximumPerfectMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMaximumPerfectMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMaximumPerfectMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMaximumPerfectMatching(g, w);
				}
			}
		}

		@Override
		public <V, E> Matching<V, E> computeMinimumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMinimumPerfectMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMinimumPerfectMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMinimumPerfectMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMinimumPerfectMatching(g, w);
				}
			}
		}

		private static boolean isBipartite(Graph<?, ?> g) {
			return BipartiteGraphs.getExistingPartition(g).isPresent();
		}

	}

}
