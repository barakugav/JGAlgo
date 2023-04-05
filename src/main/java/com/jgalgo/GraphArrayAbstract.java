package com.jgalgo;

import java.util.Arrays;
import java.util.NoSuchElementException;

abstract class GraphArrayAbstract extends GraphBaseContinues {

	private final DataContainer.Long edgeEndpoints;

	static final int[] EmptyIntArr = new int[0];

	public GraphArrayAbstract(int n, GraphCapabilities capabilities) {
		super(n, capabilities);
		edgeEndpoints = new DataContainer.Long(0, sourceTarget2Endpoints(-1, -1));
		addInternalEdgesDataContainer(edgeEndpoints);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edgeEndpoints.set(e, sourceTarget2Endpoints(u, v));
		return e;
	}

	static void addEdgeToList(DataContainer.Obj<int[]> edges, DataContainer.Int edgesNum, int w, int e) {
		int[] es = edges.get(w);
		int num = edgesNum.getInt(w);
		if (es.length <= num) {
			es = Arrays.copyOf(es, Math.max(es.length * 2, 2));
			edges.set(w, es);
		}
		es[num] = e;
		edgesNum.set(w, num + 1);
	}

	static int edgeIndexOf(int[] edges, int edgesNum, int e) {
		for (int i = 0; i < edgesNum; i++)
			if (edges[i] == e)
				return i;
		return -1;
	}

	static void removeEdgeFromList(DataContainer.Obj<int[]> edges, DataContainer.Int edgesNum, int w, int e) {
		int[] es = edges.get(w);
		int num = edgesNum.getInt(w);
		int i = edgeIndexOf(es, num, e);
		es[i] = es[num - 1];
		edgesNum.set(w, num - 1);
	}

	void reverseEdge(int edge) {
		long endpoints = edgeEndpoints.getLong(edge);
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		if (u == v)
			return;
		endpoints = sourceTarget2Endpoints(v, u);
		edgeEndpoints.set(edge, endpoints);
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		long endpoints = edgeEndpoints.getLong(edge);
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public int edgeSource(int edge) {
		checkEdgeIdx(edge);
		return endpoints2Source(edgeEndpoints.getLong(edge));
	}

	@Override
	public int edgeTarget(int edge) {
		checkEdgeIdx(edge);
		return endpoints2Target(edgeEndpoints.getLong(edge));
	}

	private static long sourceTarget2Endpoints(int u, int v) {
		return ((u & 0xffffffffL) << 32) | ((v & 0xffffffffL) << 0);
	}

	private static int endpoints2Source(long endpoints) {
		return (int) ((endpoints >> 32) & 0xffffffffL);
	}

	private static int endpoints2Target(long endpoints) {
		return (int) ((endpoints >> 0) & 0xffffffffL);
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
