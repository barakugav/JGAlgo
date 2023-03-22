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

	default void removeEdgesAllOut(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	default void removeEdgesAllIn(int v) {
		for (EdgeIter eit = edgesIn(v); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

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
