package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Minimum Cut algorithm with terminal vertices (source-sink, S-T).
 * <p>
 * Given a graph {@code G=(V,E)}, a cut is a partition of {@code V} into two
 * sets {@code C, C' = V \ C}. Given a weight function, the weight of a cut
 * {@code (C,C')} is the weight of all edges {@code (u,v)} such that {@code u}
 * is in {@code C} and {@code v} is in {@code C'}. There are two variants of the
 * problem to find a minimum weight cut: (1) With terminal vertices, and (2)
 * without terminal vertices. In the variant with terminal vertices, we are
 * given two special vertices {@code source (S)} and {@code sink (T)} and we
 * need to find the minimum cut {@code (C,C')} such that the {@code source} is
 * in {@code C} and the {@code sink} is in {@code C'}. In the variant without
 * terminal vertices we need to find the global cut, and {@code C,C'} just must
 * not be empty.
 * <p>
 * Algorithm implementing this interface compute the minimum cut given two
 * terminal vertices, {@code source (S)} and {@code sink (T)}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Minimum_cut">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MinimumCutST {

	/**
	 * Compute the minimum cut in a graph and a weight function with two terminal
	 * vertices.
	 * <p>
	 * Given a graph {@code G=(V,E)}, a cut is a partition of {@code V} into twos
	 * sets {@code C, C' = V \ C}. The return value of this function is the set
	 * {@code C}, and {@code C'} can be computed easily by the caller if needed.
	 *
	 * @param g      a graph
	 * @param w      an edge weight function
	 * @param source a special vertex that will be in {@code C}
	 * @param sink   a special vertex that will be in {@code C'}
	 * @return the set C, where the minimum cut is partition of the vertices
	 *         {@code V} into two sets {@code C, C' = V \ C}
	 * @throws IllegalArgumentException if the source and the sink are the same
	 *                                  vertex
	 */
	IntList computeMinimumCut(Graph g, EdgeWeightFunc w, int source, int sink);

	/**
	 * Create a new minimum cut algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumCutST}
	 * object.
	 *
	 * @return a new builder that can build {@link MinimumCutST} objects
	 */
	static MinimumCutST.Builder newBuilder() {
		return new MinimumCutSTBuilderImpl.Default();
	}

	/**
	 * Create a new minimum cut algorithm using a maximum flow algorithm.
	 * <p>
	 * By first computing a maximum flow between the source and the sink, the
	 * minimum cut can be realized from the maximum flow without increasing the
	 * asymptotical running time of the maximum flow algorithm running time.
	 *
	 * @param maxFlowAlg a maximum flow algorithm
	 * @return a minimum cut algorithm based on the provided maximum flow algorithm
	 */
	static MinimumCutST newFromMaximumFlow(MaximumFlow maxFlowAlg) {
		return MinimumCutSTBuilderImpl.buildFromMaxFlow(maxFlowAlg);
	}

	/**
	 * A builder for {@link MinimumCutST} objects.
	 *
	 * @see MinimumCutST#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum cut computation.
		 *
		 * @return a new minimum cut algorithm
		 */
		MinimumCutST build();
	}

}
