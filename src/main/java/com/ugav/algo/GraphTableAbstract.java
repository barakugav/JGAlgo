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
	public int addVertex() {
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
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edgeEndpoints[edgeSourceIdx(e1)] = u2;
		edgeEndpoints[edgeTargetIdx(e1)] = v2;
		edgeEndpoints[edgeSourceIdx(e2)] = u1;
		edgeEndpoints[edgeTargetIdx(e2)] = v1;
		super.edgeSwap(e1, e2);
	}

	void reverseEdge(int e) {
		int u = edgeSource(e), v = edgeTarget(e);
		edgeEndpoints[edgeSourceIdx(e)] = v;
		edgeEndpoints[edgeTargetIdx(e)] = u;
	}

	@Override
	public int getEdge(int u, int v) {
		return edges[u][v];
	}

	@Override
	public EdgeIter edges(int u) {
		return new EdgeIterOut(u);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEdges() {
		int n = verticesNum();
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);
		super.clearEdges();
	}

	@Override
	public int edgeSource(int edge) {
		checkEdgeIdx(edge);
		return edgeEndpoints[edgeSourceIdx(edge)];
	}

	@Override
	public int edgeTarget(int edge) {
		checkEdgeIdx(edge);
		return edgeEndpoints[edgeTargetIdx(edge)];
	}

	private static int edgeSourceIdx(int e) {
		return edgeEndpointIdx(e, 0);
	}

	private static int edgeTargetIdx(int e) {
		return edgeEndpointIdx(e, 1);
	}

	private static int edgeEndpointIdx(int e, int offset) {
		return e * SizeofEdgeEndpoints + offset;
	}

	class EdgeIterOut implements EdgeIter {

		private final int u;
		private int v;
		private int lastV = -1;

		EdgeIterOut(int u) {
			if (!(0 <= u && u < verticesNum()))
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
			int e = edges[u][lastV = v++];
			advanceUntilNext();
			return e;
		}

		void advanceUntilNext() {
			int n = verticesNum();
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

		@Override
		public void remove() {
			removeEdge(edges[u()][v()]);
		}
	}

	class EdgeIterIn implements EdgeIter {

		private int u;
		private final int v;
		private int lastU = -1;

		EdgeIterIn(int v) {
			if (!(0 <= v && v < verticesNum()))
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
			int e = edges[lastU = u++][v];
			advanceUntilNext();
			return e;
		}

		private void advanceUntilNext() {
			int n = verticesNum();
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

		@Override
		public void remove() {
			removeEdge(edges[u()][v()]);
		}
	}

}
