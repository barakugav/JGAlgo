package com.ugav.algo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntIterator;

public class GraphArrayUndirected<E> extends GraphArrayAbstract<E> implements Graph.Undirected<E> {

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
	public EdgeIter<E> edges(int u) {
		checkVertexIdx(u);
		return new EdgeIt(u, edges[u], edgesNum[u]);
	}

	@Override
	public int getEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);
		for (IntIterator it = edges(u); it.hasNext();) {
			int e = it.nextInt();
			if (getEdgeTarget(e) == v)
				return e;
		}
		return -1;
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

	private class EdgeIt extends GraphArrayAbstract<E>.EdgeIt {

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
