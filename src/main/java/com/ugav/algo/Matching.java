package com.ugav.algo;

import java.util.Collection;

import com.ugav.algo.Graph.Edge;

public interface Matching {

	/**
	 * Calculate the maximum matching of unweighted undirected graph
	 *
	 * @param g a graph
	 * @return collection of edges representing a maximum matching
	 */
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g);

}
