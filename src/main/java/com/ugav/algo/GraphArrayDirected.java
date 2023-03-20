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

		if (edgesOut[u].length <= edgesOutNum[u])
			edgesOut[u] = Arrays.copyOf(edgesOut[u], Math.max(edgesOut[u].length * 2, 2));
		edgesOut[u][edgesOutNum[u]++] = e;

		if (edgesIn[v].length <= edgesInNum[v])
			edgesIn[v] = Arrays.copyOf(edgesIn[v], Math.max(edgesIn[v].length * 2, 2));
		edgesIn[v][edgesInNum[v]++] = e;

		return e;
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
