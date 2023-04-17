package com.jgalgo;

import java.util.List;

/**
 * An algorithm that finds all cycles in a graph.
 *
 * @author Barak Ugav
 */
public interface CyclesFinder {

	/**
	 * Find all cycles in the given graph.
	 *
	 * @param g a graph
	 * @return a list of all cycles in the graph
	 */
	public List<Path> findAllCycles(Graph g);

}
