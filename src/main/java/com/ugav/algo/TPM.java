package com.ugav.algo;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

/* Tree-Path Maxima */
public interface TPM {

	public <E> Edge<E>[] calcTPM(Graph<E> t, WeightFunction<E> w, int[] queries, int queriesNum);

}
