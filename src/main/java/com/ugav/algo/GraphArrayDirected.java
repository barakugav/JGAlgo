package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class GraphArrayDirected<E> extends GraphArrayAbstract<E> implements GraphDirected<E> {

	private Edge<E>[][] edgesOut;
	private Edge<E>[][] edgesIn;
	private int[] edgesOutLen;
	private int[] edgesInLen;
	private final Collection<Edge<E>> edgesView;

	@SuppressWarnings("unchecked")
	public GraphArrayDirected(int n) {
		super(n);
		edgesOut = n == 0 ? EDGES_EMPTY : new Edge[n][];
		edgesIn = n == 0 ? EDGES_EMPTY : new Edge[n][];
		Arrays.fill(edgesIn, EDGES_LIST_EMPTY);
		Arrays.fill(edgesOut, EDGES_LIST_EMPTY);
		edgesOutLen = n == 0 ? EDGES_LEN_EMPTY : new int[n];
		edgesInLen = n == 0 ? EDGES_LEN_EMPTY : new int[n];
		edgesView = new EdgesView();
	}

	@Override
	public Collection<Edge<E>> edges() {
		return edgesView;
	}

	@Override
	public EdgeIterator<E> edgesOut(int u) {
		return new VertexEdgeOutItr(u);
	}

	@Override
	public EdgeIterator<E> edgesIn(int v) {
		return new VertexEdgeInItr(v);
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		Edge<E> e = new EdgeImpl<>(u, v);
		addEdge(e);
		return e;
	}

	@Override
	public Edge<E> getEdge(int u, int v) {
		if (u >= n)
			throw new IndexOutOfBoundsException();
		int len = edgesOutLen[u];
		Edge<E>[] es = edgesOut[u];
		for (int i = 0; i < len; i++)
			if (es[i].v() == v)
				return es[i];
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int newVertex() {
		int v = n++;
		if (n > edgesOut.length) {
			int aLen = Math.max(edgesOut.length * 2, 2);
			edgesOut = Arrays.copyOf(edgesOut, aLen);
			edgesIn = Arrays.copyOf(edgesIn, aLen);
			edgesOutLen = Arrays.copyOf(edgesOutLen, aLen);
			edgesInLen = Arrays.copyOf(edgesInLen, aLen);
		}
		edgesOut[v] = edgesIn[v] = EDGES_LIST_EMPTY;
		return v;
	}

	@Override
	public void addEdge(Edge<E> e) {
		if (e.u() >= vertices() || e.v() >= vertices())
			throw new IllegalArgumentException("Invalid edge: " + e);
		Edge<E> twin = e.twin();
		if (twin != null)
			throw new IllegalArgumentException("twin edges are only supported in undirected graphs");

		addEdgeToArr(edgesOut, edgesOutLen, e.u(), e);
		addEdgeToArr(edgesIn, edgesInLen, e.v(), e);
		m++;
	}

	private static <E> void addEdgeToArr(Edge<E>[][] edgesArr, int[] edgesLen, int u, Edge<E> e) {
		int len = edgesLen[u];
		Edge<E>[] es = edgesArr[u];
		if (es.length <= len)
			edgesArr[u] = es = Arrays.copyOf(es, Math.max(len * 2, 2));
		es[edgesLen[u]++] = e;
	}

	@Override
	public void removeEdge(Edge<E> e) {
		removeEdge0(e);
		m--;
	}

	private void removeEdge0(Edge<E> e) {
		Edge<E>[] es = edgesOut[e.u()];
		for (int i = 0, len = edgesOutLen[e.u()]; i < len; i++) {
			if (es[i] == e) {
				removeEdge0(edgesOut, edgesOutLen, e.u(), i);
				return;
			}
		}
		es = edgesIn[e.v()];
		for (int i = 0, len = edgesInLen[e.v()]; i < len; i++) {
			if (es[i] == e) {
				removeEdge0(edgesIn, edgesInLen, e.v(), i);
				return;
			}
		}
	}

	private static <E> void removeEdge0(Edge<E>[][] edgesArr, int[] edgesLen, int u, int edgeIdx) {
		Edge<E>[] es = edgesArr[u];
		int len = edgesLen[u];

		es[edgeIdx] = es[len - 1];
		es[len - 1] = null;
		edgesLen[u]--;
	}

	private abstract class EdgeOutItrBase extends GraphArrayAbstract<E>.EdgeItrBase {

		EdgeOutItrBase(int u) {
			super(edgesOut, u);
		}

		@Override
		void removeEdge(Edge<E> e, int edgeIdx) {
			removeEdge0(edgesOut, edgesOutLen, e.u(), edgeIdx);
		}

	}

	private class VertexEdgeOutItr extends EdgeOutItrBase {

		VertexEdgeOutItr(int u) {
			super(u);
		}

		@Override
		public boolean hasNext() {
			return idx < edgesOutLen[u];
		}

	}

	private class VertexEdgeInItr extends GraphArrayAbstract<E>.EdgeItrBase {

		VertexEdgeInItr(int v) {
			super(edgesIn, v);
		}

		@Override
		public boolean hasNext() {
			return idx < edgesOutLen[u];
		}

		@Override
		void removeEdge(Edge<E> e, int edgeIdx) {
			GraphArrayDirected.this.removeEdge(e); // TODO efficient
		}

	}

	private class GraphEdgesOutItr extends EdgeOutItrBase {

		GraphEdgesOutItr() {
			super(0);
		}

		@Override
		public boolean hasNext() {
			int n = GraphArrayDirected.this.n;
			int[] edgesLen = GraphArrayDirected.this.edgesOutLen;

			// don't support remove in case idx is moved
			toRemoveEdge = null;

			for (; u < n; u++) {
				if (idx < edgesLen[u])
					return true;
				idx = 0;
			}
			return false;
		}

	}

	private class EdgesView extends AbstractCollection<Edge<E>> {

		@Override
		public int size() {
			return m;
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Edge<?>))
				return false;
			Edge<?> e = (Edge<?>) o;
			int u = e.u();
			if (u >= n || e.v() >= n)
				return false;
			int len = edgesOutLen[u];
			Edge<E>[] es = edgesOut[u];
			for (int i = 0; i < len; i++)
				if (es[i] == e)
					return true;
			return false;
		}

		@Override
		public Iterator<Edge<E>> iterator() {
			return new GraphEdgesOutItr();
		}

		@Override
		public boolean add(Edge<E> e) {
			addEdge(e);
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o) {
			removeEdge((Edge<E>) o);
			return true;
		}

		@Override
		public void clear() {
			Edge<E>[][] esO = edgesOut, esI = edgesIn;
			int[] lensO = edgesOutLen, lensI = edgesInLen;
			for (int u = 0; u < n; u++) {
				Arrays.fill(esO[u], 0, lensO[u], null);
				Arrays.fill(esI[u], 0, lensI[u], null);
			}
			Arrays.fill(lensO, 0, n, 0);
			Arrays.fill(lensI, 0, n, 0);
			m = 0;
		}

	}

	private static class EdgeImpl<E> extends EdgeAbstract<E> {

		private EdgeImpl(int u, int v) {
			super(u, v);
		}

		@Override
		public Edge<E> twin() {
			return null;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Edge))
				return false;

			Edge<?> e = (Edge<?>) o;
			return u == e.u() && v == e.v() && Objects.equals(data(), e.data());
		}

		@Override
		public int hashCode() {
			return u ^ ~v ^ Objects.hashCode(data());
		}

	}

}
