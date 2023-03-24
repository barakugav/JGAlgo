package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphArrayAbstract extends GraphAbstract {

	private final Weights.Long edgeEndpoints;

	static final int[] EmptyIntArr = new int[0];

	public GraphArrayAbstract(IDStrategy verticesIDStrategy, IDStrategy edgesIDStrategy) {
		super(verticesIDStrategy, edgesIDStrategy);
		EdgesWeights.Builder wBuilder = new EdgesWeights.Builder(this, null);
		edgeEndpoints = wBuilder.ofLongs(sourceTarget2Endpoints(-1, -1));
		addInternalEdgesWeight(edgeEndpoints);
	}

	@Override
	public IntSet edges() {
		return ((WeightsAbstract<?>) edgeEndpoints).keysSet();
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edgeEndpoints.set(e, sourceTarget2Endpoints(u, v));
		return e;
	}

	static void addEdgeToList(Weights<int[]> edges, Weights.Int edgesNum, int w, int e) {
		int[] es = edges.get(w);
		int num = edgesNum.getInt(w);
		if (es.length <= num) {
			es = Arrays.copyOf(es, Math.max(es.length * 2, 2));
			edges.set(w, es);
		}
		es[num] = e;
		edgesNum.set(w, num + 1);
	}

	static int edgeIndexOf(Weights<int[]> edges0, Weights.Int edgesNum, int w, int e) {
		int[] edges = edges0.get(w);
		int num = edgesNum.getInt(w);
		for (int i = 0; i < num; i++)
			if (edges[i] == e)
				return i;
		return -1;
	}

	static void removeEdgeFromList(Weights<int[]> edges, Weights.Int edgesNum, int w, int e) {
		int i = edgeIndexOf(edges, edgesNum, w, e);
		int[] es = edges.get(w);
		int num = edgesNum.getInt(w);
		es[i] = es[num - 1];
		edgesNum.set(w, num - 1);
	}

	void reverseEdge(int edge) {
		checkEdgeIdx(edge);
		long endpoints = edgeEndpoints.getLong(edge);
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
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
		return (((long) u) << 32) + v;
	}

	private static int endpoints2Source(long endpoints) {
		return (int) ((endpoints >> 32) & 0xffffffff);
	}

	private static int endpoints2Target(long endpoints) {
		return (int) ((endpoints >> 0) & 0xffffffff);
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
