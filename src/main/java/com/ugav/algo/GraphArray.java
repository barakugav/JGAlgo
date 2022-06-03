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
		return new VertexEdgeItr(u);
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
		if (e.u() >= vertices() || e.v() >= vertices())
			throw new IllegalArgumentException("Invalid edge: " + e);
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

	@Override
	public void removeEdgesOut(int u) {
		Edge<E>[] es = edges[u];
		int len = edgesLen[u];
		if (!directed) {
			for (int i = 0; i < len; i++) {
				Edge<E> twin = es[i].twin();
				if (twin != null)
					removeEdge0(twin);
			}
		}
		Arrays.fill(es, 0, len, null);
		m -= len;
		edgesLen[u] = 0;
	}

	private void removeEdge0(Edge<E> e) {
		int u = e.u();
		Edge<E>[] es = edges[u];
		for (int i = 0, len = edgesLen[u]; i < len; i++) {
			if (es[i] == e) {
				removeEdge0(u, i);
				return;
			}
		}
		throw new IllegalArgumentException("edge is not in graph: " + e);
	}

	private void removeEdge0(int u, int edgeIdx) {
		Edge<E>[] es = edges[u];
		int len = edgesLen[u];

		es[edgeIdx] = es[len - 1];
		es[len - 1] = null;
		edgesLen[u]--;
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
		if (twin == null)
			return true;
		if (e.u() != e.v())
			return e.u() < e.v();
		return System.identityHashCode(e) <= System.identityHashCode(twin);
	}

	private abstract class EdgeItrBase implements Iterator<Edge<E>> {

		int u;
		int idx;
		Edge<E> toRemoveEdge;
		int toRemoveIdx;

		EdgeItrBase(int u) {
			this.u = u;
			idx = 0;
			toRemoveEdge = null;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return toRemoveEdge = edges[u][toRemoveIdx = idx++];
		}

		@Override
		public void remove() {
			if (toRemoveEdge == null)
				throw new IllegalStateException();
			removeEdge0(u, toRemoveIdx);
			Edge<E> twin = toRemoveEdge.twin();
			if (twin != null)
				removeEdge0(twin);
			m--;
			toRemoveEdge = null;
			idx--;
		}

	}

	private class VertexEdgeItr extends EdgeItrBase {

		VertexEdgeItr(int u) {
			super(u);
		}

		@Override
		public boolean hasNext() {
			return idx < edgesLen[u];
		}

	}

	private class GraphEdgesItr extends EdgeItrBase {

		GraphEdgesItr() {
			super(0);
		}

		@Override
		public boolean hasNext() {
			int n = GraphArray.this.n;
			Edge<E>[][] edges = GraphArray.this.edges;
			int[] edgesLen = GraphArray.this.edgesLen;

			// don't support remove in case idx is moved
			toRemoveEdge = null;

			for (; u < n; u++) {
				for (; idx < edgesLen[u]; idx++)
					if (isDirected() || isApiEdge(edges[u][idx]))
						return true;
				idx = 0;
			}
			return false;
		}

	}

	private abstract static class EdgeImpl<E> extends EdgeAbstract<E> {

		final int u;
		final int v;

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
			return getApiEdge().val;
		}

		@Override
		public void val(E v) {
			getApiEdge().val = v;
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
			return new GraphEdgesItr();
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
