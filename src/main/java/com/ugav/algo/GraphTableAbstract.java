package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

abstract class GraphTableAbstract<E> extends GraphAbstract<E> {

	private final int n;
	private int m;
	final int[][] edges;
	private int[] edgeEndpoints;
	private EdgeData<E> edgeData;

	private static final int SizeofEdgeEndpoints = 2;
	private static final int[][] EDGES_EMPTY = new int[0][];
	private static final int EdgeNone = -1;
	private static final int[] EdgeEndpointsEmpty = new int[0];

	GraphTableAbstract(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		edges = n > 0 ? new int[n][n] : EDGES_EMPTY;
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);
		edgeEndpoints = n > 0 ? new int[m * SizeofEdgeEndpoints] : EdgeEndpointsEmpty;
		edgeData = new EdgeDataArray.Obj<>(n);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public int addEdge(int u, int v) {
		if (edges[u][v] != EdgeNone)
			throw new IllegalArgumentException();
		int e = m++;
		if (e >= edgeEndpoints.length / SizeofEdgeEndpoints)
			edgeEndpoints = Arrays.copyOf(edgeEndpoints, Math.max(edgeEndpoints.length * 2, 2));
		edgeEndpoints[edgeSourceIdx(e)] = u;
		edgeEndpoints[edgeTargetIdx(e)] = v;
//		edges[u][v] = e;
		return e;
	}

	@Override
	public EdgeIter<E> edges(int u) {
		return new EdgesOutItrVertex(u);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEdges() {
		edgeData.clear();
		for (int u = 0; u < n; u++)
			Arrays.fill(edges[u], EdgeNone);
		m = 0;
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

	@Override
	public EdgeData<E> edgeData() {
		return edgeData;
	}

	@Override
	public void setEdgesData(EdgeData<E> data) {
		edgeData = Objects.requireNonNull(data);
	}

	class EdgesOutItrVertex implements EdgeIter<E> {

		private final int u;
		private int v;
		private int lastE = EdgeNone;
		private int lastV = -1;

		EdgesOutItrVertex(int u) {
			if (!(0 <= u && u < n))
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
		public E data() {
			return edgeData().get(lastE);
		}

		@Override
		public void setData(E val) {
			edgeData().set(lastE, val);
		}

	}

	class EdgesInItrVertex implements EdgeIter<E> {

		private int u;
		private final int v;
		private int lastE = EdgeNone;
		private int lastU = -1;

		EdgesInItrVertex(int v) {
			if (!(0 <= v && v < n))
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
		public E data() {
			return edgeData().get(lastE);
		}

		@Override
		public void setData(E val) {
			edgeData().set(lastE, val);
		}

	}

}
