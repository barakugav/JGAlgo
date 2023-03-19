package com.ugav.algo;

public class GraphTableDirected<E> extends GraphTableAbstract<E> implements Graph.Directed<E> {

	public GraphTableDirected(int n) {
		super(n);
	}

	@Override
	public EdgeIter<E> edgesOut(int u) {
		return new EdgesOutItrVertex(u);
	}

	@Override
	public EdgeIter<E> edgesIn(int v) {
		return new EdgesInItrVertex(v);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edges[u][v] = e;
		return e;
	}

}
