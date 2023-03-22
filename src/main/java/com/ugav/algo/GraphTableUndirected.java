package com.ugav.algo;

public class GraphTableUndirected extends GraphTableAbstract implements UGraph {

	public GraphTableUndirected(int n) {
		super(n);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edges[u][v] = edges[v][u] = e;
		return e;
	}

	@Override
	public void removeEdge(int e) {
		checkEdgeIdx(e);
		int lastEdge = edgesNum() - 1;
		if (e != lastEdge) {
			edgeSwap(e, lastEdge);
			e = lastEdge;
		}
		int u = edgeSource(e), v = edgeTarget(e);
		edges[u][v] = edges[v][u] = EdgeNone;
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edges[u1][v1] = edges[v1][u1] = e2;
		edges[u2][v2] = edges[v2][u2] = e1;
		super.edgeSwap(e1, e2);
	}

}
