package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GraphTable<E> extends GraphAbstract<E> {

	private final int n;
	private int m;
	private final boolean directed;
	private final Edge<E>[][] edges;
	private final Collection<Edge<E>> edgesView;

	@SuppressWarnings("unchecked")
	public GraphTable(DirectedType directed, int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.directed = directed == DirectedType.Directed;
		this.n = n;

		edges = new Edge[n][n];
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
		return new EdgesItrVertex(u, false);
	}

	@Override
	public Edge<E> getEdge(int u, int v) {
		return edges[u][v];
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	@Override
	public int newVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		if (directed) {
			EdgeDirectedImpl<E> e = new EdgeDirectedImpl<>(u, v);
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
		if (edges[e.u()][e.v()] != null)
			throw new IllegalArgumentException("Duplicate edges are not supported");
		Edge<E> twin = e.twin();
		if (isDirected() && twin != null)
			throw new IllegalArgumentException("twin edges are only supported in undirected graphs");

		edges[e.u()][e.v()] = e;
		if (twin != null)
			edges[e.v()][e.u()] = twin;
		m++;
	}

	@Override
	public void removeEdgesOut(int u) {
		if (directed) {
			for (int v = 0; v < n; v++) {
				if (edges[u][v] != null) {
					edges[u][v] = null;
					m--;
				}
			}
		} else {
			for (int v = 0; v < n; v++) {
				if (edges[u][v] != null) {
					edges[u][v] = edges[v][u] = null;
					m--;
				}
			}

		}
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
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

	private static <E> boolean isApiEdge(Edge<E> e) {
		Edge<E> twin = e.twin();
		if (twin == null)
			return true;
		if (e.u() != e.v())
			return e.u() < e.v();
		return System.identityHashCode(e) <= System.identityHashCode(twin);
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

	private class EdgesItr implements Iterator<Edge<E>> {

		private int u;
		private EdgesItrVertex uIter;

		EdgesItr() {
			u = 0;
			uIter = null;
			advanceUntilNext();
		}

		@Override
		public boolean hasNext() {
			return u != -1;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Edge<E> ret = uIter.next();

			advanceUntilNext();

			return ret;
		}

		private void advanceUntilNext() {
			if (uIter != null && uIter.hasNext())
				return;
			if (uIter != null)
				u++;
			for (; u < n; u++) {
				uIter = new EdgesItrVertex(u, true);
				if (uIter.hasNext())
					return;
			}
			u = -1;
			uIter = null;
		}

	}

	private class EdgesItrVertex implements Iterator<Edge<E>> {

		private final int u;
		private int v;
		private final boolean onlyApiEdges;

		EdgesItrVertex(int u, boolean onlyApiEdges) {
			if (!(0 <= u && u < n))
				throw new IllegalArgumentException("Illegal vertex: " + u);
			this.u = u;
			this.onlyApiEdges = onlyApiEdges;

			v = 0;
			advanceUntilNext();
		}

		@Override
		public boolean hasNext() {
			return v != -1;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Edge<E> ret = edges[u][v];

			v++;
			advanceUntilNext();

			return ret;
		}

		private void advanceUntilNext() {
			for (int next = v; next < n; next++) {
				if (edges[u][next] != null && (!onlyApiEdges || isDirected() || isApiEdge(edges[u][next]))) {
					v = next;
					return;
				}
			}
			v = -1;
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
			if (e.u() >= n || e.v() >= 0)
				return false;
			return edges[e.u()][e.u()] == e;
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
			for (int u = 0; u < n; u++)
				Arrays.fill(edges[u], null);
			m = 0;
		}
	}

}
