package com.ugav.algo;

// TODO remove this class, represent the bipartitness as boolean vertices weights
public interface GraphBipartite extends Graph {

	/**
	 * Get the number of vertices in S side
	 *
	 * @return number of vertices in S side
	 */
	public int svertices();

	/**
	 * Get the number of vertices in T side
	 *
	 * @return number of vertices in T side
	 */

	public int tvertices();

	/**
	 * Check if a vertex is in S side
	 *
	 * @param v index of a vertex
	 * @return true if v is in S side
	 */
	public boolean isVertexInS(int v);

	@Deprecated
	@Override
	default int addVertex() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a new vertex in S
	 *
	 * @return index of the new vertex
	 */
	public int newVertexS();

	/**
	 * Create a new vertex in T
	 *
	 * @return index of the new vertex
	 */
	public int newVertexT();

	static interface DiGraph extends GraphBipartite, com.ugav.algo.DiGraph {
	}

	static interface UGraph extends GraphBipartite, com.ugav.algo.UGraph {
	}

}
