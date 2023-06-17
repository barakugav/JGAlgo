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
 * Maximum matching algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), a matching is a sub set of edges \(M\) such that any vertex in \(V\) has at most one
 * adjacent edge in \(M\). A maximum cardinality matching is a matching with the maximum <b>number</b> of edges in
 * \(M\). A maximum weighted matching is a matching with the maximum edges weight sum with respect to some weight
 * function. A perfect maximum weighted matching is a matching with the maximum edges weight sum out of all the matching
 * with are maximum cardinality matching. Note that the weight of a perfect maximum matching is smaller or equal to the
 * weight of a maximum weight matching.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MaximumMatching {

	/**
	 * Compute the maximum matching of unweighted undirected graph.
	 *
	 * @param  g                        an undirected graph
	 * @return                          the computed matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	public Matching computeMaximumCardinalityMatching(Graph g);

	/**
	 * Compute the maximum weighted matching of a weighted undirected graph.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed matching
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	public Matching computeMaximumWeightedMatching(Graph g, WeightFunction w);

	/**
	 * Compute the maximum perfect matching of a weighted undirected graph.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @return                          the computed perfect matching, or the maximal one if no perfect one found
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	public Matching computeMaximumWeightedPerfectMatching(Graph g, WeightFunction w);

	/**
	 * Create a new maximum matching algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximumMatching} object.
	 *
	 * @return a new builder that can build {@link MaximumMatching} objects
	 */
	static MaximumMatching.Builder newBuilder() {
		return new MaximumMatching.Builder() {

			boolean cardinality = false;
			boolean isBipartite = false;
			String impl;

			@Override
			public MaximumMatching build() {
				if (impl != null) {
					if ("CardinalityBipartiteHopcroftKarp".equals(impl))
						return new MaximumMatchingCardinalityBipartiteHopcroftKarp();
					if ("CardinalityGabow1976".equals(impl))
						return new MaximumMatchingCardinalityGabow1976();
					if ("BipartiteHungarianMethod".equals(impl))
						return new MaximumMatchingWeightedBipartiteHungarianMethod();
					if ("Gabow1990".equals(impl))
						return new MaximumMatchingWeightedGabow1990();
					if ("Gabow1990Simpler".equals(impl))
						return new MaximumMatchingWeightedGabow1990Simpler();
					throw new IllegalArgumentException("unknown 'impl' value: " + impl);
				}
				if (cardinality) {
					return isBipartite ? new MaximumMatchingCardinalityBipartiteHopcroftKarp()
							: new MaximumMatchingCardinalityGabow1976();
				} else {
					return isBipartite ? new MaximumMatchingWeightedBipartiteHungarianMethod()
							: new MaximumMatchingWeightedGabow1990Simpler();
				}
			}

			@Override
			public MaximumMatching.Builder setBipartite(boolean bipartite) {
				isBipartite = bipartite;
				return this;
			}

			@Override
			public MaximumMatching.Builder setCardinality(boolean cardinality) {
				this.cardinality = cardinality;
				return this;
			}

			@Override
			public MaximumMatching.Builder setOption(String key, Object value) {
				if ("impl".equals(key)) {
					impl = (String) value;
				} else {
					throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MaximumMatching} objects.
	 *
	 * @see    MaximumMatching#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<MaximumMatching.Builder> {

		/**
		 * Create a new maximum matching algorithm object.
		 *
		 * @return a new maximum matching algorithm
		 */
		MaximumMatching build();

		/**
		 * Set whether the maximum matching algorithms built by this builder should support only bipartite graphs.
		 * <p>
		 * If the input graphs are known to be bipartite, simpler or more efficient algorithm may exists.
		 *
		 * @param  bipartite if {@code true}, the created maximum matching algorithms will support bipartite graphs only
		 * @return           this builder
		 */
		MaximumMatching.Builder setBipartite(boolean bipartite);

		/**
		 * Set whether the maximum matching algorithms built by this builder should support only maximum cardinality
		 * matching.
		 * <p>
		 * For cardinality weights, simpler or more efficient algorithm may exists.
		 *
		 * @param  cardinality if {@code true}, the created maximum matching algorithms will support maximum cardinality
		 *                         matching only
		 * @return             this builder
		 */
		MaximumMatching.Builder setCardinality(boolean cardinality);
	}

}
