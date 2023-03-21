package com.ugav.algo;

import java.util.Arrays;

public class GraphArrayDirected extends GraphArrayAbstract implements Graph.Directed {

	private int[][] edgesOut;
	private int[] edgesOutNum;
	private int[][] edgesIn;
	private int[] edgesInNum;

	public GraphArrayDirected() {
		this(0);
	}

	public GraphArrayDirected(int n) {
		super(n);
		edgesOut = n == 0 ? EDGES_EMPTY : new int[n][];
		edgesIn = n == 0 ? EDGES_EMPTY : new int[n][];
		Arrays.fill(edgesOut, EDGES_LIST_EMPTY);
		Arrays.fill(edgesIn, EDGES_LIST_EMPTY);
		edgesOutNum = n == 0 ? EDGES_LEN_EMPTY : new int[n];
		edgesInNum = n == 0 ? EDGES_LEN_EMPTY : new int[n];
	}

	@Override
	public EdgeIter edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeOutIt(u, edgesOut[u], edgesOutNum[u]);
	}

	@Override
	public EdgeIter edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeInIt(v, edgesIn[v], edgesInNum[v]);
	}

	@Override
	public int newVertex() {
		int v = super.newVertex();
		if (v >= edgesOut.length) {
			int aLen = Math.max(edgesOut.length * 2, 2);
			edgesOut = Arrays.copyOf(edgesOut, aLen);
			edgesIn = Arrays.copyOf(edgesIn, aLen);
			edgesOutNum = Arrays.copyOf(edgesOutNum, aLen);
			edgesInNum = Arrays.copyOf(edgesInNum, aLen);
		}
		edgesOut[v] = edgesIn[v] = EDGES_LIST_EMPTY;
		return v;
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		addEdgeToList(edgesOut, edgesOutNum, u, e);
		addEdgeToList(edgesIn, edgesInNum, v, e);
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
		removeEdgeFromList(edgesOut, edgesOutNum, u, e);
		removeEdgeFromList(edgesIn, edgesInNum, v, e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = getEdgeSource(e1), v1 = getEdgeTarget(e1);
		int u2 = getEdgeSource(e2), v2 = getEdgeTarget(e2);
		int i1 = edgeIndexOf(edgesOut, edgesOutNum, u1, e1);
		int j1 = edgeIndexOf(edgesIn, edgesInNum, v1, e1);
		int i2 = edgeIndexOf(edgesOut, edgesOutNum, u2, e2);
		int j2 = edgeIndexOf(edgesIn, edgesInNum, v2, e2);
		edgesOut[u1][i1] = e2;
		edgesIn[v1][j1] = e2;
		edgesOut[u2][i2] = e1;
		edgesIn[v2][j2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesOut(int u) {
		checkVertexIdx(u);
		while (edgesOutNum[u] > 0)
			removeEdge(edgesOut[u][0]);
	}

	@Override
	public void removeEdgesIn(int v) {
		checkVertexIdx(v);
		while (edgesInNum[v] > 0)
			removeEdge(edgesIn[v][0]);
	}

	@Override
	public void reverseEdge(int e) {
		int u = getEdgeSource(e), v = getEdgeTarget(e);
		removeEdgeFromList(edgesOut, edgesOutNum, u, e);
		removeEdgeFromList(edgesIn, edgesInNum, v, e);
		addEdgeToList(edgesOut, edgesOutNum, v, e);
		addEdgeToList(edgesIn, edgesInNum, u, e);
		super.reverseEdge(e);
	}

	@Override
	public int degreeOut(int u) {
		checkVertexIdx(u);
		return edgesOutNum[u];
	}

	@Override
	public int degreeIn(int v) {
		checkVertexIdx(v);
		return edgesInNum[v];
	}

	@Override
	public void clearEdges() {
		int n = vertices();
		Arrays.fill(edgesOutNum, 0, n, 0);
		Arrays.fill(edgesInNum, 0, n, 0);
		super.clearEdges();
	}

	private class EdgeOutIt extends EdgeIt {

		private final int u;

		EdgeOutIt(int u, int[] edges, int count) {
			super(edges, count);
			this.u = u;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			return getEdgeTarget(lastEdge);
		}

	}

	private class EdgeInIt extends EdgeIt {

		private final int v;

		EdgeInIt(int v, int[] edges, int count) {
			super(edges, count);
			this.v = v;
		}

		@Override
		public int u() {
			return getEdgeSource(lastEdge);
		}

		@Override
		public int v() {
			return v;
		}

	}

}
