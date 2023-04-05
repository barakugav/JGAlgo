package com.jgalgo;

/* Single Source Shortest Path */
public interface SSSP {

	public Result calcDistances(Graph g, EdgeWeightFunc w, int source);

	public static interface Result {

		public double distance(int target);

		public Path getPathTo(int target);

		public boolean foundNegativeCycle();

		public Path getNegativeCycle();

	}

}
