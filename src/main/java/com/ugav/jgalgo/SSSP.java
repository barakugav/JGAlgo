package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntList;

/* Single Source Shortest Path */
public interface SSSP {

	public Result calcDistances(Graph g, EdgeWeightFunc w, int source);

	public static interface Result {

		public double distance(int v);

		public IntList getPathTo(int v);

		public boolean foundNegativeCycle();

		public IntList getNegativeCycle();

	}

}
