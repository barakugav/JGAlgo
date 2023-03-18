package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class GraphArrayUndirectedOld<E> extends GraphArrayAbstractOld<E> implements Graph.Undirected<E> {

	private Edge<E>[][] edges;
	private int[] edgesLen;
	private final Collection<Edge<E>> edgesView;

	public GraphArrayUndirectedOld() {
		this(0);
	}

	@SuppressWarnings("unchecked")
	public GraphArrayUndirectedOld(int n) {
		super(n);
		edges = n == 0 ? EDGES_EMPTY : new Edge[n][];
		Arrays.fill(edges, EDGES_LIST_EMPTY);
		edgesLen = n == 0 ? EDGES_LEN_EMPTY : new int[n];
		edgesView = new EdgesView();
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		EdgeImpl<E> e = new EdgeImpl<>(u, v);
		if (u != v) {
			EdgeImpl<E> eTwin = new EdgeImpl<>(v, u);
			e.twin = eTwin;
			eTwin.twin = e;
		}
		addEdge(e);
		return e;
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
	public void addEdge(Edge<E> e) {
		if (e.u() >= vertices() || e.v() >= vertices())
			throw new IllegalArgumentException("Invalid edge: " + e);

		addEdge0(e);
		Edge<E> twin = e.twin();
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

	public static <E> Graph.Undirected<E> valueOf(int n, Collection<Edge<E>> edges) {
		GraphArrayUndirectedOld<E> g = new GraphArrayUndirectedOld<>(n);
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

	private abstract class EdgeItrBase extends GraphArrayAbstractOld<E>.EdgeItrBase {

		EdgeItrBase(int u) {
			super(edges, u);
		}

		@Override
		void removeEdge(Edge<E> e, int edgeIdx) {
			removeEdge0(u, toRemoveIdx);
			Edge<E> twin = toRemoveEdge.twin();
			if (twin != null)
				removeEdge0(twin);
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
			int n = GraphArrayUndirectedOld.this.n;
			Edge<E>[][] edges = GraphArrayUndirectedOld.this.edges;
			int[] edgesLen = GraphArrayUndirectedOld.this.edgesLen;

			// don't support remove in case idx is moved
			toRemoveEdge = null;

			for (; u < n; u++) {
				for (; idx < edgesLen[u]; idx++)
					if (isApiEdge(edges[u][idx]))
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

	private static class EdgeImpl<E> extends EdgeAbstract<E> {

		EdgeImpl<E> twin;

		private EdgeImpl(int u, int v) {
			super(u, v);
		}

		EdgeImpl<E> getApiEdge() {
			return isApiEdge(this) ? this : twin;
		}

		@Override
		public E data() {
			return getApiEdge().data;
		}

		@Override
		public void setData(E data) {
			getApiEdge().data = data;
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
			return ((u == e.u() && v == e.v()) || (u == e.v() && v == e.u())) && Objects.equals(data(), e.data());
		}

		@Override
		public int hashCode() {
			return u ^ v ^ Objects.hashCode(data());
		}

	}
}
