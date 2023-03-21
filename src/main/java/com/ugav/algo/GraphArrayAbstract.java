package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;

abstract class GraphArrayAbstract extends GraphAbstract {

	private int[] edgeEndpoints;

	private static final int SizeofEdgeEndpoints = 2;
	static final int[][] EDGES_EMPTY = new int[0][];
	static final int[] EDGES_LIST_EMPTY = new int[0];
	static final int[] EDGES_LEN_EMPTY = EDGES_LIST_EMPTY;
	private static final int[] EdgeEndpointsEmpty = EDGES_LIST_EMPTY;

	public GraphArrayAbstract(int n) {
		super(n);
		edgeEndpoints = n > 0 ? new int[n * SizeofEdgeEndpoints] : EdgeEndpointsEmpty;
	}

	@Override
	public int addEdge(int u, int v) {
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
