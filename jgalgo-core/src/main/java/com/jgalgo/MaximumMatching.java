package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Maximum matching algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), a matching is a sub set of edges \(M\) such that any vertex in \(V\) have at most one
 * adjacent edge in \(M\). A maximum matching is a matching with the maximum number of edges in \(M\).
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MaximumMatching {

	/**
	 * Compute the maximum matching of unweighted undirected graph.
	 *
	 * @param  g an undirected graph
	 * @return   collection of edges representing a maximum matching
	 */
	public IntCollection computeMaximumMatching(UGraph g);

	/**
	 * Create a new maximum matching algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximumMatching} object.
	 *
	 * @return a new builder that can build {@link MaximumMatching} objects
	 */
	static MaximumMatching.Builder newBuilder() {
		return new MaximumMatching.Builder() {

			boolean isBipartite = false;

			@Override
			public MaximumMatching build() {
				return isBipartite ? new MaximumMatchingBipartiteHopcroftKarp() : new MaximumMatchingGabow1976();
			}

			@Override
			public Builder setBipartite(boolean bipartite) {
				isBipartite = bipartite;
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
	static interface Builder {

		/**
		 * Create a new maximum matching algorithm object.
		 *
		 * @return a new maximum matching algorithm
		 */
		MaximumMatching build();

		/**
		 * Set whether the maximum matching objects should support only bipartite graphs.
		 * <p>
		 * If the input graphs are known to be bipartite, simpler or more efficient algorithm may exists.
		 *
		 * @param  bipartite if {@code true}, the create maximum matching objects will support bipartite graphs only
		 * @return           this builder
		 */
		MaximumMatching.Builder setBipartite(boolean bipartite);
	}

}
