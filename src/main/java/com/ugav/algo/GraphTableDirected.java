package com.ugav.algo;

public class GraphTableDirected extends GraphTableAbstract implements Graph.Directed {

	public GraphTableDirected(int n) {
		super(n);
	}

	@Override
	public EdgeIter edgesOut(int u) {
		return new EdgesOutItrVertex(u);
	}

	@Override
	public EdgeIter edgesIn(int v) {
		return new EdgesInItrVertex(v);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edges[u][v] = e;
		return e;
	}

}
