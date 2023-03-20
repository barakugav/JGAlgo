package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;

abstract class GraphArrayAbstract extends GraphAbstract {

	private int n, m;
	private int[] edgeEndpoints;

	private static final int SizeofEdgeEndpoints = 2;
	static final int[][] EDGES_EMPTY = new int[0][];
	static final int[] EDGES_LIST_EMPTY = new int[0];
	static final int[] EDGES_LEN_EMPTY = EDGES_LIST_EMPTY;
	private static final int[] EdgeEndpointsEmpty = EDGES_LIST_EMPTY;

	public GraphArrayAbstract(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
		edgeEndpoints = n > 0 ? new int[m * SizeofEdgeEndpoints] : EdgeEndpointsEmpty;
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public int edges() {
		return m;
	}

	@Override
	public int newVertex() {
		return n++;
	}

	@Override
	public int addEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);
		int e = m++;
		if (e >= edgeEndpoints.length / SizeofEdgeEndpoints)
			edgeEndpoints = Arrays.copyOf(edgeEndpoints, Math.max(edgeEndpoints.length * 2, 2));
		edgeEndpoints[edgeSourceIdx(e)] = u;
		edgeEndpoints[edgeTargetIdx(e)] = v;
		return e;
	}

	@Override
	public void clear() {
		clearEdges();
		n = 0;
		super.clear();
	}

	@Override
	public void clearEdges() {
		m = 0;
		super.clearEdges();
	}

	@Override
	public int getEdgeSource(int edge) {
		checkEdgeIdx(edge);
		return edgeEndpoints[edgeSourceIdx(edge)];
	}

	@Override
	public int getEdgeTarget(int edge) {
		checkEdgeIdx(edge);
		return edgeEndpoints[edgeTargetIdx(edge)];
	}

	private static int edgeSourceIdx(int e) {
		return edgeEndpoint(e, 0);
	}

	private static int edgeTargetIdx(int e) {
		return edgeEndpoint(e, 1);
	}

	private static int edgeEndpoint(int e, int offset) {
		return e * SizeofEdgeEndpoints + offset;
	}

	void checkVertexIdx(int u) {
		if (u >= n)
			throw new IndexOutOfBoundsException(u);
	}

	void checkEdgeIdx(int e) {
		if (e >= m)
			throw new IndexOutOfBoundsException(e);
	}

	abstract class EdgeIt implements EdgeIter {

		private final int[] edges;
		private final int count;
		private int idx;
		int lastEdge = -1;

		EdgeIt(int[] edges, int count) {
			this.edges = edges;
			this.count = count;
		}

		@Override
		public boolean hasNext() {
			return idx < count;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			return lastEdge = edges[idx++];
		}

	}

}
