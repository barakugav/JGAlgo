package com.ugav.algo;

import java.util.Collection;


import com.ugav.algo.Graph.WeightFunction;

public interface MatchingWeighted extends Matching {

	/**
	 * Calculate the maximum matching of a weighted undirected graph
	 *
	 * @param g a graph
	 * @param w weight function
	 * @return collection of edges representing the matching found
	 */
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g, WeightFunction<E> w);

	/**
	 * Calculate the maximum perfect matching of a weighted undirected graph
	 *
	 * @param g a graph
	 * @param w weight function
	 * @return collection of edges representing perfect matching, or the maximal one
	 *         if no perfect one found
	 */
	public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w);

	@Override
	default <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g) {
		return calcMaxMatching(g, e -> 1);
	}

}
