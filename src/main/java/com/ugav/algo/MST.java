package com.ugav.algo;

import java.util.Collection;

import com.ugav.algo.Graph.Edge;

public interface MST {

	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w);

}
