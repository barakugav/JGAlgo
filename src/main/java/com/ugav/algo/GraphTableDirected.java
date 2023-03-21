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

	@Override
	public void removeEdge(int e) {
		checkEdgeIdx(e);
		int lastEdge = edges() - 1;
		if (e != lastEdge) {
			edgeSwap(e, lastEdge);
			e = lastEdge;
		}
		int u = getEdgeSource(e), v = getEdgeTarget(e);
		edges[u][v] = EdgeNone;
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = getEdgeSource(e1), v1 = getEdgeTarget(e1);
		int u2 = getEdgeSource(e2), v2 = getEdgeTarget(e2);
		edges[u1][v1] = e2;
		edges[u2][v2] = e1;
		super.edgeSwap(e1, e2);
	}

	// TODO default in graph
	// TODO implement eit.remove
	// TODO add tests
	@Override
	public void removeEdgesOut(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	public void removeEdgesIn(int v) {
		for (EdgeIter eit = edgesIn(v); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	public void reverseEdge(int e) {
		int u = getEdgeSource(e), v = getEdgeTarget(e);
		if (edges[v][u] != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		edges[v][u] = e;
		edges[u][v] = EdgeNone;
		super.reverseEdge(e);
	}

}
