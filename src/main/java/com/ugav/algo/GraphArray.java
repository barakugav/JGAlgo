package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GraphArray<E> extends GraphAbstract<E> {

	private int n;
	private int m;
	private Edge<E>[][] edges;
	private int[] edgesLen;
	private final boolean directed;
	private final Collection<Edge<E>> edgesView;

	@SuppressWarnings("rawtypes")
	private static final Edge[][] EDGES_EMPTY = new Edge[0][];
	@SuppressWarnings("rawtypes")
	private static final Edge[] EDGES_LIST_EMPTY = new Edge[0];
	private static final int[] EDGES_LEN_EMPTY = new int[0];

	public GraphArray(DirectedType directed) {
		this(directed, 0);
	}

	@SuppressWarnings("unchecked")
	public GraphArray(DirectedType directed, int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
		edges = n == 0 ? EDGES_EMPTY : new Edge[n][];
		Arrays.fill(edges, EDGES_LIST_EMPTY);
		edgesLen = n == 0 ? EDGES_LEN_EMPTY : new int[n];
		this.directed = directed == DirectedType.Directed ? true : false;
		edgesView = new EdgesView();
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public Collection<Edge<E>> edges() {
		return edgesView;
	}

	@Override
	public Iterator<Edge<E>> edges(int u) {
		return new EdgeVertexItr<>(edges[u], edgesLen[u]);
	}

	@Override
	public Edge<E> getEdge(int u, int v) {
		if (u >= n)
			throw new IndexOutOfBoundsException();
		int len = edgesLen[u];
		Edge<E>[] es = edges[u];
		for (int i = 0; i < len; i++)
			if (es[i].v() == v)
				return es[i];
		return null;
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

	@SuppressWarnings("unchecked")
	@Override
	public int newVertex() {
		int v = n++;
		if (n > edges.length) {
			int aLen = Math.max(edges.length * 2, 2);
			edges = Arrays.copyOf(edges, aLen);
			edgesLen = Arrays.copyOf(edgesLen, aLen);
		}
		edges[v] = EDGES_LIST_EMPTY;
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
	public void addEdge(Edge<E> e) {
		Edge<E> twin = e.twin();
		if (isDirected() && twin != null)
			throw new IllegalArgumentException("twin edges are only supported in undirected graphs");

		addEdge0(e);
		if (twin != null)
			addEdge0(twin);
		m++;
	}

	private void addEdge0(Edge<E> e) {
		int u = e.u(), len = edgesLen[u];
		Edge<E>[] es = edges[u];

		if (es.length <= len)
			edges[u] = es = Arrays.copyOf(es, Math.max(len * 2, 2));
		es[edgesLen[u]++] = e;
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
				es[i] = es[len - 1];
				es[len - 1] = null;
			} else
				es[i] = null;
			return;
		}
		throw new IllegalArgumentException("edge not in graph: " + e);
	}

	@Override
	public void clear() {
		edges().clear();
		n = 0;
	}

	public static <E> GraphArray<E> valueOf(int n, Collection<Edge<E>> edges, DirectedType directed) {
		GraphArray<E> g = new GraphArray<>(directed, n);
		for (Edge<E> e : edges)
			g.addEdge(e);
		return g;
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
			E val = val();
			return "(" + u + ", " + v + ")" + (val != null ? "[" + val() + "]" : "");
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
			if (u >= n)
				return false;
			int len = edgesLen[u];
			Edge<E>[] es = edges[u];
			for (int i = 0; i < len; i++)
				if (es[i] == e)
					return true;
			return false;
		}

		@Override
		public Iterator<Edge<E>> iterator() {
			return new EdgesItr();
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
			Edge<E>[][] es = edges;
			int[] lens = edgesLen;
			for (int u = 0; u < n; u++)
				Arrays.fill(es[u], 0, lens[u], null);
			Arrays.fill(lens, 0, n, 0);
			m = 0;
		}

	}

}
