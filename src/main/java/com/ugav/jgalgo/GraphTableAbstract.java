package com.ugav.jgalgo;

import java.util.Arrays;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphTableAbstract extends GraphAbstract {

	final int[][] edges;
	private final Weights.Long edgeEndpoints;
	private final IntSet verticesSet;

	private static final int[][] EDGES_EMPTY = new int[0][];
	static final int EdgeNone = -1;

	GraphTableAbstract(int n, IDStrategy edgesIDStrategy) {
		super(null, edgesIDStrategy);
		edges = n > 0 ? new int[n][n] : EDGES_EMPTY;
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);

		EdgesWeights.Builder wBuilder = new EdgesWeights.Builder(this, null);
		edgeEndpoints = wBuilder.ofLongs(sourceTarget2Endpoints(-1, -1));
		addInternalEdgesWeight(edgeEndpoints);

		verticesSet = new AbstractIntSet() {

			@Override
			public int size() {
				return edges.length;
			}

			@Override
			public boolean contains(int key) {
				return key >= 0 && key < size();
			}

			@Override
			public IntIterator iterator() {
				return new IntIterator() {
					int u = 0;

					@Override
					public boolean hasNext() {
						return u < size();
					}

					@Override
					public int nextInt() {
						if (!hasNext())
							throw new NoSuchElementException();
						return u++;
					}
				};
			}
		};
	}

	@Override
	public IntSet vertices() {
		return verticesSet;
	}

	@Override
	public IntSet edges() {
		return ((WeightsAbstract<?>) edgeEndpoints).keysSet();
	}

	@Override
	public int addVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public EdgeIter edges(int u) {
		return new EdgeIterOut(u);
	}

	@Override
	public int getEdge(int u, int v) {
		return edges[u][v];
	}

	@Override
	public int addEdge(int u, int v) {
		if (edges[u][v] != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(u, v);
		edgeEndpoints.set(e, sourceTarget2Endpoints(u, v));
		return e;
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

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEdges() {
		int n = vertices().size();
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);
		super.clearEdges();
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

	class EdgeIterOut implements EdgeIter {

		private final int u;
		private int v;
		private int lastV = -1;

		EdgeIterOut(int u) {
			if (!(0 <= u && u < vertices().size()))
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
			int n = vertices().size();
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
			if (!(0 <= v && v < vertices().size()))
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
			int n = vertices().size();
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
