package com.ugav.algo;

public class GraphTableUndirected<E> extends GraphTableAbstract<E> implements Graph.Undirected<E> {

	public GraphTableUndirected(int n) {
		super(n);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edges[u][v] = e;
		edges[v][u] = e;
		return e;
	}

}
