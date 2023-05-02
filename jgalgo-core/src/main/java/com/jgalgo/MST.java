package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum spanning tree algorithm.
 * <p>
 * A spanning tree is an edge sub set of the graph edges which form a tree and connect (span) all the vertices of the
 * graph. A minimum spanning tree (MST) is a spanning tree with the minimum edge weights sum over all spanning trees.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Minimum_spanning_tree">Wikipedia</a>
 * @see    MDST
 * @author Barak Ugav
 */
public interface MST {

	/**
	 * Compute the minimum spanning tree (MST) of a given graph.
	 *
	 * @param  g a graph
	 * @param  w an edge weight function
	 * @return   all edges that compose the MST, which there are \(n-1\) of them (or less, forming a forest if the graph
	 *           is not connected)
	 */
	IntCollection computeMinimumSpanningTree(Graph g, EdgeWeightFunc w);

	/**
	 * Create a new minimum spanning tree algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MST} object.
	 *
	 * @return a new builder that can build {@link MST} objects
	 */
	static MST.Builder newBuilder() {
		return MSTPrim::new;
	}

	/**
	 * A builder for {@link MST} objects.
	 *
	 * @see    MST#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum spanning tree computation.
		 *
		 * @return a new minimum spanning tree algorithm
		 */
		MST build();
	}

}
