package com.ugav.jgalgo;

import java.util.Arrays;

import com.ugav.jgalgo.Graphs.BFSIter;

public class SSSPCardinality {

	public SSSP.Result calcDistances(Graph g, int source) {
		int n = g.vertices().size();
		int[] distances = new int[n];
		int[] backtrack = new int[n];
		Arrays.fill(distances, 0, n, Integer.MAX_VALUE);
		Arrays.fill(backtrack, 0, n, -1);
		for (BFSIter it = new BFSIter(g, source); it.hasNext();) {
			int v = it.nextInt();
			distances[v] = it.layer();
			backtrack[v] = it.inEdge();
		}
		return new SSSPResultImpl.Int(g, distances, backtrack);
	}

}
