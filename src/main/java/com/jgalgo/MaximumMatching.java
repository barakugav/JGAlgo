package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Maximum matching algorithm.
 * <p>
 * Given a graph {@code G=(V,E)}, a matching is a sub set of edges {@code M}
 * such that any vertex in {@code V} have at most one adjacent edge in
 * {@code M}. A maximum matching is a matching with the maximum number of edges
 * in {@code M}.
 * <p>
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MaximumMatching {

	/**
	 * Compute the maximum matching of unweighted undirected graph.
	 *
	 * @param g an undirected graph
	 * @return collection of edges representing a maximum matching
	 */
	public IntCollection computeMaximumMatching(UGraph g);

}
