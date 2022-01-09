package com.ugav.algo;

import java.util.List;

import com.ugav.algo.Graph.WeightFunction;

/* Single Source Shortest Path */
public interface SSSP {

	public <E> Result<E> calcDistances(Graph<E> g, WeightFunction<E> w, int s);

	public static interface Result<E> {

		public double distance(int t);

		public List<Graph.Edge<E>> getPathTo(int t);

		public boolean foundNegativeCircle();

		public List<Graph.Edge<E>> getNegativeCircle();

	}

}
