package com.jgalgo;

public interface DiGraph extends Graph {

	/**
	 * Reverse an edge by switching its source and target
	 *
	 * @param edge an existing edge in the graph
	 */
	public void reverseEdge(int edge);

}
