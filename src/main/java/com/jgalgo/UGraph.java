package com.jgalgo;

public interface UGraph extends Graph {

	@Override
	default EdgeIter edgesIn(int v) {
		return edgesOut(v);
	}

	@Override
	default void removeEdgesAll(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	default void removeEdgesAllOut(int u) {
		removeEdgesAll(u);
	}

	@Override
	default void removeEdgesAllIn(int v) {
		removeEdgesAll(v);
	}

	@Override
	default int degreeIn(int v) {
		return degreeOut(v);
	}

}
