package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Maximum weighted matching algorithm.
 * <p>
 * Given a graph {@code G=(V,E)}, a matching is a sub set of edges {@code M}
 * such that any vertex in {@code V} have at most one adjacent edge in
 * {@code M}. A maximum matching is a matching with the maximum edges weight sum
 * with respect to some weight function. The 'maximum matching' with out weight
 * is referred as 'maximum cardinality matching'.
 * <p>
 * A perfect maximum matching is a matching with the maximum edges weight sum
 * out of all the matching with are maximum cardinality matching. Note that the
 * weight of a perfect maximum matching is smaller or equal to the weight of a
 * maximum weight matching.
 * <p>
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Maximum_weight_matching">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MaximumMatchingWeighted extends MaximumMatching {

	/**
	 * Compute the maximum weighted matching of a weighted undirected graph.
	 * <p>
	 *
	 * @param g an undirected graph
	 * @param w an edge weight function
	 * @return collection of edges representing the matching
	 */
	public IntCollection computeMaximumMatching(UGraph g, EdgeWeightFunc w);

	/**
	 * Compute the maximum perfect matching of a weighted undirected graph.
	 * <p>
	 *
	 * @param g an undirected graph
	 * @param w an edge weight function
	 * @return collection of edges representing perfect matching, or the maximal one
	 *         if no perfect one found
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

}
