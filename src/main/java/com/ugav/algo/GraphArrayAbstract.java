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

	static void addEdgeToList(int[][] edges, int[] edgesNum, int w, int e) {
		if (edges[w].length <= edgesNum[w])
			edges[w] = Arrays.copyOf(edges[w], Math.max(edges[w].length * 2, 2));
		edges[w][edgesNum[w]++] = e;
	}

	static int edgeIndexOf(int[][] edges0, int[] edgesNum, int w, int e) {
		int[] edges = edges0[w];
		int num = edgesNum[w];
		for (int i = 0; i < num; i++)
			if (edges[i] == e)
				return i;
		return -1;
	}

	static void removeEdgeFromList(int[][] edges, int[] edgesNum, int w, int e) {
		int i = edgeIndexOf(edges, edgesNum, w, e);
		edges[w][i] = edges[w][--edgesNum[w]];
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
