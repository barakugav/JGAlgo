package com.ugav.algo;

public class GraphTableDirected extends GraphTableAbstract implements DiGraph {

	public GraphTableDirected(int n) {
		super(n);
	}

	@Override
	public EdgeIter edgesOut(int u) {
		return new EdgeIterOut(u);
	}

	@Override
	public EdgeIter edgesIn(int v) {
		return new EdgeIterIn(v);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edges[u][v] = e;
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
		edges[u][v] = EdgeNone;
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edges[u1][v1] = e2;
		edges[u2][v2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void reverseEdge(int e) {
		int u = edgeSource(e), v = edgeTarget(e);
		if (edges[v][u] != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		edges[v][u] = e;
		edges[u][v] = EdgeNone;
		super.reverseEdge(e);
	}

}
