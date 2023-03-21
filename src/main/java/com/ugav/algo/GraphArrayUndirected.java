package com.ugav.algo;

import java.util.Arrays;

public class GraphArrayUndirected extends GraphArrayAbstract implements Graph.Undirected {

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
	public int newVertex() {
		int v = super.newVertex();
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

		for (int w : new int[] { u, v }) {
			if (edges[w].length <= edgesNum[w])
				edges[w] = Arrays.copyOf(edges[w], Math.max(edges[w].length * 2, 2));
			edges[w][edgesNum[w]++] = e;
		}

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
		for (int w : new int[] { u, v }) {
			for (int i = 0; i < edgesNum[w]; i++) {
				if (edges[w][i] == e) {
					edges[w][i] = edges[w][--edgesNum[w]];
					break;
				}
			}
		}
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int[] es = new int[] { e1, e2 };
		for (int eIdx = 0; eIdx < 2; eIdx++) {
			int e = es[eIdx], eSwap = es[(eIdx + 1) % 2];

			for (int w : new int[] { getEdgeSource(e), getEdgeTarget(e) }) {
				for (int i = 0; i < edgesNum[w]; i++) {
					if (edges[w][i] == e) {
						edges[w][i] = eSwap;
						break;
					}
				}
			}
		}

		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdges(int u) {
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
		int n = vertices();
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
			int u0 = getEdgeSource(lastEdge);
			int v0 = getEdgeTarget(lastEdge);
			return u == u0 ? v0 : u0;
		}

	}

}
