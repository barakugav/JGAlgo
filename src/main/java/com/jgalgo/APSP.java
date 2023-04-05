package com.jgalgo;

public interface APSP {

	public APSP.Result calcDistances(Graph g, EdgeWeightFunc w);

	interface Result {

		public double distance(int source, int target);

		public Path getPath(int source, int target);

		public boolean foundNegativeCycle();

		public Path getNegativeCycle();
	}

}
