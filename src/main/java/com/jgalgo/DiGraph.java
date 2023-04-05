package com.jgalgo;

public interface DiGraph extends Graph {

	@Deprecated
	@Override
	default EdgeIter edges(int u) {
		return edgesOut(u);
	}

	/**
	 * Get the edges whose source is u
	 *
	 * @param u a source vertex
	 * @return an iterator of all the edges whose source is u
	 */
	public EdgeIter edgesOut(int u);

	/**
	 * Get the edges whose target is v
	 *
	 * @param v a target vertex
	 * @return an iterator of all the edges whose target is v
	 */
	public EdgeIter edgesIn(int v);

	@Override
	default void removeEdgesAll(int u) {
		removeEdgesAllOut(u);
		removeEdgesAllIn(u);
	}

	/**
	 * Remove all edges whose source is u.
	 *
	 * <p>
	 * This operation might have side effects, see {@link #removeEdgesAll(int)}.
	 *
	 * @param u a source vertex identifier.
	 */
	default void removeEdgesAllOut(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Remove all edges whose target is v.
	 *
	 * <p>
	 * This operation might have side effects, see {@link #removeEdgesAll(int)}.
	 *
	 * @param v a target vertex identifier.
	 */
	default void removeEdgesAllIn(int v) {
		for (EdgeIter eit = edgesIn(v); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	/**
	 * Reverse an edge by switching its source and target
	 *
	 * @param edge an existing edge in the graph
	 */
	public void reverseEdge(int edge);

	@Override
	default int degree(int u) {
		int count = 0;
		for (EdgeIter it = edgesOut(u); it.hasNext();) {
			it.nextInt();
			if (it.v() != u) /* Count self edges only once, in the in-edges loop */
				count++;
		}
		for (EdgeIter it = edgesIn(u); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;

	}

	/**
	 * Get the out degree of a source vertex
	 *
	 * @param u a source vertex
	 * @return the number of edges whose source is u
	 */
	default int degreeOut(int u) {
		int count = 0;
		for (EdgeIter it = edgesOut(u); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	/**
	 * Get the out degree of a target vertex
	 *
	 * @param v a target vertex
	 * @return the number of edges whose target is v
	 */
	default int degreeIn(int v) {
		int count = 0;
		for (EdgeIter it = edgesIn(v); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

}
