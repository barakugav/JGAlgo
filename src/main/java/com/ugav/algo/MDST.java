package com.ugav.algo;

import java.util.Collection;

import com.ugav.algo.Graph.Edge;

/* Directed version of MST */
public interface MDST extends MST {

	/**
	 * Calculate MST from some vertex in the graph.
	 */
	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w);

	/**
	 * Calculate minimum spanning tree (MST) in directed graph root at the given
	 * vertex
	 *
	 * @param g    a directed graph
	 * @param w    weight function
	 * @param root vertex in the graph the spanning tree will be rooted from
	 * @return all edges composing the spanning tree
	 */
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w, int root);

}
