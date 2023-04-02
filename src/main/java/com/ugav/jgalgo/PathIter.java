package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntList;

public interface PathIter {

	public boolean hasNext();

	public int nextEdge();

	public int u();

	public int v();

	public static PathIter of(Graph g0, IntList edgeList) {
		if (g0 instanceof UGraph g) {
			return new PathIterImpl.Undirected(g, edgeList);
		} else if (g0 instanceof DiGraph g) {
			return new PathIterImpl.Directed(g, edgeList);
		} else {
			throw new IllegalArgumentException();
		}
	}

}