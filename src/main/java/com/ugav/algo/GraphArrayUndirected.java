package com.ugav.algo;

import java.util.Arrays;

public class GraphArrayUndirected extends GraphArrayAbstract implements UGraph {

	private int[][] edges;
	private int[] edgesNum;

	private static final int[][] EDGES_EMPTY = new int[0][];
	private static final int[] EDGES_LIST_EMPTY = new int[0];
	private static final int[] EDGES_LEN_EMPTY = EDGES_LIST_EMPTY;

	public GraphArrayUndirected() {
		this(0);
	}

	public GraphArrayUndirected(int n) {
		super(n);
		edges = n == 0 ? EDGES_EMPTY : new int[n][];
		Arrays.fill(edges, EDGES_LIST_EMPTY);
		edgesNum = n == 0 ? EDGES_LEN_EMPTY : new int[n];
	}

	@Override
	public EdgeIter edges(int u) {
		checkVertexIdx(u);
		return new EdgeIt(u, edges[u], edgesNum[u]);
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		if (v >= edges.length) {
			int aLen = Math.max(edges.length * 2, 2);
			edges = Arrays.copyOf(edges, aLen);
			edgesNum = Arrays.copyOf(edgesNum, aLen);
		}
		edges[v] = edges[v] = EDGES_LIST_EMPTY;
		return v;
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		addEdgeToList(edges, edgesNum, u, e);
		addEdgeToList(edges, edgesNum, v, e);
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
		removeEdgeFromList(edges, edgesNum, u, e);
		removeEdgeFromList(edges, edgesNum, v, e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int i1 = edgeIndexOf(edges, edgesNum, u1, e1);
		int j1 = edgeIndexOf(edges, edgesNum, v1, e1);
		int i2 = edgeIndexOf(edges, edgesNum, u2, e2);
		int j2 = edgeIndexOf(edges, edgesNum, v2, e2);
		edges[u1][i1] = e2;
		edges[v1][j1] = e2;
		edges[u2][i2] = e1;
		edges[v2][j2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesAll(int u) {
		checkVertexIdx(u);
		while (edgesNum[u] > 0)
			removeEdge(edges[u][0]);
	}

	@Override
	public int degree(int u) {
		checkVertexIdx(u);
		return edgesNum[u];
	}

	@Override
	public void clearEdges() {
		int n = verticesNum();
		Arrays.fill(edgesNum, 0, n, 0);
		super.clearEdges();
	}

	private class EdgeIt extends GraphArrayAbstract.EdgeIt {

		private final int u;

		EdgeIt(int u, int[] edges, int count) {
			super(edges, count);
			this.u = u;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			int u0 = edgeSource(lastEdge);
			int v0 = edgeTarget(lastEdge);
			return u == u0 ? v0 : u0;
		}

	}

}
