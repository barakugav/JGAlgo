package com.jgalgo;

import java.util.Arrays;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntBigArrays;

abstract class GraphTableAbstract extends GraphBaseContinues {

	final int[][] edges;
	private final DataContainer.Long edgeEndpoints;

	static final int EdgeNone = -1;

	GraphTableAbstract(int n, GraphCapabilities capabilities) {
		super(n, capabilities);
		edges = n > 0 ? new int[n][n] : IntBigArrays.EMPTY_BIG_ARRAY;
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);

		edgeEndpoints = new DataContainer.Long(n, sourceTarget2Endpoints(-1, -1));
		addInternalEdgesDataContainer(edgeEndpoints);
	}

	/**
	 * Vertex addition is not support. The number of vertices is fixed and should be specified in the constructor.
	 */
	@Deprecated
	@Override
	public final int addVertex() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Vertex removal is not support. The number of vertices is fixed and should be specified in the constructor.
	 */
	@Deprecated
	@Override
	public final void removeVertex(int v) {
		throw new UnsupportedOperationException();
	}

	@Override
	final void vertexSwap(int v1, int v2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EdgeIter edgesOut(int u) {
		return new EdgeIterOut(u);
	}

	@Override
	public int getEdge(int u, int v) {
		return edges[u][v];
	}

	@Override
	public EdgeIter getEdges(int u, int v) {
		int e = edges[u][v];
		if (e == EdgeNone) {
			return GraphsUtils.EmptyEdgeIter;
		} else {
			return new EdgeIter() {

				boolean first = true;

				@Override
				public boolean hasNext() {
					return first;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					first = false;
					return e;
				}

				@Override
				public int u() {
					return u;
				}

				@Override
				public int v() {
					return v;
				}
			};
		}
	}

	@Override
	public int addEdge(int u, int v) {
		if (edges[u][v] != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(u, v);
		edgeEndpoints.add(e);
		edgeEndpoints.set(e, sourceTarget2Endpoints(u, v));
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		edgeEndpoints.remove(e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		edgeEndpoints.swap(e1, e2);
		super.edgeSwap(e1, e2);
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

	/**
	 * Clearing is not supported, as vertices removal are not supported. The number of vertices is fixed and should be
	 * specified in the constructor.
	 */
	@Deprecated
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEdges() {
		int n = vertices().size();
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);
		edgeEndpoints.clear();
		super.clearEdges();
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

	class EdgeIterOut implements EdgeIter {

		private final int u;
		private int v;
		private int lastV = -1;

		EdgeIterOut(int u) {
			if (!(0 <= u && u < vertices().size()))
				throw new IllegalArgumentException("Illegal vertex: " + u);
			this.u = u;

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return v >= 0;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			int e = edges[u][lastV = v];
			advanceUntilNext(v + 1);
			return e;
		}

		void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
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

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return u != -1;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			int e = edges[lastU = u][v];
			advanceUntilNext(u + 1);
			return e;
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
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
