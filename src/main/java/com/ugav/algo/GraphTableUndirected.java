package com.ugav.algo;

public class GraphTableUndirected extends GraphTableAbstract implements Graph.Undirected {

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
		int lastEdge = edges() - 1;
		if (e != lastEdge) {
			edgeSwap(e, lastEdge);
			e = lastEdge;
		}
		int u = getEdgeSource(e), v = getEdgeTarget(e);
		edges[u][v] = edges[v][u] = EdgeNone;
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = getEdgeSource(e1), v1 = getEdgeTarget(e1);
		int u2 = getEdgeSource(e2), v2 = getEdgeTarget(e2);
		edges[u1][v1] = edges[v1][u1] = e2;
		edges[u2][v2] = edges[v2][u2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdges(int u) {
		for (EdgeIter eit = edges(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

}
