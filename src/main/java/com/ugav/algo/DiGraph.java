package com.ugav.algo;

public interface DiGraph extends Graph {

	@Deprecated
	@Override
	default EdgeIter edges(int u) {
		return edgesOut(u);
	}

	public EdgeIter edgesOut(int u);

	public EdgeIter edgesIn(int v);

	@Override
	default void removeEdgesAll(int u) {
		removeEdgesAllOut(u);
	}

	public void removeEdgesAllOut(int u);

	public void removeEdgesAllIn(int v);

	public void reverseEdge(int e);

	@Override
	@Deprecated
	default int degree(int u) {
		return degreeOut(u);
	}

	default int degreeOut(int u) {
		int count = 0;
		for (EdgeIter it = edgesOut(u); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	default int degreeIn(int v) {
		int count = 0;
		for (EdgeIter it = edgesIn(v); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

}
