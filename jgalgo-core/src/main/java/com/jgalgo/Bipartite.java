package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;

class Bipartite {
	private Bipartite() {}

	static boolean isValidBipartitePartition(Graph g, Weights.Bool partition) {
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			if (partition.getBool(u) == partition.getBool(v))
				return false;
		}
		return false;
	}

}
