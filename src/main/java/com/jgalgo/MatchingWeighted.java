package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

public interface MatchingWeighted extends Matching {

	/**
	 * Calculate the maximum matching of a weighted undirected graph
	 *
	 * @param g a graph
	 * @param w weight function
	 * @return collection of edges representing the matching found
	 */
	public IntCollection calcMaxMatching(Graph g, EdgeWeightFunc w);

	/**
	 * Calculate the maximum perfect matching of a weighted undirected graph
	 *
	 * @param g a graph
	 * @param w weight function
	 * @return collection of edges representing perfect matching, or the maximal one
	 *         if no perfect one found
	 */
	public IntCollection calcPerfectMaxMatching(Graph g, EdgeWeightFunc w);

	@Override
	default IntCollection calcMaxMatching(Graph g) {
		return calcMaxMatching(g, e -> 1);
	}

}
