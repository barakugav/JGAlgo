package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum spanning tree algorithm.
 * <p>
 * A spanning tree is an edge sub set of the graph edges which form a tree and
 * connect (span) all the vertices of the graph. A minimum spanning tree (MST)
 * is a spanning tree with the minimum edge weights sum over all spanning trees.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Minimum_spanning_tree">Wikipedia</a>
 * @see MDST
 * @author Barak Ugav
 */
public interface MST {

	/**
	 * Compute the minimum spanning tree (MST) of a given graph.
	 *
	 * @param g a graph
	 * @param w a weight function
	 * @return all edges that compose the MST, which there are {@code n-1} of them
	 *         (or less, forming a forest if the graph is not connected)
	 */
	IntCollection computeMinimumSpanningTree(Graph g, EdgeWeightFunc w);

}
