package com.ugav.jgalgo;

import com.ugav.jgalgo.Graphs.BFSIter;

public class SSSPCardinality {

	public SSSP.Result calcDistances(Graph g, int source) {
		SSSPResultImpl.Int res = new SSSPResultImpl.Int(g);
		for (BFSIter it = new BFSIter(g, source); it.hasNext();) {
			int v = it.nextInt();
			res.distances[v] = it.layer();
			res.backtrack[v] = it.inEdge();
		}
		return res;
	}

}
