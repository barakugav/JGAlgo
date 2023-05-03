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

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Maximum weighted matching algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), a matching is a sub set of edges \(M\) such that any vertex in \(V\) have at most one
 * adjacent edge in \(M\). A maximum matching is a matching with the maximum edges weight sum with respect to some
 * weight function. The 'maximum matching' with out weight is referred as 'maximum cardinality matching'.
 * <p>
 * A perfect maximum matching is a matching with the maximum edges weight sum out of all the matching with are maximum
 * cardinality matching. Note that the weight of a perfect maximum matching is smaller or equal to the weight of a
 * maximum weight matching.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Maximum_weight_matching">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MaximumMatchingWeighted extends MaximumMatching {

	/**
	 * Compute the maximum weighted matching of a weighted undirected graph.
	 *
	 * @param  g an undirected graph
	 * @param  w an edge weight function
	 * @return   collection of edges representing the matching
	 */
	public IntCollection computeMaximumMatching(UGraph g, EdgeWeightFunc w);

	/**
	 * Compute the maximum perfect matching of a weighted undirected graph.
	 *
	 * @param  g an undirected graph
	 * @param  w an edge weight function
	 * @return   collection of edges representing perfect matching, or the maximal one if no perfect one found
	 */
	public IntCollection computeMaximumPerfectMatching(UGraph g, EdgeWeightFunc w);

	/**
	 * {@inheritDoc}
	 * <p>
	 * Compute the maximum cardinality matching of a weighted undirected graph.
	 */
	@Override
	default IntCollection computeMaximumMatching(UGraph g) {
		return computeMaximumMatching(g, e -> 1);
	}

	/**
	 * Create a new maximum weighted matching algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximumMatchingWeighted} object.
	 *
	 * @return a new builder that can build {@link MaximumMatchingWeighted} objects
	 */
	static MaximumMatchingWeighted.Builder newBuilder() {
		return new MaximumMatchingWeighted.Builder() {

			boolean isBipartite = false;

			@Override
			public MaximumMatchingWeighted build() {
				return isBipartite ? new MaximumMatchingWeightedBipartiteHungarianMethod()
						: new MaximumMatchingWeightedGabow1990();
			}

			@Override
			public Builder setBipartite(boolean bipartite) {
				isBipartite = bipartite;
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MaximumMatchingWeighted} objects.
	 *
	 * @see    MaximumMatchingWeighted#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends MaximumMatching.Builder {

		/**
		 * Create a new maximum weighted matching algorithm object.
		 *
		 * @return a new maximum weighted matching algorithm
		 */
		@Override
		MaximumMatchingWeighted build();

		@Override
		MaximumMatchingWeighted.Builder setBipartite(boolean bipartite);
	}

}
