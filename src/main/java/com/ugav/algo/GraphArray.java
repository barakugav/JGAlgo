package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GraphArray<E> implements Graph.Flexible<E> {

	private int n;
	private int m;
	private Edge<E>[][] edges;
	private int[] edgesLen;
	private final boolean directed;

	@SuppressWarnings("rawtypes")
	private static final Edge[][] EDGES_EMPTY = new Edge[0][];
	private static final int[] EDGES_LEN_EMPTY = new int[0];

	public GraphArray(boolean directed) {
		this(directed, 0);
	}

	@SuppressWarnings("unchecked")
	public GraphArray(boolean directed, int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
		edges = n == 0 ? EDGES_EMPTY : new Edge[n][];
		edgesLen = n == 0 ? EDGES_LEN_EMPTY : new int[n];
		this.directed = directed;
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public int edgesNum() {
		return m;
	}

	@Override
	public Iterator<Edge<E>> edges() {
		return new EdgesItr();
	}

	@Override
	public Iterator<Edge<E>> edges(int u) {
		return new EdgeVertexItr<>(edges[u], edgesLen[u]);
	}

	@Override
	public int getEdgesArr(int u, Edge<E>[] edges, int begin) {
		int len = edgesLen[u];
		if (len == 0)
			return 0;
		Edge<E>[] es = this.edges[u];
		System.arraycopy(es, 0, edges, begin, len);
		return len;
	}

	@Override
	public int getEdgesArrVs(int u, int[] edges, int begin) {
		Edge<E>[] es = this.edges[u];
		int len = edgesLen[u];
		for (int i = 0; i < len; i++)
			edges[begin + i] = es[i].v();
		return len;
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	@Override
	public int newVertex() {
		int v = n++;
		if (n > edges.length) {
			int aLen = Math.max(edges.length * 2, 2);
			edges = Arrays.copyOf(edges, aLen);
			edgesLen = Arrays.copyOf(edgesLen, aLen);
		}
		return v;
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		if (directed) {
			Edge<E> e = new EdgeDirectedImpl<>(u, v);
			addEdge(e);
			return e;
		} else {
			EdgeUndirectedImpl<E> e = new EdgeUndirectedImpl<>(u, v);
			if (u != v) {
				EdgeUndirectedImpl<E> eTwin = new EdgeUndirectedImpl<>(v, u);
				e.twin = eTwin;
				eTwin.twin = e;
			}
			addEdge(e);
			return e;
		}
	}

	@Override
	public void removeEdge(Edge<E> e) {
		Edge<E> twin = e.twin();
		removeEdge0(e);
		if (twin != null)
			removeEdge0(twin);
		m--;
	}

	private void removeEdge0(Edge<E> e) {
		int u = e.u();
		Edge<E>[] es = edges[u];
		for (int i = 0, len = edgesLen[u]; i < len; i++) {
			if (es[i] != e)
				continue;
			if (--edgesLen[u] > 0) {
				es[i] = es[len - 2];
				es[len - 2] = null;
			}
			es[i] = null;
			return;
		}
		throw new IllegalArgumentException("edge not in graph: " + e);
	}

	@Override
	public void addEdge(Edge<E> e) {
		Edge<E> twin = e.twin();
		if (isDirected() && twin != null)
			throw new IllegalArgumentException("twin edges are only supported in undirected graphs");

		addEdge0(e);
		if (twin != null)
			addEdge0(twin);
		m++;
	}

	@SuppressWarnings("unchecked")
	private void addEdge0(Edge<E> e) {
		int u = e.u(), len = edgesLen[u];
		Edge<E>[] es = edges[u];

		if (es == null)
			edges[u] = es = new Edge[1];
		else if (es.length == len)
			edges[u] = es = Arrays.copyOf(es, len * 2);
		es[edgesLen[u]++] = e;
	}

	@Override
	public void clear() {
		Edge<E>[] es;
		for (int i = 0; i < edges.length; i++)
			if ((es = edges[i]) != null)
				Arrays.fill(es, null);
		Arrays.fill(edgesLen, 0);
		n = 0;
		m = 0;
	}

	private static <E> boolean isApiEdge(Edge<E> e) {
		Edge<E> twin = e.twin();
		return twin == null || System.identityHashCode(e) < System.identityHashCode(twin);
	}

	private static class EdgeVertexItr<E> implements Iterator<Edge<E>> {

		private final Edge<E>[] edges;
		private int idx;

		EdgeVertexItr(Edge<E>[] edges, int len) {
			this.edges = edges;
			idx = len;
		}

		@Override
		public boolean hasNext() {
			return idx > 0;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return edges[--idx];
		}

	}

	private class EdgesItr implements Iterator<Edge<E>> {

		private int u;
		private int idx;

		EdgesItr() {
			u = 0;
			idx = 0;
		}

		public boolean hasNext() {
			for (; u < n; u++) {
				for (; idx < edgesLen[u]; idx++)
					if (isDirected() || isApiEdge(edges[u][idx]))
						return true;
				idx = 0;
			}
			return false;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return edges[u][idx++];
		}

	}

	private abstract static class EdgeImpl<E> implements Graph.Edge<E> {

		final int u;
		final int v;
		E value;

		private EdgeImpl(int u, int v) {
			this.u = u;
			this.v = v;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			return v;
		}

		@Override
		public E val() {
			return value;
		}

		@Override
		public void val(E v) {
			value = v;
		}

		@Override
		public String toString() {
			return "(" + u + ", " + v + ")[" + val() + "]";
		}

	}

	private static class EdgeUndirectedImpl<E> extends EdgeImpl<E> {

		EdgeUndirectedImpl<E> twin;

		private EdgeUndirectedImpl(int u, int v) {
			super(u, v);
		}

		EdgeUndirectedImpl<E> getApiEdge() {
			return isApiEdge(this) ? this : twin;
		}

		@Override
		public E val() {
			return getApiEdge().value;
		}

		@Override
		public void val(E v) {
			getApiEdge().value = v;
		}

		@Override
		public Edge<E> twin() {
			return twin;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Edge))
				return false;

			Edge<?> e = (Edge<?>) o;
			return ((u == e.u() && v == e.v()) || (u == e.v() && v == e.u())) && Objects.equals(val(), e.val());
		}

		@Override
		public int hashCode() {
			return u ^ v ^ Objects.hashCode(val());
		}

	}

	private static class EdgeDirectedImpl<E> extends EdgeImpl<E> {

		private EdgeDirectedImpl(int u, int v) {
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
			return u == e.u() && v == e.v() && Objects.equals(val(), e.val());
		}

		@Override
		public int hashCode() {
			return u ^ ~v ^ Objects.hashCode(val());
		}

	}

}
