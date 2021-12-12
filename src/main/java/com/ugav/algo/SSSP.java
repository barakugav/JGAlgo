package com.ugav.algo;

import java.util.Collection;

import com.ugav.algo.Graph.WeightFunction;

/* Single Source Shortest Path */
public interface SSSP {

	public <E> Result<E> calcDistances(Graph<E> g, WeightFunction<E> w, int s);

	public static interface Result<E> {

		public double distance(int t);

		public Collection<Graph.Edge<E>> getPathTo(int t);

		public boolean foundNegativeCircle();

		public Collection<Graph.Edge<E>> getNegativeCircle();

	}

}
