package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

public interface Matching {

	/**
	 * Calculate the maximum matching of unweighted undirected graph
	 *
	 * @param g a graph
	 * @return collection of edges representing a maximum matching
	 */
	public IntCollection calcMaxMatching(Graph g);

}
