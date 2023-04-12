package com.jgalgo;

public interface UGraph extends Graph {

	@Override
	default EdgeIter edgesIn(int v) {
		return edgesOut(v);
	}

	@Override
	default void removeEdges(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	default void removeEdgesOut(int u) {
		removeEdges(u);
	}

	@Override
	default void removeEdgesIn(int v) {
		removeEdges(v);
	}

	@Override
	default int degreeIn(int v) {
		return degreeOut(v);
	}

}
