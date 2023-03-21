package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;

abstract class GraphTableAbstract extends GraphAbstract {

	final int[][] edges;
	private int[] edgeEndpoints;

	private static final int SizeofEdgeEndpoints = 2;
	private static final int[][] EDGES_EMPTY = new int[0][];
	static final int EdgeNone = -1;
	private static final int[] EdgeEndpointsEmpty = new int[0];

	GraphTableAbstract(int n) {
		super(n);
		edges = n > 0 ? new int[n][n] : EDGES_EMPTY;
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);
		edgeEndpoints = EdgeEndpointsEmpty;
	}

	@Override
	public int newVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int addEdge(int u, int v) {
		if (edges[u][v] != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(u, v);
		if (e >= edgeEndpoints.length / SizeofEdgeEndpoints)
			edgeEndpoints = Arrays.copyOf(edgeEndpoints, Math.max(edgeEndpoints.length * 2, 2));
		edgeEndpoints[edgeSourceIdx(e)] = u;
		edgeEndpoints[edgeTargetIdx(e)] = v;
		return e;
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = getEdgeSource(e1), v1 = getEdgeTarget(e1);
		int u2 = getEdgeSource(e2), v2 = getEdgeTarget(e2);
		edgeEndpoints[edgeSourceIdx(e1)] = u2;
		edgeEndpoints[edgeTargetIdx(e1)] = v2;
		edgeEndpoints[edgeSourceIdx(e2)] = u1;
		edgeEndpoints[edgeTargetIdx(e2)] = v1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public EdgeIter edges(int u) {
		return new EdgesOutItrVertex(u);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEdges() {
		int n = vertices();
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);
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

	class EdgesOutItrVertex implements EdgeIter {

		private final int u;
		private int v;
		private int lastE = EdgeNone;
		private int lastV = -1;

		EdgesOutItrVertex(int u) {
			if (!(0 <= u && u < vertices()))
				throw new IllegalArgumentException("Illegal vertex: " + u);
			this.u = u;

			v = 0;
			advanceUntilNext();
		}

		@Override
		public boolean hasNext() {
			return v >= 0;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			lastE = edges[u][lastV = v];
			v++;
			advanceUntilNext();
			return lastE;
		}

		void advanceUntilNext() {
			int n = vertices();
			for (int next = v; next < n; next++) {
				if (edges[u][next] != EdgeNone) {
					v = next;
					return;
				}
			}
			v = -1;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			return lastV;
		}
	}

	class EdgesInItrVertex implements EdgeIter {

		private int u;
		private final int v;
		private int lastE = EdgeNone;
		private int lastU = -1;

		EdgesInItrVertex(int v) {
			if (!(0 <= v && v < vertices()))
				throw new IllegalArgumentException("Illegal vertex: " + v);
			this.v = v;

			u = 0;
			advanceUntilNext();
		}

		@Override
		public boolean hasNext() {
			return u != -1;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			lastE = edges[lastU = u][v];
			u++;
			advanceUntilNext();
			return lastE;
		}

		private void advanceUntilNext() {
			int n = vertices();
			for (int next = u; next < n; next++) {
				if (edges[next][v] != EdgeNone) {
					u = next;
					return;
				}
			}
			u = -1;
		}

		@Override
		public int u() {
			return lastU;
		}

		@Override
		public int v() {
			return v;
		}
	}

}
