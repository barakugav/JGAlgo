package com.ugav.algo;

import java.util.Collection;

import com.ugav.algo.Graph.Edge;

public interface MatchingBipartite {

	/**
	 * Calculate the maximum matching of unweighted undirected bipartite graph
	 *
	 * @param g a bipartite graph
	 * @return collection of edges representing a maximum matching
	 */
	public <E> Collection<Edge<E>> calcMaxMatching(GraphBipartite<E> g);

}
