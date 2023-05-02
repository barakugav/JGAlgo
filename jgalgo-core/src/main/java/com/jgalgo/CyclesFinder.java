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
	 * @param  g a graph
	 * @return   a list of all cycles in the graph
	 */
	public List<Path> findAllCycles(Graph g);

	/**
	 * Create a new cycles finder algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link CyclesFinder} object.
	 *
	 * @return a new builder that can build {@link CyclesFinder} objects
	 */
	static CyclesFinder.Builder newBuilder() {
		return CyclesFinderTarjan::new;
	}

	/**
	 * A builder for {@link CyclesFinder} objects.
	 *
	 * @see    CyclesFinder#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for cycles computation.
		 *
		 * @return a new cycles finder algorithm
		 */
		CyclesFinder build();
	}

}
