package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntList;

public interface APSP {

	public APSP.Result calcDistances(Graph g, EdgeWeightFunc w);

	interface Result {

		public double distance(int source, int target);

		public IntList getPath(int source, int target);

		public boolean foundNegativeCycle();

		public IntList getNegativeCycle();
	}

}
